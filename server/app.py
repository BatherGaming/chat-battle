#!/usr/bin/python3
from flask import Flask
from flask import jsonify
from flask import make_response
from flask import request

import profile_manager
import battlemaker
import chat_backend

app = Flask(__name__)


def process(response):
    return jsonify(response[0]), response[1]


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


@app.route('/players', methods=['GET'])
def get_players():
    return process(profile_manager.get_players())


@app.route('/players/<int:player_id>', methods=['GET'])
def get_player(player_id):
    return process(profile_manager.get_player(player_id))


@app.route('/signin/<login>/<password>', methods=['GET'])
def signin(login, password):
    return process(profile_manager.signin(login, password))


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

# temporary functions for testing


@app.route('/chat/create', methods=['POST'])
def create_chat():
    return process(chat_backend.create_chat(request.json))


@app.route('/chat/close/<int:chat_id>', methods=['POST'])
def close_chat(chat_id):
    return process(chat_backend.close_chat(chat_id))


@app.route('/battlemaker/<int:player_id>', methods=['POST'])
def add_player_to_queue(player_id):
    return process(battlemaker.add_player_to_queue(player_id))


@app.route('/battlemaker/<int:player_id>', methods=['DELETE'])
def del_player_from_queue(player_id):
    return process(battlemaker.del_player_from_queue(player_id))


if __name__ == '__main__':
    app.run(debug=True)
