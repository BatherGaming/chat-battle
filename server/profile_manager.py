import db
from db import session, Player

import hashlib

def get_players():
    players = session.query(Player).all()
    return list(map(Player.map_repr, players)), 200

def get_player(player_id):
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {}, 404
    return player.map_repr(), 200

def add_player(json):
    if not json or not 'login' in json\
            or not 'sex' in json\
            or not 'password' in json\
            or json["login"] == ""\
            or json["password"] == "":
        return {}, 400

    if session.query(Player).filter_by(login=json['login']).first():
        return {}, 422 # Unprocessible entity

    password = json.get("password", "")
    password_hash = hashlib.md5(password.encode("utf-8")).hexdigest()


    player = Player(login=json["login"],
                    sex=json["sex"],
                    password_hash=password_hash,
                    age=json.get("age", 0)
                    )
    session.add(player)
    session.commit()
    return player.map_repr(), 201

def signin(login, password):
    password_hash = hashlib.md5(password.encode("utf-8")).hexdigest()
    player = session.query(Player).filter_by(login=login).first()
    if not player or player.password_hash != password_hash:
        return {"error": "Wrong login or password. Try again."}, 401
    return player.map_repr(), 200