import os
import tempfile
import json
import pytest

from app import app, db


@pytest.fixture
def client():
    """
    Configures the application for testing and initializes database,
    """
    db_fd, app.config['DATABASE'] = tempfile.mkstemp()
    app.config['TESTING'] = True
    client = app.test_client()

    with app.app_context():
        db.drop_all()
        db.create_all()

    yield client

    os.close(db_fd)
    os.unlink(app.config['DATABASE'])


def test_empty_db(client):
    """
    Tests a request to the index page.
    """
    rv = client.get('/')
    assert b'Hidden Boss' in rv.data


def test_login(client):
    """
    Tests login functionality.
    """
    register = client.post('http://127.0.0.1:5000/users',
                           json={'username': 'Dante',
                                 'password': 'gilgamesh'})
    login = client.post('http://127.0.0.1:5000/users/login', json={'username': 'Dante',
                                                                   'password': 'gilgamesh'})
    assert register.status_code == 200
    assert login.status_code == 200


def test_logout(client):
    """
    Tests logout functionality.
    """
    user = {'username': 'Dante', 'password': 'gilgamesh'}

    register = client.post('http://127.0.0.1:5000/users', json=user)
    login = client.post('http://127.0.0.1:5000/users/login', json=user)

    token = login.json['token']
    header = {'Authorization': 'Bearer ' + token}
    logout = client.post('http://127.0.0.1:5000/users/logout', headers=header)

    assert register.status_code == 200
    assert login.status_code == 200
    assert logout.status_code == 200


def test_server(client):
    """
    Tests various different server functionalities for games, tourneys and users.
    """
    database_url = 'http://127.0.0.1:5000'  # Every other url will build upon this url

    # USER

    user1 = {'username': 'coolname123', 'password': 'coolerpassword321'}
    user2 = {'username': 'wandmaster', 'password': 'alohomora'}
    user3 = {'username': 'admin', 'password': 'masterofuniverse'}

    add_user_url = database_url + '/users'
    add_user1 = client.post(add_user_url, json=user1)
    add_user2 = client.post(add_user_url, json=user2)
    add_user3 = client.post(add_user_url, json=user3)

    # Asserts that the users are added successfully
    assert add_user1.status_code == 200
    assert add_user2.status_code == 200
    assert add_user3.status_code == 200

    assert b'Successfully added user' in add_user1.data
    assert b'Successfully added user' in add_user1.data
    assert b'Successfully added user' in add_user1.data

    login_url = database_url + '/users/login'
    login1 = client.post(login_url, json=user1)
    login2 = client.post(login_url, json=user2)
    login3 = client.post(login_url, json=user3)

    # Asserts that the users are logged in successfully
    assert login1.status_code == 200
    assert login2.status_code == 200
    assert login3.status_code == 200

    assert b'token' in login1.data
    assert b'token' in login2.data
    assert b'token' in login3.data

    # The access tokens from logging in with the users
    token1 = login1.json['token']
    token2 = login2.json['token']
    token3 = login3.json['token']

    # Create headers using the access tokens. These will be used in requests that require user authentication.
    header_coolname123 = {'Authorization': 'Bearer ' + token1}
    header_wandmaster = {'Authorization': 'Bearer ' + token2}
    header_admin = {'Authorization': 'Bearer ' + token3}

    # Assorted requests related to users
    get_all_users = client.get('http://127.0.0.1:5000/users')
    search_users = client.get('http://127.0.0.1:5000/users/search/wand')
    logout = client.post('http://127.0.0.1:5000/users/logout', headers=header_wandmaster)
    delete_user = client.delete('http://127.0.0.1:5000/users/wandmaster', headers=header_admin)

    # Assert that the assorted requests are successful
    assert get_all_users.status_code == 200
    assert search_users.status_code == 200
    assert logout.status_code == 200
    assert delete_user.status_code == 200

    assert b'coolname123' == str.encode(json.loads(get_all_users.data)['users'][0]['username'])
    assert b'wandmaster' == str.encode(json.loads(search_users.data)['users'][0]['username'])
    assert b'Access token revoked' in logout.data
    assert b'User has been deleted' in delete_user.data

    game1 = {'id': 'drks3', 'img': 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1swv.jpg',
             'title': 'Darkstalkers 3', 'release_year': 1997, 'publisher': 'Capcom',
             'platforms': ['Arcade', 'Playstation 1', 'Sega Saturn'], 'alt_titles': ['Vampire Savior']}
    game2 = {'id': 'ssb64', 'title': 'Super Smash Brothers', 'release_year': 1999, 'publisher': 'Nintendo',
             'platforms': ['Nintendo 64']}
    game3 = {'id': 'umvc3', 'title': 'Ultimate Marvel vs Capcom 3', 'release_year': 2011, 'publisher': 'Capcom'}

    add_game1 = client.post('http://127.0.0.1:5000/games', json=game1, headers=header_admin)
    add_game2 = client.post('http://127.0.0.1:5000/games', json=game2, headers=header_admin)
    add_game3 = client.post('http://127.0.0.1:5000/games', json=game3, headers=header_admin)

    # Assert that games are added successfully
    assert add_game1.status_code == 200
    assert add_game2.status_code == 200
    assert add_game3.status_code == 200

    # Assorted requests related to games
    edit_game = client.post('http://127.0.0.1:5000/games/ssb64/edit',
                            json={'title': 'Super Crash Bros.', 'release_year': 2112}, headers=header_admin)
    delete_game = client.delete('http://127.0.0.1:5000/games/drks3', headers=header_admin)
    retrieve_games = client.get('http://127.0.0.1:5000/games')
    retrieve_single_game = client.get('http://127.0.0.1:5000/games/ssb64')
    search_games = client.get('http://127.0.0.1:5000/games/search/title/marvel')
    sort_games = client.get('http://127.0.0.1:5000/games/sort/asc/release_year')

    # Assert that the assorted requests are successful
    assert edit_game.status_code == 200
    assert delete_game.status_code == 200
    assert retrieve_games.status_code == 200
    assert retrieve_single_game.status_code == 200
    assert search_games.status_code == 200
    assert sort_games.status_code == 200

    tourney = {'start_year': 2020, 'start_month': 6, 'start_day': 31, 'start_time': '18:30',
               'title': 'EVO 2020: Quarantine Edition', 'game_id': 'umvc3', 'location': 'Las Vegas, Nevada, US',
               'end_year': 2020, 'end_month': 8, 'end_day': 2, 'end_time': '12:00',
               'description': 'The biggest, most hype and most prestigious fighting game tournament in the world!'}

    add_tourney = client.post('http://127.0.0.1:5000/tourneys', json=tourney, headers=header_coolname123)
    get_all_tourneys = client.get('http://127.0.0.1:5000/tourneys')

    # Assert that the tourney is added and retrieved successfully
    assert add_tourney.status_code == 200
    assert get_all_tourneys.status_code == 200
