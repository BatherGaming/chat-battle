#!/usr//local/bin/python3
from flask import Flask
from flask import jsonify
from flask import make_response
from flask import request
from flask import send_from_directory

import base64
import os
import profile_manager
import battlemaker
import chat_backend

app = Flask(__name__)

WHITEBOARD_FOLDER = 'whiteboards'


def process(response):
    return jsonify(response[0]), response[1]


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found1'}), 404)


@app.route('/players', methods=['GET'])
def get_players():
    return process(profile_manager.get_players())


@app.route('/players/<int:player_id>', methods=['GET'])
def get_player(player_id):
    return process(profile_manager.get_player(player_id))


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


@app.route('/chat/get/leader/<int:chat_id>', methods=['GET'])
def get_leader(chat_id):
    return process(chat_backend.get_leader(chat_id))


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


if __name__ == '__main__':
    app.run(debug=True)