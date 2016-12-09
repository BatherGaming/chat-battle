import db
import uuid
import itertools
import base64
import os

from db import session, Message, Player, Chat, datetime

WHITEBOARD_FOLDER = 'whiteboards'
DOMAIN_NAME = "qwsafex.pythonanywhere.com"  

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
    chat.is_closed = True
    for player in chat.players:
        player.chat_id = None
        player.status = "IDLE"
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
    if not json or 'authorId' not in json\
            or 'text' not in json\
            or 'chatId' not in json\
            or not str(json["chatId"]).isdigit()\
            or not str(json["authorId"]).isdigit():
        return {}, 400
    if json["text"] == "" and ('whiteboard' not in json 
                               or json['whiteboard'] == ""):
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

    if 'whiteboard' in json and json['whiteboard'] != "":
        message.whiteboard_tag = save_whiteboard(json['whiteboard'])

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


def chat_status(player_id, chat_id):
    player = session.query(Player).filter_by(id=player_id).first()
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not chat.is_closed and player not in chat.players:
        return {"error": "You are not in required chat"}, 400
    if chat.is_closed:
        if chat.leader_id == player.id:
            return {"result": "leader"}, 200
        elif chat.winner_id == player.id:
            return {"result": "winner"}, 200
        else:
            return {"result": "loser"}, 200
    else:
        return {"result": "running"}, 200


def accept(player_id):
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {"error": "Player doesn't exist"}, 400
    if not player.chat_id:
        return {"error": "Player is not in chat"}, 400
    chat = session.query(Chat).filter_by(id=player.chat_id).first()
    

def get_whiteboard(whiteboard_tag):
    location = whiteboard_location(whiteboard_tag)
    with open(location, "rb") as f:
        whiteboard = f.read()
        return base64.b64encode(whiteboard)
    return ""
