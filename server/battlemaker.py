from db import Player, session
from chat_backend import create_chat

queue_filename = "queue.txt"
player_amount = 2

# Make sure file is present
open(queue_filename, "a").close()


def add_player_to_queue(player_id):
    player = session.query(Player).filter_by(id=player_id).first()
    if not player:
        return {"error": "There's no such player."}, 400

    if player.chat_id:
        return {"error": "Player's already chatting."}, 400

    queue = []
    with open(queue_filename, "r") as queue_file:
        queue = queue_file.readline().split(",")

    queue = list(map(int, filter(lambda s: str(s).isdigit(), queue)))

    print(queue)

    if player_id in queue:
        return {"error": "Player's already in queue."}, 400

    queue.append(player_id)

    while len(queue) >= player_amount:
        players = []
        for i in range(player_amount):
            players.append(queue.pop())
        create_chat(0, players)

    with open(queue_filename, "w") as queue_file:
        queue_file.write(",".join(map(str, queue)))

    return {}, 200


def del_player_from_queue(player_id):
    if not session.query(Player).filter_by(id=player_id).first():
        return {"error": "There's no such player."}, 400

    queue = []
    with open(queue_filename, "r") as queue_file:
        queue = queue_file.readline().split(",")

    if player_id in queue:
        return {"error": "Player's not in queue."}, 400

    queue = list(filter(lambda x: x != player_id, queue))

    with open(queue_filename, "w") as queue_file:
        queue_file.write(",".join(queue))

    return {}, 200
