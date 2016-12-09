import db
import itertools

from db import session, Message, Player, Chat, datetime


def create_chat(type, players_ids, leader_id):
    chat = Chat(type=type, creation_time=datetime.datetime.now(),
                is_closed=False, leader_id=leader_id, accepted=0)
    session.add(chat)
    for playerId in itertools.chain(players_ids, [leader_id]):
        player = session.query(Player).filter_by(id=playerId).first()
        player.chat_id = chat.id
        player.status = "CHATTING_AS_LEADER" if player.id == leader_id else "CHATTING_AS_PLAYER"
    session.commit()
    return chat.id


def close_chat(leader_id, winner_id):
    leader = session.query(Player).filter_by(id=leader_id).first()
    if not leader:
        return {"error": "player with provided leader_id doesn't exist"}
    chat = session.query(Chat).filter_by(id=leader.chat_id).first()
    if chat.leader_id != leader_id:
        return {"error": "You have to be a leader"}, 400
    leader.status = "IDLE"
    chat.winner_id = winner_id
    close(chat)
    session.commit()
    return {}, 200


def send_message(json):
    if not json or 'authorId' not in json\
            or 'text' not in json\
            or 'chatId' not in json\
            or not str(json["chatId"]).isdigit()\
            or not str(json["authorId"]).isdigit()\
            or json["text"] == "":
        return {}, 400
    player = session.query(Player).filter_by(id=int(json["authorId"])).first()
    chat = session.query(Chat).filter_by(id=int(json["chatId"])).first()
    if player is None \
            or chat is None\
            or chat.is_closed\
            or player not in chat.players:
        return {}, 422
    message = Message(chat_id=chat.id, text=json["text"], author_id=player.id,
                      time=datetime.datetime.now())
    session.add(message)
    session.commit()
    return message.toDict(), 200


def get_messages(chat_id, num):
    messages = session.query(Message).\
                        filter_by(chat_id=chat_id).\
                        order_by(Message.time).\
                        offset(num).\
                        all()
    return list(map(Message.toDict, messages)), 200


def get_leader(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if not leader:
        return {"error": "Chat doesn't exist"}, 400
    return {"leader": chat.leader_id}


def verify(chat):
    if chat.is_closed or chat.is_started:
        return
    if (datetime.datetime.now() - chat.creation_time).total_seconds() > 25:
        close(chat)


def close(chat):
    chat.is_closed = True
    for player in chat.players:
        player.chat_id = None
        player.status = "IDLE"
    session.commit()



def chat_status(player_id, chat_id):
    player = session.query(Player).filter_by(id=player_id).first()
    chat = session.query(Chat).filter_by(id=chat_id).first()
    verify(chat)
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not chat.is_closed and player not in chat.players:
        return {"error": "You are not in required chat"}, 400
    if chat.is_closed:
        if not chat.is_started:
            return {"result": "won't start"}, 200
        elif chat.leader_id == player.id:
            return {"result": "leader"}, 200
        elif chat.winner_id == player.id:
            return {"result": "winner"}, 200
        else:
            return {"result": "loser"}, 200
    elif chat.is_started:
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
        close(chat)
    return {}, 200

def get_chat(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    return chat.toDict(), 200



    
