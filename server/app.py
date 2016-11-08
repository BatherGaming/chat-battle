#!/usr/bin/python3
from flask import Flask
from flask import jsonify
from flask import make_response
from flask import request

import profile_manager

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



if __name__ == '__main__':
    app.run(debug=True)
