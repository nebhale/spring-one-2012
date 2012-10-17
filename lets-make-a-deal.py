#!/usr/bin/env python

import json
import requests

GAME_ROOT='http://localhost:8080/lets-make-a-deal/games'

def get_link_location(source, rel):
	if isinstance(source, str):
		payload = requests.get(source).json
	else:
		payload = source

	for link in payload['links']:
		if link['rel'] == rel:
			return link['href']

	return None

def get_doors(location):
	doors = {}

	for door in requests.get(location).json['doors']:
		doors[get_link_location(door, 'self')] = { 'status': door['status'], 'content': door['content'] }

	return doors

def print_current_state(game_location, doors):
	print('')
	print('Game Status:    {}'.format(requests.get(game_location).json['status']))
	for i, key in enumerate(doors):
		door = doors[key]
		print('Door {} Status:  {}/{}'.format(i, door['status'], door['content']))

###############################################################################

game_location = requests.post(GAME_ROOT).headers['Location']
doors_location = get_link_location(game_location, 'doors')

print("Let's Make a Deal!")

doors = get_doors(doors_location)
print_current_state(game_location, doors)
selection = int(raw_input('Select a door (0, 1, or 2)... '))
r = requests.post(doors.keys()[selection], data=json.dumps({'status': 'SELECTED'}), headers={'Content-Type': 'application/json'})

doors = get_doors(doors_location)
print_current_state(game_location, doors)
selection = int(raw_input('Open a door (0, 1, or 2)... '))
requests.post(doors.keys()[selection], data=json.dumps({ 'status': 'OPEN'}), headers={'Content-Type': 'application/json'})

doors = get_doors(doors_location)
print_current_state(game_location, doors)

requests.delete(game_location)
