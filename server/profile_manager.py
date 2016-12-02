import hashlib

from db import session, Player, Chat
from sqlalchemy import and_


def get_players():
    players = session.query(Player).all()
    return list(map(Player.toDict, players)), 200


def get_chat_players(chat_id):
    chat = session.query(Chat).filter_by(id=chat_id).first()
    if not chat:
        return {"error": "Chat doesn't exist"}, 400
    players = session.query(Player)\
                     .filter(and_(Player.chat_id == chat_id,
                                  Player.id != chat.leader_id))\
                     .all()
    return list(map(Player.toDict, players)), 200


def get_player(player_id):
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {}, 404
    return player.toDict(), 200


def add_player(json):
    if not json:
        return {"error": "JSON is null"}, 400
    for parametr in ['login', 'sex', 'password']:
        if parametr not in json:
            return {"error": "no " + parametr + " in JSON"}, 400
    for parametr in ['login', 'password']:
        if json[parametr] == "":
            return {"error": parametr + " should be not empty"}

    if session.query(Player).filter_by(login=json['login']).first():
        return {"error": "Login is taken."}, 422  # Unprocessible entity

    password = json.get("password", "")
    password_hash = hashlib.md5(password.encode("utf-8")).hexdigest()

    player = Player(login=json["login"],
                    sex=json["sex"],
                    password_hash=password_hash,
                    age=json.get("age", 0),
                    status="IDLE")

    session.add(player)
    session.commit()
    return player.toDict(), 201


def sign_in(login, password):
    password_hash = hashlib.md5(password.encode("utf-8")).hexdigest()
    player = session.query(Player).filter_by(login=login).first()
    if not player or player.password_hash != password_hash:
        return {"error": "Wrong login or password. Try again."}, 401
    return player.toDict(), 200
