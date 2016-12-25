import hashlib

from db import session, Player, Chat
from sqlalchemy import and_

BASIC_RATING = 1000


def get_players():
    players = session.query(Player).all()
    return list(map(Player.to_dict, players)), 200


def get_chat_players(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if not chat:
        return {"error": "Chat doesn't exist"}, 404
    players = session.query(Player)\
                     .filter(and_(Player.chat_id == chat_id,
                                  Player.id != chat.leader_id))\
                     .all()
    return list(map(Player.to_dict, players)), 200


def get_player(player_id):
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {}, 404
    return player.to_dict(), 200


def get_hash(password):
    return hashlib.md5(password.encode("utf-8")).hexdigest()


def add_player(json):
    if not json:
        return {"error": "JSON is null"}, 400
    for parameter in ['login', 'sex', 'password']:
        if parameter not in json:
            return {"error": "no " + parameter + " in JSON"}, 400
    for parameter in ['login', 'password']:
        if not json[parameter]:
            return {"error": parameter + " should be not empty"}, 400

    if session.query(Player).filter_by(login=json['login']).first():
        return {"error": "Login is taken."}, 422  # Unprocessable entity

    password = json.get("password", "")
    password_hash = get_hash(password)

    player = Player(login=json["login"],
                    sex=json["sex"].upper(),
                    password_hash=password_hash,
                    age=json.get("age", 0),
                    status="IDLE",
                    rating=BASIC_RATING)

    session.add(player)
    session.commit()
    return player.to_dict(), 201


def sign_in(login, password):
    password_hash = get_hash(password)
    player = session.query(Player).filter_by(login=login).first()
    if not player or player.password_hash != password_hash:
        return {"error": "Wrong login or password. Try again."}, 401
    return player.to_dict(), 200

def change_password(player_id, old_password, new_password):
    player = session.query(Player).filter_by(id=player_id).first()
    old_password_hash = get_hash(old_password)
    if not player or player.password_hash != old_password_hash:
        return {"error": "Wrong old password"}, 400
    new_password_hash = get_hash(new_password)
    player.password_hash = new_password_hash
    return {}, 200
    
