# This is a program used for quickly testing a local server. The amount of methods requested here is more expansive
# compared to heroku_requests, as that program is solely used to supply sample data for the app.

import requests

database_url = 'http://127.0.0.1:5000' # Every other url will build upon this url

# USERS

user1 = {'username': 'coolname123', 'password': 'coolerpassword321'}
user2 = {'username': 'wandmaster', 'password': 'alohomora'}
user3 = {'username': 'wandrookie', 'password': 'abracadabra'}
user4 = {'username': 'admin', 'password': 'masterofuniverse'}

add_user_url = database_url + '/users'
add_user1 = requests.post('http://127.0.0.1:5000/users', json=user1)
add_user2 = requests.post('http://127.0.0.1:5000/users', json=user2)
add_user3 = requests.post('http://127.0.0.1:5000/users', json=user3)
add_admin = requests.post('http://127.0.0.1:5000/users', json=user4)

login_url = database_url + '/users/login'
login1 = requests.post(login_url, json=user1)
login2 = requests.post(login_url, json=user2)
login3 = requests.post(login_url, json=user3)
login4 = requests.post(login_url, json=user4)

token1 = login1.json()['token']
token2 = login2.json()['token']
token3 = login3.json()['token']
token4 = login4.json()['token']

print(token1)
print(token2)
print(token3)
print(token4)

header_coolname123 = {'Authorization': 'Bearer ' + token1}
header_wandmaster = {'Authorization': 'Bearer ' + token2}
header_wandrookie = {'Authorization': 'Bearer ' + token3}
header_admin = {'Authorization': 'Bearer ' + token4}

#delete_user = requests.delete('http://127.0.0.1:5000/users/wandmaster', headers=header_admin)
get_all_users = requests.get('http://127.0.0.1:5000/users')
search_users = requests.get('http://127.0.0.1:5000/users/search/wand')
#logout = requests.post('http://127.0.0.1:5000/users/logout', headers=header_user2)

#print(add_user1.text, add_user1.status_code)
#print(add_user2.text, add_user2.status_code)
#print(add_user3.text, add_user3.status_code)
#print(delete_user.text, delete_user.status_code)
print(get_all_users.text, get_all_users.status_code)
#print(search_users.text, search_users.status_code)
#print(logout.text, logout.status_code)

# GAMES

game1 = {'id': 'drks3', 'img':'https://images.igdb.com/igdb/image/upload/t_cover_big/co1swv.jpg', 'title': 'Darkstalkers 3', 'release_year': 1997, 'publisher': 'Capcom', 'platforms': ['Arcade', 'Playstation 1', 'Sega Saturn'], 'alt_titles': ['Vampire Savior']}
game2 = {'id': 'ssb64', 'title': 'Super Smash Brothers', 'release_year': 1999, 'publisher': 'Nintendo', 'platforms': ['Nintendo 64']}
game3 = {'id': 'umvc3', 'title': 'Ultimate Marvel vs Capcom 3', 'release_year': 2011, 'publisher': 'Capcom'}
game4 = {'id': 'sf3rs', 'title': 'Street Fighter III: 3rd Strike', 'release_year': 1999, 'release_month': 5, 'publisher': 'Capcom'}
game5 = {'id': 'sfxtk', 'title': 'Street Fighter X Tekken', 'release_year': 2012, 'publisher': 'Capcom'}

add_game1 = requests.post('http://127.0.0.1:5000/games', json=game1, headers=header_admin)
add_game2 = requests.post('http://127.0.0.1:5000/games', json=game2, headers=header_admin)
add_game3 = requests.post('http://127.0.0.1:5000/games', json=game3, headers=header_admin)
add_game4 = requests.post('http://127.0.0.1:5000/games', json=game4, headers=header_admin)
add_game5 = requests.post('http://127.0.0.1:5000/games', json=game5, headers=header_admin)

edit_game = requests.post('http://127.0.0.1:5000/games/ssb64/edit', json={'title': 'Soup Slash Broods.', 'release_year': 2112}, headers=header_admin)
#delete_game = requests.delete('http://127.0.0.1:5000/games/drks3', headers=header_admin)
retrieve_games1 = requests.get('http://127.0.0.1:5000/games')
#retrieve_games2 = requests.get('http://127.0.0.1:5000/games')
#retrieve_single_game = requests.get('http://127.0.0.1:5000/games/drks3')
search_games1 = requests.get('http://127.0.0.1:5000/games/search/title/street%fight')
search_games2 = requests.get('http://127.0.0.1:5000/games/search/platform/Sega%Saturn')
sort_games = requests.get('http://127.0.0.1:5000/games/sort/asc/release_year')

#print(add_game1.text, add_game1.status_code)
#print(add_game2.text, add_game2.status_code)
#print(edit_game.text, edit_game.status_code)
#print(delete_game.text, delete_game.status_code)
#print(retrieve_games1.text, retrieve_games1.status_code)
#print(retrieve_games2.text, retrieve_games2.status_code)
#print(retrieve_single_game.text, retrieve_single_game.status_code)
#print(sort_games.text, sort_games.status_code)
#print(search_games1.text, search_games1.status_code)
#print(search_games2.text, search_games2.status_code)

# TOURNEYS
#Ta bort host_id när autentisering är klart

tourney1 = {'start_year': 2020, 'start_month': 6, 'start_day': 31, 'start_time': '18:30',  \
                'title': 'EVO 2020: Quarantine Edition', 'game_id': 'umvc3', 'location': 'Las Vegas, Nevada, US',
                'end_year': 2020, 'end_month': 8, 'end_day': 2, 'end_time': '12:00', \
                'description': 'The biggest, most hype and most prestigious fighting game tournament in the world!'}
tourney2 = {'start_year': 2021, 'start_month': 4, 'start_day': 20, 'start_time': '13:37', \
                'end_year': 2021, 'end_month': 4, 'end_day': 20, 'end_time': '22:00', \
                'title': 'Linkan Overlords 64', 'game_id': 'ssb64', \
                'location': 'Linköping, Östergötland, Sweden'}

add_tourney1 = requests.post('http://127.0.0.1:5000/tourneys', json=tourney1, headers=header_wandrookie)
add_tourney2 = requests.post('http://127.0.0.1:5000/tourneys', json=tourney2, headers=header_coolname123)

get_all_tourneys1 = requests.get('http://127.0.0.1:5000/tourneys')

tourney1_id = ""
tourney2_id = ""
if get_all_tourneys1.status_code == 200:
    search_results = eval(get_all_tourneys1.text)
    for dict in search_results['tourneys']:
        if dict['title'] == "EVO 2020: Quarantine Edition":
            tourney1_id = dict['id']
        if dict['title'] == "Linkan Overlords 64":
            tourney2_id = dict['id']

get_tourney_url = 'http://127.0.0.1:5000/tourneys/{}'.format(tourney1_id)
get_tourney = requests.get(get_tourney_url)

register1_url = 'http://127.0.0.1:5000/tourneys/{}/register'.format(tourney1_id)
register2_url = 'http://127.0.0.1:5000/tourneys/{}/register'.format(tourney1_id)
register3_url = 'http://127.0.0.1:5000/tourneys/{}/register'.format(tourney2_id)

register1 = requests.post(register1_url, headers=header_coolname123)
#register1_copy = requests.post(register1_url)
register2 = requests.post(register2_url, headers=header_wandmaster)
register3 = requests.post(register3_url, headers=header_wandmaster)

#delete_tourney_url = 'http://127.0.0.1:5000/tourneys/{}'.format(tourney1_id)
#delete_tourney = requests.delete(delete_tourney_url, headers=header_wandrookie)
delete_participant = requests.delete(register2_url, headers=header_wandmaster)
search_tourney1 = requests.get('http://127.0.0.1:5000/tourneys/search/game/mvc')
tourney1_dict = eval(search_tourney1.text)
sort_tourney = requests.get('http://127.0.0.1:5000/tourneys/sort/des/start_year', json=tourney1_dict)
edit_tourney_url = 'http://127.0.0.1:5000/tourneys/{}/edit'.format(tourney1_id)
edit_tourney = requests.post(edit_tourney_url, json={'start_month': 7, 'start_time': '14:00'}, headers=header_wandrookie)

like1_url = 'http://127.0.0.1:5000/tourneys/{}/likes'.format(tourney1_id)
like2_url = 'http://127.0.0.1:5000/tourneys/{}/likes'.format(tourney1_id)
like1 = requests.post(like1_url, headers=header_coolname123)
like2 = requests.post(like2_url, headers=header_wandmaster)
#unlike = requests.delete(like1_url, headers=header_coolname123)
get_likes_url = 'http://127.0.0.1:5000/tourneys/{}/likes'.format(tourney1_id)
get_likes = requests.get(get_likes_url)

comment1_url = 'http://127.0.0.1:5000/tourneys/{}/comments'.format(tourney1_id)
comment2_url = 'http://127.0.0.1:5000/tourneys/{}/comments'.format(tourney2_id)
post_comment1 = requests.post(comment1_url, json={'message': 'Hopefully we get it offline next year.'}, headers=header_coolname123)
post_comment2 = requests.post(comment1_url, json={'message': 'Gosh darn the current situation.'}, headers=header_wandmaster)
post_comment3 = requests.post(comment2_url, json={'message': "I'm winning this one!"}, headers=header_wandmaster)
#remove_comment = requests.delete(comment1_url, json="{'message': 'Hopefully we get it offline next year.'}", headers=header_coolname123)
get_comments_url = 'http://127.0.0.1:5000/tourneys/{}/comments'.format(tourney1_id)
get_comments = requests.get(get_comments_url)

get_all_tourneys2 = requests.get('http://127.0.0.1:5000/tourneys')
get_participants1_url = "http://127.0.0.1:5000/tourneys/{}/participants".format(tourney1_id)
get_participants1 = requests.get(get_participants1_url)
get_registered_tourneys = requests.get('http://127.0.0.1:5000/users/coolname123/tourneys')

get_tourney_and_game_all = requests.get('http://127.0.0.1:5000/tourneys/game')
get_tourney_and_game_single_url = "http://127.0.0.1:5000/tourneys/{}/game".format(tourney1_id)
get_tourney_and_game_single = requests.get(get_tourney_and_game_single_url)

print(add_tourney1.text, add_tourney1.status_code)
#print(add_tourney1_copy.text, add_tourney1_copy.status_code)
print(add_tourney2.text, add_tourney2.status_code)
#print(register1.text, register1.status_code)
#print(register2.text, register2.status_code)
#print(register3.text, register3.status_code)
#print(get_all_tourneys1.text, get_all_tourneys1.status_code)
#print(get_tourney.text, get_tourney.status_code)
#print(delete_tourney.text, delete_tourney.status_code)
#print(delete_participant.text, delete_participant.status_code)
#print(search_tourney1.text, search_tourney1.status_code)
#print(sort_tourney.text, sort_tourney.status_code)
#print(edit_tourney.text, edit_tourney.status_code)
#print(like1_copy.text, like1_copy.status_code)
#print(unlike.text, unlike.status_code)
#print(get_likes.text, get_likes.status_code)
#print(post_comment1_copy.text, post_comment1_copy.status_code)
#print(remove_comment.text, remove_comment.status_code)
#print(get_comments.text, get_comments.status_code)
#print(get_all_tourneys2.text, get_all_tourneys2.status_code)
#print(get_participants1.text, get_participants1.status_code)
#print(get_registered_tourneys.text, get_registered_tourneys.status_code)

print(get_tourney_and_game_all.text, get_tourney_and_game_all.status_code)
print(get_tourney_and_game_single.text, get_tourney_and_game_single.status_code)