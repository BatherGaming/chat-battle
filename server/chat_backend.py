import db

from db import session, Message, Player, Chat, datetime
from sqlalchemy.sql.expression import func

def create_chat(type, playersId, leader_id):
    chat = Chat(type=type, creation_time=datetime.datetime.now(), is_closed=False, leader_id=leader_id)
    session.add(chat)
    session.commit()
    playersId.append(leader_id)
    for playerId in playersId:
        player = session.query(Player).filter_by(id=playerId).first()
        player.chat_id = chat.id
    return chat.id

def close_chat(leader_id, winner_id):
    leader = session.query(Player).filter_by(id=leader_id).first()
    chat = session.query(Chat).filter_by(id=leader.chat_id).first()
    if chat.leader_id != leader_id:
        return {"error":"You have to be a leader"}, 400
    chat.winner_id = winner_id
    chat.is_closed = True
    for player in chat.players:
        player.chat_id = None
    session.commit()
    return {}, 200

def send_message(json):
    if not json or not 'authorId' in json\
        	or not 'text' in json\
            or not 'chatId' in json\
            or not str(json["chatId"]).isdigit()\
            or not str(json["authorId"]).isdigit()\
            or json["text"] == "":
        return {}, 400
    player = session.query(Player).filter_by(id=int(json["authorId"])).first()
    chat = session.query(Chat).filter_by(id=int(json["chatId"])).first()
    if player == None \
            or chat == None\
            or chat.is_closed\
            or player not in chat.players:
        return {}, 422
    message = Message(chat_id=chat.id, text=json["text"], author_id=player.id, time=datetime.datetime.now())
    session.add(message)
    session.commit()
    return message.map_repr(), 200

def get_messages(chat_id, num):
	messages = session.query(Message).\
						filter_by(chat_id = chat_id).\
						order_by(Message.time).\
						offset(num).\
                        all()
	return list(map(Message.map_repr, messages)), 200

def is_finished(player_id, chat_id):
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
        return {"result":"running"}, 200




   


