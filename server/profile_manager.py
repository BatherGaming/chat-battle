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
            or not 'sex' in json:
        return {}, 400

    if session.query(Player).filter_by(login=json['login']).first():
        return {}, 422 # Unprocessible entity

    if json["login"] == "":
        return {}, 400
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