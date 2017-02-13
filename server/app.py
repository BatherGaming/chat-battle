#!/usr//local/bin/python3
from flask import Flask
from flask import jsonify
from flask import make_response
from flask import request

import profile_manager
import battlemaker
import chat_backend

app = Flask(__name__)

WHITEBOARD_FOLDER = 'whiteboards'


def process(response):
    return jsonify(response[0]), response[1]


@app.route('/players', methods=['GET'])
def get_players():
    return process(profile_manager.get_players())

@app.route('/player/change_pass/<int:player_id>/<old_password>/<new_password>', methods=['POST'])
def change_password(player_id, old_password, new_password):
    return process(profile_manager.change_password(player_id, old_password, new_password))


@app.route('/players/<int:player_id>', methods=['GET'])
def get_player(player_id):
    return process(profile_manager.get_player(player_id))


@app.route('/players/leaderboard', methods=['GET'])
def get_leaderboard():
    return process(profile_manager.get_leaderboard())

@app.route('/sign_in/<login>/<password>', methods=['GET'])
def sign_in(login, password):
    return process(profile_manager.sign_in(login, password))


@app.route('/players', methods=['POST'])
def add_player():
    if not request.get_json(silent=True):
        return process(({"error": "Please, provide JSON."}, 400))
    return process(profile_manager.add_player(request.get_json()))


@app.route('/chat/send', methods=['POST'])
def send_message():
    return process(chat_backend.send_message(request.json))


@app.route('/chat/get/<int:chat_id>/<int:num>', methods=['GET'])
def get_messages(chat_id, num):
    return process(chat_backend.get_messages(chat_id, num))


@app.route('/chat/close/<int:leader_id>/<int:winner_id>', methods=['POST'])
def close_chat(leader_id, winner_id):
    print("po ebalu")
    return process(chat_backend.close_chat(leader_id, winner_id))


@app.route('/battlemaker/<role>/<int:player_id>', methods=['POST'])
def add_player_to_queue(role, player_id):
    return process(battlemaker.add_player_to_queue(role, player_id))


@app.route('/battlemaker/<int:player_id>', methods=['DELETE'])
def del_player_from_queue(player_id):
    return process(battlemaker.del_player_from_queue(player_id))


@app.route('/chat/chat_status/<int:player_id>/<int:chat_id>', methods=['GET'])
def chat_status(player_id, chat_id):
    return process(chat_backend.chat_status(player_id, chat_id))


@app.route('/profile_manager/players/<int:chat_id>', methods=['GET'])
def get_chat_players(chat_id):
    return process(profile_manager.get_chat_players(chat_id))


@app.route('/chat/mute/<int:chat_id>/<int:player_id>/<int:mute_time>', methods=['POST'])
def mute_player(player_id, chat_id, mute_time):
    return process(chat_backend.mute_player(player_id, chat_id, mute_time))


@app.route('/chat/kick/<int:chat_id>/<int:player_id>', methods=['POST'])
def kick_player(player_id, chat_id):
    return process(chat_backend.kick_player(player_id, chat_id))


@app.route('/chat/accept/<int:player_id>', methods=['POST'])
def accept(player_id):
    return process(chat_backend.accept(player_id))


@app.route('/chat/decline/<int:player_id>', methods=['POST'])
def decline(player_id):
    return process(chat_backend.decline(player_id))


@app.route('/whiteboards/<whiteboard_tag>')
def get_whiteboard(whiteboard_tag):
    # No process() needed because of raw return value
    return chat_backend.get_whiteboard(whiteboard_tag) 


@app.route('/chat/<int:chat_id>', methods=['GET'])
def get_chat(chat_id):
    return process(chat_backend.get_chat(chat_id))


@app.route('/profile_manager/reset_password/<login>', methods=['POST'])
def reset_password(login):
    return process(profile_manager.reset_password(login))


@app.route('/chat/time_left/<int:chat_id>', methods=['GET'])
def get_time_left(chat_id):
    return process(chat_backend.get_time_left(chat_id))


@app.route('/chat/summary/<int:chat_id>', methods=['GET'])
def get_chat_summary(chat_id):
    return process(chat_backend.get_summary(chat_id))


@app.route('/chat/list', methods=['GET'])
def get_chats():
    return process(chat_backend.get_chats())


@app.route('/profile_manager/ratings/<player_ids>', methods=['GET'])
def get_ratings(player_ids):
    return process(profile_manager.get_ratings(player_ids))

if __name__ == '__main__':
    app.run(debug=True)
