import db

from db import session, Message, Player, Chat, datetime
from sqlalchemy.sql.expression import func

def create_chat(json):
    #temporary for testing
    type = json.get("type")
    playersId = json["playersId"]
    
    chat = Chat(type=type, creation_time=datetime.datetime.now(), is_closed=False)
    session.add(chat)
    session.commit()
    for playerId in playersId:
        player = session.query(Player).filter_by(id=playerId).first()
        if player.chat_id != None:
            return {}, 422
        player.chat_id = chat.id
    return chat.id, 200

def close_chat(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    chat.is_closed = True
    for player in chat.players:
        player.chat_id = None
    session.commit()
    return {}, 200

def send_message(json):
    if not json or not 'autorId' in json\
        	or not 'text' in json\
            or not 'chatId' in json\
            or not str(json["chatId"]).isdigit()\
            or not str(json["autorId"]).isdigit()\
            or json["text"] == "":
        return {}, 400
    player = session.query(Player).filter_by(id=int(json["autorId"])).first()
    chat = session.query(Chat).filter_by(id=int(json["chatId"])).first()
    if player == None \
            or chat == None\
            or chat.is_closed\
            or player not in chat.players:
        return {}, 422
    message = Message(chat_id=chat.id, text=json["text"], sender_id=player.id, time=datetime.datetime.now())
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



