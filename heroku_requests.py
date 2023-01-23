# This program sends requests to the Heroku server to supply it with sample data that can be used in the app.

import requests

database_url = 'https://hidden-boss-server.herokuapp.com'  # Every other url will build upon this url

# USERS

# JSON data for several users
user1 = {'username': 'PotatoesAreYum7', 'password': 'falconpuncher'}
user2 = {'username': 'TastySteve', 'password': 'koreanbd'}
user3 = {'username': 'JWong', 'password': 'curlehmustache'}
user4 = {'username': 'admin', 'password': 'masterofuniverse'}

# Add users to the server
add_user_url = database_url + "/users"
add_user1 = requests.post(add_user_url, json=user1)
add_user2 = requests.post(add_user_url, json=user2)
add_user3 = requests.post(add_user_url, json=user3)
add_user4 = requests.post(add_user_url, json=user4)

# Log in with the users
login_url = database_url + '/users/login'
login1 = requests.post(login_url, json=user1)
login2 = requests.post(login_url, json=user2)
login3 = requests.post(login_url, json=user3)
login4 = requests.post(login_url, json=user4)

# The access tokens from logging in with the users
token1 = login1.json()['token']
token2 = login2.json()['token']
token3 = login3.json()['token']
token4 = login4.json()['token']

# Create headers using the access tokens. These will be used in requests that require user authentication.
header_potatoes = {'Authorization': 'Bearer ' + token1}
header_steve = {'Authorization': 'Bearer ' + token2}
header_jwong = {'Authorization': 'Bearer ' + token3}
header_admin = {'Authorization': 'Bearer ' + token4}

print(add_user1.text, add_user1.status_code)
print(add_user2.text, add_user2.status_code)
print(add_user3.text, add_user3.status_code)
print(add_user4.text, add_user4.status_code)
print('login1', login1.status_code)
print('login2', login2.status_code)
print('login3', login3.status_code)
print('login4', login4.status_code)

# JSON data for several games
bbtag_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1n8d.jpg', 'id': 'bbtag',
              'title': 'BlazBlue: Cross Tag Battle', 'release_year': 2018, 'release_month': 5,
              'release_day': 31, 'publisher': 'Arc System Works', 'platforms': ['Windows', 'PlayStation 4', 'Arcade', 'Nintendo Switch']}
drks3_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1swv.jpg', 'id': 'drks3',
              'title': 'Darkstalkers 3', 'release_year': 1997, 'publisher': 'Capcom',
              'platforms': ['Arcade', 'Playstation 1', 'Sega Saturn'], 'alt_titles': ['Vampire Savior']}
umvc3_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1yej.jpg', 'id': 'umvc3',
              'title': 'Ultimate Marvel vs Capcom 3', 'release_year': 2011, 'publisher': 'Capcom',
              'platforms': ['Xbox 360', 'Xbox One', 'Windows', 'PlayStation 4', 'PlayStation 3']}
sf3rs_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1w4w.jpg', 'id': 'sf3rs',
              'title': 'Street Fighter III: 3rd Strike', 'release_year': 1999, 'release_month': 5, 'publisher':'Capcom',
              'platforms': ['PlayStation 2', 'Dreamcast', 'Arcade', 'Xbox']}
strfv_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1pka.jpg', 'id': 'strfv',
              'title': 'Street Fighter V', 'release_year': 2016, 'publisher': 'Capcom', 'platforms': ['Windows', 'PlayStation 4']}
mbaac_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1pqo.jpg', 'id': 'mbaac',
              'title': 'Melty Blood Actress Again Current Code', 'release_year': 2016, 'publisher': 'French Bread',
              'platforms': ['Windows']}
ggxrd_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tqv.jpg', 'id': 'ggxrd',
              'title': 'Guilty Gear Xrd Rev 2', 'release_year': 2017, 'publisher': 'Arc System Works',
              'platforms': ['Windows', 'PlayStation 4', 'PlayStation 3']}
ggacr_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1twa.jpg', 'id': 'ggacr',
              'title': 'GUILTY GEAR XX ACCENT CORE PLUS R', 'release_year': 2015, 'publisher': 'Arc System Works',
              'platforms': ['Windows', 'Nintendo Switch', 'Playstation 3', 'Xbox 360']}
bbcfi_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co232h.jpg', 'id': 'bbcfi',
              'title': 'BlazBlue: Central Fiction', 'release_year': 2015, 'publisher': 'Arc System Works',
              'platforms': ['Windows', 'PlayStation 4', 'Arcade', 'PlayStation 3']}
usfiv_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1w4s.jpg', 'id': 'usfiv',
              'title': 'Ultra Street Fighter IV', 'release_year': 2014, 'publisher': 'Capcom',
              'platforms': ['Xbox 360', 'Windows', 'PlayStation 4', 'Arcade', 'PlayStation 3']}
drbfz_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1nih.jpg', 'id': 'drbfz',
              'title': 'Dragon Ball FighterZ', 'release_year': 2018, 'publisher': 'Arc System Works',
              'platforms': ['Xbox One', 'Windows', 'PlayStation 4', 'Nintendo Switch']}
tekk7_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1w4f.jpg', 'id': 'tekk7',
              'title': 'Tekken 7', 'release_year': 2015, 'publisher': 'Bandai Namco Studios',
              'platforms': ['Xbox One', 'Windows', 'PlayStation 4', 'Arcade']}
socvi_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1x6j.jpg', 'id': 'socvi',
              'title': 'SoulCalibur VI', 'release_year': 2018, 'publisher': 'Bandai Namco Studios',
              'platforms': ['Xbox One', 'Windows', 'PlayStation 4']}
unclr_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co25f8.jpg', 'id': 'unclr',
              'title': 'Under Night In-Birth Exe:Late[cl-r]', 'release_year': 2020, 'publisher': 'French Bread',
              'platforms': ['Windows', 'PlayStation 4', 'Nintendo Switch']}
mvsci_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1wqa.jpg', 'id': 'mvsci',
              'title': 'Marvel vs. Capcom: Infinite', 'release_year': 2017, 'publisher': 'Capcom',
              'platforms': ['Xbox One', 'Windows', 'PlayStation 4']}
kfxiv_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1uf2.jpg', 'id': 'kfxiv',
              'title': 'The King of Fighters XIV', 'release_year': 2016, 'publisher': 'SNK Corporation',
              'platforms': ['Windows', 'PlayStation 4']}
smsho_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1m9a.jpg', 'id': 'smsho',
              'title': 'SAMURAI SHODOWN (2019)', 'release_year': 2019, 'publisher': 'SNK Corporation',
              'platforms': ['Xbox One', 'Windows', 'PlayStation 4', 'Arcade', 'Nintendo Switch']}
melee_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co21yv.jpg', 'id': 'melee',
              'title': 'Super Smash Bros. Melee', 'release_year': 2001, 'publisher': 'Nintendo',
              'platforms': ['Nintendo GameCube']}
ssbul_json = {'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2255.jpg', 'id': 'ssbul',
              'title': 'Super Smash Bros. Ultimate', 'release_year': 2018, 'publisher': 'Nintendo',
              'platforms': ['Nintendo Switch']}

# Add games to the server
add_game_url = database_url + "/games"
add_game1 = requests.post(add_game_url, json=bbtag_json, headers=header_admin)
add_game2 = requests.post(add_game_url, json=drks3_json, headers=header_admin)
add_game3 = requests.post(add_game_url, json=umvc3_json, headers=header_admin)
add_game4 = requests.post(add_game_url, json=sf3rs_json, headers=header_admin)
add_game5 = requests.post(add_game_url, json=strfv_json, headers=header_admin)
add_game6 = requests.post(add_game_url, json=mbaac_json, headers=header_admin)
add_game7 = requests.post(add_game_url, json=ggxrd_json, headers=header_admin)
add_game8 = requests.post(add_game_url, json=ggacr_json, headers=header_admin)
add_game9 = requests.post(add_game_url, json=bbcfi_json, headers=header_admin)
add_game10 = requests.post(add_game_url, json=usfiv_json, headers=header_admin)
add_game11 = requests.post(add_game_url, json=drbfz_json, headers=header_admin)
add_game12 = requests.post(add_game_url, json=tekk7_json, headers=header_admin)
add_game13 = requests.post(add_game_url, json=socvi_json, headers=header_admin)
add_game14 = requests.post(add_game_url, json=unclr_json, headers=header_admin)
add_game15 = requests.post(add_game_url, json=mvsci_json, headers=header_admin)
add_game16 = requests.post(add_game_url, json=kfxiv_json, headers=header_admin)
add_game17 = requests.post(add_game_url, json=smsho_json, headers=header_admin)
add_game18 = requests.post(add_game_url, json=melee_json, headers=header_admin)
add_game19 = requests.post(add_game_url, json=ssbul_json, headers=header_admin)

print(add_game1.text, add_game1.status_code)
print(add_game2.text, add_game2.status_code)
print(add_game3.text, add_game3.status_code)
print(add_game4.text, add_game4.status_code)
print(add_game5.text, add_game5.status_code)
print(add_game6.text, add_game6.status_code)
print(add_game7.text, add_game7.status_code)
print(add_game8.text, add_game8.status_code)
print(add_game9.text, add_game9.status_code)
print(add_game10.text, add_game10.status_code)
print(add_game11.text, add_game11.status_code)
print(add_game12.text, add_game12.status_code)
print(add_game13.text, add_game13.status_code)
print(add_game14.text, add_game14.status_code)
print(add_game15.text, add_game15.status_code)
print(add_game16.text, add_game16.status_code)
print(add_game17.text, add_game17.status_code)
print(add_game18.text, add_game18.status_code)
print(add_game19.text, add_game19.status_code)


# JSON data for several tourneys
tourney1 = {'start_year': 2020, 'start_month': 10, 'start_day': 31, 'start_time': '18:30',
            'title': 'EVO 2020: Quarantine Edition', 'game_id': 'bbtag', 'location': 'Las Vegas, Nevada, US',
            'end_year': 2020, 'end_month': 11, 'end_day': 2, 'end_time': '12:00',
            'description': 'The biggest, most hype and most prestigious fighting game tournament in the world!'}
tourney2 = {'start_year': 2021, 'start_month': 4, 'start_day': 20, 'start_time': '13:00',
            'end_year': 2021, 'end_month': 4, 'end_day': 20, 'end_time': '22:00',
            'title': 'Linkan Overlords', 'game_id': 'drks3',
            'location': 'Linköping, Östergötland, Sweden'}
tourney3 = {'start_year': 2021, 'start_month': 6, 'start_day': 31, 'start_time': '18:30',
            'title': 'EVÖ 2021', 'game_id': 'tekk7', 'location': 'Norrköping, Östergötland, Sweden',
            'end_year': 2021, 'end_month': 7, 'end_day': 4, 'end_time': '12:00',
            'description': 'The biggest, most hype and most prestigious fighting game tournament in Östergötland!'}


# Add tourneys to the server
add_tourney_url = database_url + "/tourneys"
add_tourney1 = requests.post(add_tourney_url, json=tourney1, headers=header_jwong)
add_tourney2 = requests.post(add_tourney_url, json=tourney2, headers=header_steve)
add_tourney3 = requests.post(add_tourney_url, json=tourney3, headers=header_potatoes)

print(add_tourney1.text, add_tourney1.status_code)
print(add_tourney2.text, add_tourney2.status_code)
print(add_tourney3.text, add_tourney3.status_code)

# Retrieve all tourneys so that UUIDs can be gathered from them
get_all_tourneys_url = database_url + '/tourneys'
get_all_tourneys = requests.get(get_all_tourneys_url)

tourneyid_1 = ""
tourneyid_2 = ""
tourneyid_3 = ""

# Get the UUIDs for 2 specific tourneys, so that requests requiring them can be made
if get_all_tourneys.status_code == 200:
    search_results = eval(get_all_tourneys.text)
    for tourney in search_results['tourneys']:
        if tourney['title'] == "EVO 2020: Quarantine Edition":
            tourneyid_1 = tourney['id']
        if tourney['title'] == "Linkan Overlords":
            tourneyid_2 = tourney['id']
        if tourney['title'] == "EVÖ 2021":
            tourneyid_3 = tourney['id']

# Register several users to several tourneys
register_tourney1_url = database_url + '/tourneys/{}/register'.format(tourneyid_1)
register_tourney2_url = database_url + '/tourneys/{}/register'.format(tourneyid_2)

register1 = requests.post(register_tourney1_url, headers=header_steve)
register2 = requests.post(register_tourney1_url, headers=header_jwong)
register3 = requests.post(register_tourney2_url, headers=header_jwong)

print(register1.text, register1.status_code)
print(register2.text, register2.status_code)
print(register3.text, register3.status_code)

# Make several users like several tourneys
like_tourney1_url = database_url + "/tourneys/{}/likes".format(tourneyid_1)
like_tourney2_url = database_url + "/tourneys/{}/likes".format(tourneyid_2)

like1 = requests.post(like_tourney2_url, headers=header_steve)
like2 = requests.post(like_tourney2_url, headers=header_potatoes)
like3 = requests.post(like_tourney1_url, headers=header_potatoes)

print(like1.text, like1.status_code)
print(like2.text, like2.status_code)
print(like3.text, like3.status_code)

# JSON data for several comments
comment1_json = {'message': 'Bracing myself for the lag'}
comment2_json = {'message': 'yo i have wifi is that ok?'}
comment3_json = {'message': 'Got this one in the bag.'}
comment4_json = {'message': "Tekken is a kusoge. Why aren't we playing Virtua Fighter?"}

# Post comments on several tournaments as several users
comment1_url = database_url + '/tourneys/' + tourneyid_1 + '/comments'
comment2_url = database_url + '/tourneys/' + tourneyid_1 + '/comments'
comment3_url = database_url + '/tourneys/' + tourneyid_2 + '/comments'
comment4_url = database_url + '/tourneys/' + tourneyid_3 + '/comments'

comment1 = requests.post(comment1_url, json=comment1_json, headers=header_jwong)
comment2 = requests.post(comment2_url, json=comment2_json, headers=header_potatoes)
comment3 = requests.post(comment3_url, json=comment3_json, headers=header_steve)
comment4 = requests.post(comment4_url, json=comment4_json, headers=header_jwong)

print(comment1.text, comment1.status_code)
print(comment2.text, comment2.status_code)
print(comment3.text, comment3.status_code)
print(comment4.text, comment4.status_code)

logout_url = database_url + '/users/logout'

# Log the users out of the server

logout1 = requests.post(logout_url, headers=header_admin)
logout2 = requests.post(logout_url, headers=header_steve)
logout3 = requests.post(logout_url, headers=header_jwong)
logout4 = requests.post(logout_url, headers=header_potatoes)

print(logout1.text, logout1.status_code)
print(logout2.text, logout1.status_code)
print(logout3.text, logout1.status_code)
print(logout4.text, logout1.status_code)