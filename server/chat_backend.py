import uuid
import itertools
import base64
import os

from random import randrange
from db import session, Message, Player, Chat, datetime

WHITEBOARD_FOLDER = 'whiteboards'
DOMAIN_NAME = "qwsafex.pythonanywhere.com"  

WINNER_DELTA = 1
LOSER_DELTA = -1
BATTLE_TIME = 30


def create_chat(chat_type, players_ids, leader_id):
    print("create_chat")
    chat = Chat(type=chat_type, creation_time=datetime.datetime.now(),
                is_closed=False, leader_id=leader_id, accepted=0)
    session.add(chat)
    for playerId in itertools.chain(players_ids, [leader_id]):
        player = session.query(Player).filter_by(id=playerId).first()
        player.chat_id = chat.id
        player.penalty = "NONE"
        player.status = "CHATTING_AS_LEADER" if player.id == leader_id else "CHATTING_AS_PLAYER"
    session.commit()
    return chat.id


def close(chat, winner_id):
    print("closing")
    if chat.is_closed:
        return
    chat.is_closed = True
    for player in chat.players:
        player.chat_id = None
        player.status = "IDLE"
        if chat.is_started and player.id != chat.leader_id:
            player.rating += WINNER_DELTA if player.id == winner_id else LOSER_DELTA
    session.commit()


def close_chat(leader_id, winner_id):
    print("close_chat")
    leader = session.query(Player).filter_by(id=leader_id).first()
    if not leader:
        return {"error": "player with provided leader_id doesn't exist"}, 400
    chat = session.query(Chat).filter_by(id=leader.chat_id).first()
    if chat.leader_id != leader_id:
        return {"error": "You have to be a leader"}, 400
    leader.status = "IDLE"
    chat.winner_id = winner_id
    close(chat, winner_id)
    session.commit()
    return {}, 200


def whiteboard_location(filename):
    return os.path.join("mysite", WHITEBOARD_FOLDER, filename)


def unique_filename():
    base_filename = uuid.uuid4().hex[0:15]  # Which makes 2^64 different UUIDs
    filename = whiteboard_location(base_filename)
    while os.path.isfile(filename):
        base_filename = uuid.uuid4().hex[0:15]
        filename = whiteboard_location(base_filename)
    return base_filename


def save_whiteboard(whiteboard_body):
    whiteboard_body = base64.b64decode(whiteboard_body)
    base_filename = unique_filename()
    filename = whiteboard_location(base_filename)
    with open(filename, "wb") as file:
        file.write(whiteboard_body)
    return base_filename


def send_message(json):
    print("send_message")
    if not json or 'authorId' not in json\
            or 'text' not in json\
            or 'chatId' not in json\
            or not str(json["chatId"]).isdigit()\
            or not str(json["authorId"]).isdigit()\
            or not json["text"] and ('whiteboard' not in json
                                     or json['whiteboard'] == ""):
        return {}, 400

    player = session.query(Player).filter_by(id=int(json["authorId"])).first()
    chat = session.query(Chat).filter_by(id=int(json["chatId"])).first()
    if player is None \
            or chat is None\
            or chat.is_closed\
            or player not in chat.players:
        return {}, 422

    if player.penalty == "MUTED":
        return {"error": "You're muted"}, 400
    message = Message(chat_id=chat.id, text=json["text"], author_id=player.id,
                      time=datetime.datetime.now())

    if 'whiteboard' in json and json['whiteboard'] != "":
        message.whiteboard_tag = save_whiteboard(json['whiteboard'])

    session.add(message)
    session.commit()
    return message.to_dict(), 200


def get_messages(chat_id, num):
    messages = session.query(Message).\
                        filter_by(chat_id=chat_id).\
                        order_by(Message.time).\
                        offset(num).\
                        all()
    return list(map(Message.to_dict, messages)), 200


def verify(chat):
    print("verifying")
    if chat.is_closed:
        return
    if chat.is_started:
        print(chat.end_time, datetime.datetime.now())
        if chat.end_time < datetime.datetime.now():
            print("trying to close")
            player_amount = len(chat.players)
            winner_n = randrange(0,player_amount-1)
            winner_id = -1
            for player in chat.players:
                if player.id != chat.leader_id:
                    if winner_n == 0:
                        winner_id = player.id
                        break;
                    winner_n -= 1
            print("winner: ", winner_id)
            close_chat(chat.leader_id, winner_id)
        return
    if (datetime.datetime.now() - chat.creation_time).total_seconds() > 25:
        print("mamku ebal")
        close(chat, -1)



def chat_status(player_id, chat_id):
    print("chat_status")
    player = session.query(Player).filter_by(id=player_id).first()
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if player.penalty == "MUTED" and  player.mute_end_time < datetime.datetime.now():
        player.penalty = "NONE"
    verify(chat)
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if player.penalty == "KICKED":
        return {"result": "kicked"}, 200
    if not chat.is_closed and player not in chat.players:
        return {"error": "You are not in required chat"}, 400
    if chat.is_closed:
        if not chat.is_started:
            return {"result": "won't start"}, 200
        elif chat.leader_id == player.id:
            return {"result": "leader"}, 200
        elif chat.winner_id == player.id:
            return {"result": "winner", "rating": player.rating}, 200
        else:
            return {"result": "loser", "rating": player.rating}, 200
    elif chat.is_started:
        if player.penalty == "MUTED":
            return {"result": "muted"}, 200
        else:
            return {"result": "running"}, 200
    else:
        return {"result": "waiting"}, 200


def accept(player_id):
    print('accepted', player_id)
    player = session.query(Player).filter_by(id=player_id).first()
    if player is None:
        return {"error": "Player doesn't exist"}, 400
    if player.chat_id is None:
        return {"error": "Player is not in chat"}, 400
    chat = session.query(Chat).filter_by(id=player.chat_id).first()
    verify(chat)
   
    chat.accepted += 1
    if chat.accepted == len(chat.players):
        print('chat is created')
        chat.is_started = True
        chat.end_time = datetime.datetime.now() + datetime.timedelta(seconds=BATTLE_TIME)
    session.commit()
    return {}, 200


def decline(player_id):
    print('declined', player_id)
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not player.chat_id:
        return {"error": "Player is not in chat"}, 400
    chat = session.query(Chat).filter_by(id=player.chat_id).first()
    if not chat.is_closed:
        close(chat, -1)
    return {}, 200


def get_chat(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    return chat.to_dict(), 200


def get_whiteboard(whiteboard_tag):
    location = whiteboard_location(whiteboard_tag)
    with open(location, "rb") as f:
        whiteboard = f.read()
        return base64.b64encode(whiteboard)

def mute_player(player_id, chat_id, mute_time):
    print("mute_player")
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not player.chat_id or player.chat_id != chat_id:
        return {"error": "Player is not in this chat"}, 400
    chat = session.query(Chat).filter_by(id=player.chat_id).first()
    if chat.is_closed:
        return {}, 200
    player.penalty = "MUTED"
    player.mute_end_time = datetime.datetime.now() + datetime.timedelta(seconds=mute_time)
    print("mute2")
    session.commit()
    return {}, 200

def kick_player(player_id, chat_id):
    print("kick_player")
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not player.chat_id or player.chat_id != chat_id:
        return {"error": "Player is not in this chat"}, 400
    chat = session.query(Chat).filter_by(id=player.chat_id).first()

    # TODO: think about desired behaviour in this case
    if len(chat.players) == 2:
        return {"error": "You can't kick last player"}, 400 

    if chat.is_closed:
        return {}, 200
    player.penalty = "KICKED"
    player.status = "IDLE"
    player.rating += LOSER_DELTA
    player.chat_id = None
    session.commit()

    return {}, 200

def get_time_left(chat_id):
    print("get_time_left")
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    if chat.is_closed:
        return {"error": "Chat has already closed"}, 400
    if not chat.is_started:
        return {"error": "Chat has not yet started"}, 400

    return {"time": (chat.end_time - datetime.datetime.now()).total_seconds()}, 200

def get_summary(chat_id):
    print("get_summary")
    chat = session.query(Chat).filter_by(id=chat_id).first()
    def to_dict(player):
        delta = 0
        if player.id == chat.winner_id:
            delta = WINNER_DELTA
        elif player.id == chat.leader_id:
            delta = 0
        else:
            delta = LOSER_DELTA
        return {"id": player.id, "login": player.login, "new_rating": player.rating,
                            "old_rating": player.rating - delta}
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    if not chat.is_started or not chat.is_closed:
        return {"error": "Chat summary doesn't exist"}, 400

    leader = session.query(Player).filter_by(id=chat.leader_id).first()
    winner = session.query(Player).filter_by(id=chat.winner_id).first()
    print("Chat players: ", chat.players)

    players = []
    for player in chat.players:
        if player.id != leader.id and player.id != winner.id:
            players.append(to_dict(player))
    return {"leader": to_dict(leader), "winner": to_dict(winner)}, 200