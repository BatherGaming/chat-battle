import hashlib
import smtplib
import random
import string
from db import session, Player, Chat
from sqlalchemy import and_
from chat_backend import LOSER_DELTA

BASIC_RATING = 1000
PASSWORD_LENGTH = 10
SERVER_EMAIL = "noreply.chatbattle@gmail.com"
SERVER_EMAIL_PASSWORD = "qwerty123456asdfgh123456"


def get_players():
    players = session.query(Player).all()
    return list(map(Player.to_dict, players)), 200


def get_chat_players(chat_id): #1
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


def get_leaderboard():
    leaderboard = session.query(Player).order_by(Player.rating.desc()).all()
    return list(map(Player.to_dict, leaderboard)), 200


def add_player(json):
    if not json:
        return {"error": "JSON is null"}, 400
    for parameter in ['login', 'password']:
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
                    password_hash=password_hash,
                    status="IDLE",
                    rating=BASIC_RATING,
                    email=json["email"])

    session.add(player)
    session.commit()
    return player.to_dict(), 201


def sign_in(login, password):
    password_hash = get_hash(password)
    player = session.query(Player).filter_by(login=login).first()
    if not player or player.password_hash != password_hash:
        return {"error": "Wrong login or password. Try again."}, 401
    return player.to_dict(), 200


def set_password(player, new_password):
    new_password_hash = get_hash(new_password)
    player.password_hash = new_password_hash


def change_password(player_id, old_password, new_password):
    player = session.query(Player).filter_by(id=player_id).first()
    old_password_hash = get_hash(old_password)
    if not player or player.password_hash != old_password_hash:
        return {"error": "Wrong old password"}, 400
    set_password(player, new_password)
    return {}, 200


def reset_password(login):
    player = session.query(Player).filter_by(login=login).first()
    if not player:
        return {"error": "Player with provided id doesn't exist"}, 400
    new_password = ''.join(random.choice(string.ascii_uppercase +
                                         string.digits)
                           for _ in range(PASSWORD_LENGTH))
    set_password(player, new_password)

    FROM = SERVER_EMAIL
    recipient = player.email
    TO = recipient if type(recipient) is list else [recipient]
    SUBJECT = 'Chat battle account password reset'
    TEXT = 'Login: ' + player.login + '\n' + 'New password: ' + new_password

    message = """From: %s\nTo: %s\nSubject: %s\n\n%s
    """ % (FROM, ", ".join(TO), SUBJECT, TEXT)
    server = smtplib.SMTP("smtp.gmail.com", 587)
    server.ehlo()
    server.starttls()
    server.login(SERVER_EMAIL, SERVER_EMAIL_PASSWORD)
    server.sendmail(SERVER_EMAIL, TO, message)
    server.close()
    return {}, 200


def get_ratings(player_ids):
    def to_dict(player):
        return {"id": player.id, "login": player.login,
                "new_rating": player.rating,
                "old_rating": player.rating - LOSER_DELTA}

    player_ids = player_ids.split(",")
    players = []
    for id in player_ids:
        player = session.query(Player).filter_by(id=id).first()
        players.append(to_dict(player))

    return players, 200
