from db import Player, session
from chat_backend import create_chat

queue_filename = "queue.txt"
player_amount = 1



# Make sure file is present
open(queue_filename, "a").close()

def read_queues_from_file():
	queues = []
	with open(queue_filename, "r") as queue_file:
		queues = [queue_file.readline()[:-1].split(",") for i in range(2)]
	queues = [list(map(int, filter(lambda s: str(s).isdigit(), queue))) for queue in queues]
	return queues[0], queues[1]

def write_queues_in_file(players_queue, leaders_queue):
	with open(queue_filename, "w") as queue_file:
		queue_file.write(",".join(map(str, players_queue)) + '\n')
		queue_file.write(",".join(map(str, leaders_queue)) + '\n')


def add_player_to_queue(role, player_id):	
	player = session.query(Player).filter_by(id=player_id).first()
	if not player:
		return {"error": "There's no such player."}, 400

	if player.chat_id:
		return {"error": "Player's already chatting."}, 400

	players_queue, leaders_queue = read_queues_from_file()
	
	if role == "player":
		if player_id in players_queue:
			return {"error": "Player's already in queue."}, 400
		if player_id in leaders_queue:
			leaders_queue.remove(player_id)
		players_queue.append(player_id)
	elif role == "leader":
		if player_id in leaders_queue:
			return {"error": "Player's already in queue"}, 400
		if player_id in players_queue:
			players_queue.remove(player_id)
		leaders_queue.append(player_id)
	else:
		return {"error": "Can't parse role"}, 400


	while len(players_queue) >= player_amount and leaders_queue != []:
		players = []
		for i in range(player_amount):
			players.append(players_queue.pop(0))
		create_chat(0, players, leaders_queue.pop(0))

	
	write_queues_in_file(players_queue, leaders_queue)

	return {}, 200

def del_player_from_queue(player_id):
	if not session.query(Player).filter_by(id=player_id).first():
		return {"error": "There's no such player."}, 400

	players_queue, leaders_queue = read_queues_from_file()

	for queue in [players_queue, leaders_queue]:
		queue = list(filter(lambda x: x != player_id, queue))

	write_queues_in_file(players_queue, leaders_queue)

	return {}, 200
