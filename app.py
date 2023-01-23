# The server itself, contained in a single file
import uuid
import os
import datetime

from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, jwt_required, create_access_token, get_jwt_claims, get_raw_jwt, get_jwt_identity
from sqlalchemy import DateTime, asc

db_path = os.path.join(os.path.dirname(__file__), 'app.db')
db_uri = 'sqlite:///{}'.format(db_path)
debug_flag = True

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
app.config['JSON_AS_ASCII'] = False
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)
app.config['JWT_SECRET_KEY'] = os.urandom(20)
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(minutes=300)
app.config['JWT_BLACKLIST_ENABLED'] = True
app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access']
app.config['SQLALCHEMY_ENGINE_OPTIONS'] = {"po  ol_pre_ping": True}

platforms_table = db.Table('platforms', db.Column('plat_id', db.String(50), db.ForeignKey('platform.id'), primary_key=True, nullable=False, unique=False),
                    db.Column('game_id', db.String(200), db.ForeignKey('game.id'), primary_key=True, nullable=False, unique=False))
alt_titles_table = db.Table('alternative_titles', db.Column('title_id', db.String(50), db.ForeignKey('alt_title.name'), primary_key=True, nullable=False),
                    db.Column('game_id', db.String(200), db.ForeignKey('game.id'), primary_key=True, nullable=False))
participants_table = db.Table('participants', db.Column('tourney_id', db.String(36), db.ForeignKey('tourney.id'), primary_key=True, nullable=False),
                      db.Column('usr_id', db.String(80), db.ForeignKey('user.username'), primary_key=True, nullable=False))
liked_by_table = db.Table('liked_by', db.Column('tourney_id', db.String(36), db.ForeignKey('tourney.id'), primary_key=True, nullable=False),
                      db.Column('usr_id', db.String(80), db.ForeignKey('user.username'), primary_key=True, nullable=False))
comments_table = db.Table('tourney_comments', db.Column('tourney_id', db.String(36), db.ForeignKey('tourney.id'), primary_key=True, nullable=False),
                          db.Column('comment_id', db.String(36), db.ForeignKey('comment.id'), primary_key=True, nullable=False))
hosted_table = db.Table('tourney_hosts', db.Column('tourney_id', db.String(36), db.ForeignKey('tourney.id'), primary_key=True, nullable=False),
                        db.Column('usr_id', db.String(80), db.ForeignKey('user.username'), primary_key=True, nullable=False))

@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    token_jti = decrypted_token['jti']
    blacklist = db.session.query(Blacklist).filter_by(jti=token_jti).all()
    for token in blacklist:
        if token_jti == token.jti:
            return True
    return False

@jwt.invalid_token_loader
def invalid_token_error(error):
    return jsonify({'status': 401, 'msg': 'The token is invalid'}), 401


class Blacklist(db.Model):
    """
    A blacklisted access token.
    Attributes:
        jti - the blacklisted access token's unique identifier
    """
    jti = db.Column(db.String(36), nullable=False, primary_key=True)

    def __init__(self, jti):
        self.jti = jti

    def __repr__(self):
        return self.jti


class Comment(db.Model):
    """
    A comment by a user on a tourney.
    Attributes:
        id - unique identifier
        message - comment content
        user - user who made the comment
        tourney_commented - tourney the comment was posted on
    """
    __tablename__ = "comment"
    id = db.Column(db.String(36), unique=True, nullable=False, primary_key=True)
    message = db.Column(db.String(180), unique=False, nullable=False)
    user = db.Column(db.String(30), nullable=False)
    timestamp = db.Column(DateTime, nullable=False)
    tourney_commented = db.relationship('Tourney', secondary=comments_table, back_populates="comments")

    def __init__(self, user, message, timestamp):
        self.id = get_unique_id('Comment')
        self.user = user
        self.message = message
        self.timestamp = timestamp

    def __repr__(self):
        return str(self.timestamp) + ": " + self.message + " - " + self.user


class Platform(db.Model):
    """
    A gaming platform for a game.
    Attributes:
        name - name of the platform
        games - Game object the platform is connected to
    """
    name = db.Column(db.String(50), unique=False, nullable=False)
    games = db.relationship('Game', secondary=platforms_table, back_populates="platforms")
    id = db.Column(db.Integer, primary_key=True)
    def __repr__(self):
        return self.name


class AltTitle(db.Model):
    """
    Alternative titles for a game. This could, for example, be the title of a game within a particular region.
    Attributes:
        name - name of the alternative title
        games - Game object the alternative title is connected to
    """
    __tablename__ = "alt_title"
    name = db.Column(db.String(50), unique=False, nullable=False, primary_key=True)
    games = db.relationship('Game', secondary=alt_titles_table, back_populates="alt_titles")

    def __repr__(self):
        return self.name


class User(db.Model):
    """
    A user of the HiddenBoss app.
    Attributes:
        username - user's username
        password_hash - user's password after being salted and hashed
        password_salt - user's unique password salt
        tourneys - tourneys which user has registered for or participated in
        liked_tourneys - tourneys the user has liked
        hosted_tourneys - tourney the user has created or hosted
    """
    username = db.Column(db.String(30), primary_key=True, unique=True, nullable=False)
    password_hash = db.Column(db.String(100), nullable=False)
    password_salt = db.Column(db.String(36), unique=True, nullable=False)
    tourneys = db.relationship('Tourney', secondary=participants_table, back_populates="participants")
    liked_tourneys = db.relationship('Tourney', secondary=liked_by_table, back_populates="liked_by")
    hosted_tourneys = db.relationship('Tourney', secondary=hosted_table, back_populates="tourney_hosts")

    def __init__(self, username, password):
        self.username = username
        self.password_salt = get_unique_id("User")
        salted_password = self.password_salt + str(password)
        self.password_hash = bcrypt.generate_password_hash(salted_password).decode('utf-8')

    def __repr__(self):
        return self.username


class Game(db.Model):
    """
    A video game.
    Attributes:
        id - game's unique id. not random
        title - game's title
        release_year/month/day - date of game's original release
        publisher - the company that published the given game
        img - URL to an image of the game's cover art
        platforms - platforms the game is available on. can be several, or none at all.
        alt_titles - game's alternative titles. can be several, or none at all.
    """
    id = db.Column(db.String(5), nullable=False, unique=True, primary_key=True)
    title = db.Column(db.String(200), unique=True, nullable=False)
    release_year = db.Column(db.Integer(), nullable=False, unique=False)
    release_month = db.Column(db.Integer(), nullable=True, unique=False)
    release_day = db.Column(db.Integer(), nullable=True, unique=False)
    publisher = db.Column(db.String(50), nullable=True, unique=False)
    img = db.Column(db.String(200), nullable=True, unique=False)
    platforms = db.relationship("Platform", secondary=platforms_table, back_populates="games")
    alt_titles = db.relationship("AltTitle", secondary=alt_titles_table, back_populates="games")

    def __init__(self, id, title, release_year, release_month, release_day, publisher, img):
        self.id = id
        self.title = title
        self.release_year = release_year
        self.release_month = release_month
        self.release_day = release_day
        self.publisher = publisher
        self.img = img

    def __repr__(self):
        return self.title

class Tourney(db.Model):
    """
    A tournament for a particular video game.
    Attributes:
        start_year/month/day/time - time and date of tournament start
        end_year/month/day/time - time and date of tournament end
        host_id - user who created and is hosting the tournament
        id - the tourney's unique identifier
        title - name of the tournament
        game_id - ID of the game being played
        description - description of the tournament
        location - the location the tournament is being held at
    """
    start_year = db.Column(db.Integer(), nullable=False)
    start_month = db.Column(db.Integer(), nullable=False)
    start_day = db.Column(db.Integer(), nullable=False)
    start_time = db.Column(db.String(5), nullable=False)
    end_year = db.Column(db.Integer(), nullable=True)
    end_month = db.Column(db.Integer(), nullable=True)
    end_day = db.Column(db.Integer(), nullable=True)
    end_time = db.Column(db.String(5), nullable=True)

    host_id = db.Column(db.String(30), nullable=False)
    id = db.Column(db.String(150), primary_key=True, unique=True)
    title = db.Column(db.String(100), nullable=False)
    game_id = db.Column(db.String(5), nullable=False)
    description = db.Column(db.String(1000), nullable=True)
    location = db.Column(db.String(200), nullable=False)

    participants = db.relationship('User', secondary=participants_table, back_populates='tourneys')
    liked_by = db.relationship('User', secondary=liked_by_table, back_populates="liked_tourneys")
    comments = db.relationship('Comment', secondary=comments_table, back_populates="tourney_commented")
    tourney_hosts = db.relationship('User', secondary=hosted_table, back_populates="hosted_tourneys")

    def __init__(self, start_year, start_month, start_day, start_time, host_id, title, game_id, location,
                 end_year, end_month, end_day, end_time, description):
        self.start_year = start_year
        self.start_month = start_month
        self.start_day = start_day
        self.start_time = start_time
        self.host_id = host_id
        self.id = get_unique_id('Tourney')
        self.title = title
        self.game_id = game_id
        self.location = location
        self.end_year = end_year
        self.end_month = end_month
        self.end_day = end_day
        self.end_time = end_time
        self.description = description


def get_unique_id(table_name):
    """
    Obtain a UUID4 that's unique for a particular database table.
    :param table_name: the name of table that the UUID4 will be checked against
    """
    id = str(uuid.uuid4())
    search_string = "{}.query.all()".format(table_name)
    results = exec(search_string)
    # If the given ID exists in one of the table's records, give ID a new value and continue looping until ID is unique
    if results:
        while id in results:
            id = str(uuid.uuid4())

    return id


def sorted_games(games_list, condition, direction):
    """
    Sorts an array of Game objects by a condition in ascending or descending order.
    :param games_list: The array of Game objects
    :param condition: The condition that games_list is sorted by
    :param direction: Sort direction. Either 'asc' for ascending or 'des' for descending
    """
    #The list is sorted by 'key', which in this case is one of Game's attributes
    if direction == 'asc':
        sorted_list = sorted(games_list, key=lambda i: i[condition])
    elif direction == 'des':
        sorted_list = sorted(games_list, key=lambda i: i[condition], reverse=True)

    return sorted_list


def sorted_tourneys(tourney_list, condition, direction):
    """
    Sorts an array of Tourney objects by a condition in a given order.
    :param tourney_list: The array of Tourney objects
    :param condition: The condition that games_list is sorted by
    :param direction: Sort direction. Either 'asc' for ascending or 'des' for descending
    """
    if direction == 'asc':
        sorted_list = sorted(tourney_list, key= lambda i: i[condition])
    elif direction == 'des':
        sorted_list = sorted(tourney_list, key= lambda i: i[condition], reverse=True)

    return sorted_list


def comment_to_dict(comment):
    """
    Converts a Comment object into a jsonify-able dictionary for request response.
    :param comment: Comment object to be turned into dictionary
    """
    comment_dict = {'id': comment.id, 'message': comment.message, 'user': comment.user, 'timestamp': comment.timestamp}
    return comment_dict


def game_to_dict(game):
    """
    Converts a Game object into a jsonify-able dictionary for request response.
    :param game: Game object to be turned into dictionary
    """
    #Searches for all of game's platforms and alternative titles and puts them all into their own arrays
    search_platforms = db.session.query(Platform).join(Platform.games).filter_by(id=game.id).all()
    if search_platforms:
        game_platforms = [str(element) for element in search_platforms]
    else:
        game_platforms = []

    search_titles = db.session.query(AltTitle).join(AltTitle.games).filter_by(id=game.id).all()
    if search_titles:
        game_titles = [str(element) for element in search_titles]
    else:
        game_titles = []

    game_dict = {'id': game.id, 'title': game.title, 'release_year': game.release_year, 'release_month':
        game.release_month, 'release_day': game.release_day, 'publisher': game.publisher,
                 'img': game.img, 'platforms':game_platforms, 'alt_titles': game_titles}
    return game_dict


def user_to_dict(user):
    """
    Converts a User object into a jsonify-able dictionary for request response.
    :param user: User object to be turned into dictionary
    """
    user_dict = {'username': user.username}
    return user_dict


def tourney_to_dict(tourney):
    """
    Converts a Tourney object into a jsonify-able dictionary for request response.
    :param tourney: Tourney object to be turned into dictionary
    """
    #Searches for all of tourney's participants, likes and comments, and puts them all into their own arrays
    tourney_participants = db.session.query(User).join(User.tourneys).filter_by(id=tourney.id).all()
    if tourney_participants:
        tourney_participants = [str(element) for element in tourney_participants]
    else:
        tourney_participants = []

    tourney_likes = db.session.query(User).join(User.liked_tourneys).filter_by(id=tourney.id).all()
    if tourney_likes:
        tourney_likes = [str(element) for element in tourney_likes]
    else:
        tourney_likes = []

    search_comments = db.session.query(Comment).join(Comment.tourney_commented).filter_by(id=tourney.id).all()
    if search_comments:
        search_comments = [str(element) for element in search_comments]
    else:
        search_comments = []

    tourney_dict = {'start_year': tourney.start_year, 'start_month': tourney.start_month, 'start_day': tourney.start_day, 'start_time': tourney.start_time, 'id': tourney.id, 'host_id': tourney.host_id, 'title': tourney.title, 'game_id': tourney.game_id, 'location': tourney.location, 'end_year': tourney.end_year, 'end_month': tourney.end_month, 'end_day': tourney.end_day, 'end_time': tourney.end_time, 'description': tourney.description, 'participants': tourney_participants, 'liked_by': tourney_likes, 'comments': search_comments}
    return tourney_dict


def add_platforms(game, platforms):
    """
    Add platforms to a Game object.
    :param game: Game object
    :param platforms: Array of strings of platform names
    """
    for platform in platforms:
        platform_exists = db.session.query(Platform).filter_by(name=platform).first()
        if platform_exists:
            game.platforms.append(platform_exists)
        else:
            new_platform = Platform(name=platform)
            game.platforms.append(new_platform)
    return "Platforms added"


def add_alternative_titles(game, titles):
    """
    Add alternative titles to a Game object.
    :param game: Game object
    :param titles: Array of strings of alternative titles
    """
    for title in titles:
        current_title = AltTitle(name=title)
        game.alt_titles.append(current_title)
    return "Titles added"


@app.route('/')
def home_page():
    """
    Display a simple message for the database's index page.
    """
    return 'Hidden Boss'


@app.route('/check_token', methods=['POST'])
@jwt_required
def check_token():
    """
    Checks if an access token is valid. Automatically does this with jwt_required.
    """
    return 'Token is valid!'

#
# TOURNEYS
#


def tourney_and_game_dict(tourney):
    """
    Returns a tourney with additional information, including tourney participants, user that have liked the
    tourney, comments posted on the tourney and detailed information regarding the tournament game.
    :param tourney: : Tourney object to be returned
    """
    tourney_participants = db.session.query(User).join(User.tourneys).filter_by(id=tourney.id).all()
    tourney_likes = db.session.query(User).join(User.liked_tourneys).filter_by(id=tourney.id).all()
    search_comments = db.session.query(Comment).join(Comment.tourney_commented).filter_by(id=tourney.id).all()
    #Checks if there are any registered tournament participants, likes or comments.
    if tourney_participants:
        tourney_participants = [str(element) for element in tourney_participants]
    else:
        tourney_participants = []
    if tourney_likes:
        tourney_likes = [str(element) for element in tourney_likes]
    else:
        tourney_likes = []
    if search_comments:
        search_comments = [str(element) for element in search_comments]
    else:
        search_comments = []

    tourney_game = Game.query.filter_by(id=tourney.game_id).first()
    game_dict = game_to_dict(tourney_game)
    tourney_dict = {'start_year': tourney.start_year, 'start_month': tourney.start_month,
                    'start_day': tourney.start_day, 'start_time': tourney.start_time, 'id': tourney.id,
                    'host_id': tourney.host_id, 'title': tourney.title, 'game_id': tourney.game_id,
                    'location': tourney.location, 'end_year': tourney.end_year, 'end_month': tourney.end_month,
                    'end_day': tourney.end_day, 'end_time': tourney.end_time, 'description': tourney.description,
                    'participants': tourney_participants, 'liked_by': tourney_likes, 'comments': search_comments, 'game': game_dict}
    return tourney_dict


@app.route('/tourneys/game', methods=['GET'])
def tourney_and_game_all():
    """
    Retrieves all tourneys in server and returns them as a list of dictionaries (one for every tourney),
    with additional information compared to get_all_tourneys.
    """
    tourneys = Tourney.query.all()
    if tourneys:
        tourney_list = [tourney_and_game_dict(tourney) for tourney in tourneys]
        return jsonify({'tourneys': tourney_list})
    return "No tourneys in DB", 404


@app.route('/tourneys/<tourney_id>/game', methods=['GET'])
def tourney_and_game_single(tourney_id):
    """
    Retrieves a single tourney from the server and returns it as a dictionary,
    with additional information compared to get_tourney.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = Tourney.query.filter_by(id=tourney_id).first()
    if tourney:
            return jsonify({'tourney': tourney_and_game_dict(tourney)})
    return "tourney not found in DB", 400


@app.route('/tourneys', methods=['POST'])
@jwt_required
def create_tourney():
    """
    Creates a Tourney object from JSON data sent with request, and adds it to the server.
    """
    if request.json:
        json_dict = request.json

        essential_data = ['start_year', 'start_month', 'start_day', 'start_time', 'title', 'location', 'game_id']
        for row in essential_data:
            if row not in json_dict:
                return "Request JSON missing essential data", 401
        if len(json_dict['start_time']) != 5:
            return "Tourney's start time must be exactly 5 characters long", 403
        if len(json_dict['game_id']) != 5:
            return("Game's ID must be exactly 5 characters long", 404)
        tourney_game = Game.query.filter_by(id=json_dict['game_id']).first()
        if not tourney_game:
            return "Game specified in tourney is not in database", 405

        # End time and date aren't required, and thus will be empty if not given
        if 'end_year' not in json_dict:
            json_dict['end_year'] = ""
        if 'end_month' not in json_dict:
            json_dict['end_month'] = ""
        if 'end_day' not in json_dict:
            json_dict['end_day'] = ""
        if 'end_time' not in json_dict:
            json_dict['end_time'] = ""
        if 'description' not in json_dict:
            json_dict['description'] = "No description given."

        tourney = Tourney(json_dict['start_year'],json_dict['start_month'], json_dict['start_day'], json_dict['start_time'],
                          get_jwt_identity(), json_dict['title'], json_dict['game_id'], json_dict['location'],
                          json_dict['end_year'], json_dict['end_month'], json_dict['end_day'], json_dict['end_time'], json_dict['description'])
        db.session.add(tourney)

        tourney_host = User.query.filter_by(username=get_jwt_identity()).first()
        tourney_host.hosted_tourneys.append(tourney)

        db.session.commit()
        return "Tourney was created", 200
    return "No JSON in request", 400


@app.route('/tourneys', methods=['GET'])
def get_all_tourneys():
    """
    Retrieves all tourneys in server and returns them as a list of dictionaries (one for every tourney).
    """
    tourneys = Tourney.query.all()
    if tourneys:
        tourney_list = [tourney_to_dict(tourney) for tourney in tourneys]
        return jsonify({'tourneys': tourney_list})
    return "No tourneys in database", 404


@app.route('/tourneys/<tourney_id>', methods=['GET'])
def get_tourney(tourney_id):
    """
    Retrieves a single tourney from the server and returns it as a dictionary.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    if tourney:
        return jsonify({'tourney': tourney_to_dict(tourney)})
    return "Tourney not found in database", 404


@app.route('/tourneys/<tourney_id>', methods=['DELETE'])
@jwt_required
def remove_tourney(tourney_id):
    """
    Removes a tournament from the server.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney_to_delete = db.session.query(Tourney).filter_by(id=tourney_id).first()
    if get_jwt_identity() != tourney_to_delete.host_id:
        return "User is trying to remove a tourney that isn't theirs", 401
    if tourney_to_delete:
        db.session.delete(tourney_to_delete)
        db.session.commit()
        return "Tourney has been removed", 200
    return "Tourney not found in database", 404


@app.route('/tourneys/<tourney_id>/edit', methods=['POST'])
@jwt_required
def edit_tourney(tourney_id):
    """
    Edit a tourney's information using JSON data. The function will edit whatever attributes that has corresponding
    JSON data in the request, as long as it is a valid attribute to edit.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    if request.json:
        json_dict = request.json

        valid_attributes = ['title', 'game_id', 'description', 'location', 'start_year', 'start_month', 'start_day', 'end_year', 'end_month', 'end_day', 'start_time', 'end_time']
        for attribute in json_dict.keys():
            if attribute not in valid_attributes:
                return "JSON data contains uneditable attributes", 400

        tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
        if tourney:
            if get_jwt_identity() != tourney.host_id:
                return "User is trying to edit a tourney that isn't theirs", 401
            #Since it is impossible to know exactly what attributes will be edited, string formatting is used,
            #with the edit's key as the tourney attribute and the edit's value as the new value of the attribute
            for current_edit in json_dict.keys():
                edit = "tourney.{} = json_dict[current_edit]".format(current_edit)
                exec(edit)
            db.session.commit()
            return "Edited tourney", 200
        return "Tourney not found in database", 404
    return "No JSON in request", 400


@app.route('/tourneys/search/<condition>/<search_term>', methods=['GET'])
def search_tourney(condition, search_term):
    """
    Search for a particular tourney using one of several search conditions.
    :param condition: String of the search condition
    :param search_term: String of the search term
    """
    valid_conditions = ['title', 'start_year', 'host', 'game', 'location']
    if condition not in valid_conditions:
        return "Not a valid search condition", 400

    #If search condition is title or game, the % operator is used to get tourneys even if the search term isn't complete
    if condition == 'title':
        search_string = "%{}%".format(search_term)
        print(search_string)
        search_results = db.session.query(Tourney).filter(Tourney.title.like(search_string)).all()
    elif condition == 'start_year':
        search_results = Tourney.query.filter_by(start_year=search_term).all()
    elif condition == 'host':
        search_results = Tourney.query.filter_by(host_id=search_term).all()
    elif condition == 'game':
        search_string = "%{}%".format(search_term)
        search_results = db.session.query(Tourney).filter(Tourney.game_id.like(search_string)).all()

    if search_results:
        results_list = [tourney_to_dict(tourney) for tourney in search_results]
        return jsonify({'tourneys': results_list})
    return("Search yielded no results", 404)


@app.route('/tourneys/sort/<direction>/<condition>', methods=['GET'])
def sort_tourneys(direction, condition):
    """
    Sorts a list of tourneys after a condition, in either ascending or descending order.
    :param direction: String of the sorting direction
    :param condition: String of the condition to sort by
    """
    json_dict = request.json
    available_conditions = ['title', 'game_id', 'start_year']
    if condition in available_conditions:
        #If a list of tournaments is supplied in the form of JSON data, that list will be sorted. Otherwise, all
        #tourneys in the server will be sorted. This is so sort_tourneys can be used in tandem with search_tourney
        if request.json:
            tourneys_list = json_dict['tourneys']
        else:
            tourneys = db.session.query(Tourney).all()
            tourneys_list = [tourney_to_dict(tourney) for tourney in tourneys]
        if tourneys_list:
            tourneys_list = sorted_tourneys(tourneys_list, condition, direction)
            return jsonify({'tourneys': tourneys_list})
        return ("No tourneys in database", 404)
    return ("Not a viable sorting condition. tourneys can be sorted in order of ID, title, publisher and release date.", 400)


@app.route('/tourneys/<tourney_id>/register', methods=["POST"])
@jwt_required
def register_participant(tourney_id):
    """
    Register user as a participant in the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    participant = db.session.query(User).filter_by(username=get_jwt_identity()).first()
    if tourney:
        if participant:
            if participant in tourney.participants:
                return "User already registered to tournament", 400
            tourney.participants.append(participant)
            db.session.commit()
            return "User registered to tournament", 200
        return("User not found in database", 404)
    return("Tourney not found in database", 404)


@app.route('/tourneys/<tourney_id>/register', methods=['DELETE'])
@jwt_required
def remove_participant(tourney_id):
    """
    Remove user from the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    participant = db.session.query(User).filter_by(username=get_jwt_identity()).first()
    if tourney:
        if participant:
            if participant in tourney.participants:
                tourney.participants = [participant for participant in tourney.participants if participant.username != get_jwt_identity()]
                db.session.commit()
                return "User successfully deregistered from tourney", 200
            return ("User not participant in tourney", 400)
        return ("User not found in database", 404)
    return ("Tourney not found in database", 404)


@app.route('/tourneys/<tourney_id>/participants', methods=['GET'])
def get_participants(tourney_id):
    """
    Retrieve all of a tournament's participants.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = Tourney.query.filter_by(id=tourney_id).first()
    if not tourney:
        return "Tourney not in database!", 400
    tourney_participants = db.session.query(User).join(User.tourneys).filter_by(id=tourney_id).all()
    if tourney_participants:
        tourney_participants = [user_to_dict(participant) for participant in tourney_participants]
        return jsonify({'participants': tourney_participants})
    return "No participants found for the given tourney", 404


@app.route('/tourneys/<tourney_id>/likes', methods=['POST'])
@jwt_required
def like(tourney_id):
    """
    Make a user "like" the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    user = db.session.query(User).filter_by(username=get_jwt_identity()).first()
    if tourney:
        if user:
            if user in tourney.liked_by:
                return "Tourney already liked by user", 400
            tourney.liked_by.append(user)
            db.session.commit()
            return "User liked the tourney", 200
        return("User not found in database", 404)
    return("Tourney not found in database", 404)


@app.route('/tourneys/<tourney_id>/likes', methods=['DELETE'])
@jwt_required
def unlike(tourney_id):
    """
    Removes a user's "like" from the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    user = db.session.query(User).filter_by(username=get_jwt_identity()).first()
    if tourney:
        if user:
            if user in tourney.liked_by:
                #User is removed from the list of likes by remaking the list while excluding the specific user
                tourney.liked_by = [user for user in tourney.liked_by if user.username != get_jwt_identity()]
                db.session.commit()
                return "User unliked tourney", 200
            return ("Tourney isn't liked by user", 400)
        return ("User not found in database", 404)
    return ("Tourney not found in database", 404)


@app.route('/tourneys/<tourney_id>/likes', methods=['GET'])
def get_likes(tourney_id):
    """
    Retrieve all users that have liked the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
    tourney_likes = db.session.query(User).join(User.liked_tourneys).filter_by(id=tourney_id).all()
    if tourney:
        tourney_likes = [user_to_dict(user) for user in tourney_likes]
        return jsonify({'liked_by': tourney_likes})
    return ("Tourney not found in database", 404)


@app.route('/tourneys/<tourney_id>/comments', methods=['POST'])
@jwt_required
def comment(tourney_id):
    """
    Make a user comment on the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    if request.json:
        json_dict = request.json
        if 'message' not in json_dict.keys():
            return "Request JSON missing essential data", 401
        user = get_jwt_identity()
        commenter = User.query.filter_by(username=user).first()
        if not commenter:
            return "Commenter is not a registered user", 402
        comment = Comment.query.filter_by(message=json_dict['message'], user=user).join(Comment.tourney_commented).filter_by(id=tourney_id).first()
        if comment:
            return "Exact same comment by same user already posted", 403
        tourney = db.session.query(Tourney).filter_by(id=tourney_id).first()
        if tourney:
            new_comment = Comment(user, json_dict['message'], datetime.datetime.now())
            db.session.add(new_comment)
            tourney.comments.append(new_comment)
            db.session.commit()
            return("Successfully added comment!", 200)
        return ("Tourney not found in database", 404)
    return ("No JSON in request", 400)


@app.route('/tourneys/<tourney_id>/comments', methods=['DELETE'])
@jwt_required
def remove_comment(tourney_id):
    """
    Remove a user's comment from the given tournament.
    :param tourney_id: Unique ID of the desired tourney (String)
    """
    if request.json:
        json_dict = request.json
        identity = get_jwt_identity()
        user = db.session.query(User).filter_by(username=identity).first()
        if not user:
            return "User not found in database", 404

        tourney = db.session.query(Tourney).filter_by(id =tourney_id).first()
        if not tourney:
            return "Tourney not found in database", 404

        comment = db.session.query(Comment).filter_by(message=json_dict['message'], user=identity).first()
        if comment:
            if comment in tourney.comments:
                tourney.comments = [comment for comment in tourney.comments if (comment.message != json_dict['message'] and comment.user != identity)]
                db.session.commit()
                return "Comment removed from tourney", 200
            return "Comment was not made on this tourney", 400
        return "Comment not found in database", 404
    return "No JSON in request", 400


@app.route('/tourneys/<tourney_id>/comments', methods=['GET'])
def get_comments(tourney_id):
    """
    Retrieve all of the given tournament's comments.
    :param tourney_id:
    """
    tourney = Tourney.query.filter_by(id=tourney_id).first()
    if not tourney:
        return "Tourney not in database!", 400

    comments_in = db.session.query(Comment).join(Comment.tourney_commented).filter_by(id=tourney_id).order_by(asc(Comment.timestamp)).all()
    if comments_in:
        comments_out = [comment_to_dict(comment) for comment in comments_in]
        return jsonify({'comments': comments_out})
    return "Tourney has no comments!", 404

#
# USER
#


@app.route('/users', methods=['POST'])
def register():
    """
    Register a new user.
    :return:
    """
    if request.json:
        json_dict = request.json
        essential_data = ['username', 'password']
        for row in essential_data:
            if row not in json_dict:
                if row == username:
                    return "Request JSON missing essential data", 400
                else:
                    return "Request JSON missing essential data", 404
        if db.session.query(User).filter_by(username=json_dict['username']).first():
            return "Username already in use", 401
        if json_dict['password'] == "":
            return "No password given", 402
        new_user = User(json_dict['username'], json_dict['password'])
        db.session.add(new_user)
        db.session.commit()
        return ("Successfully added user", 200)
    return ("No JSON in request", 403)


@app.route('/users', methods=['GET'])
def get_all_users():
    """
    Retrieve all users in the server.
    """
    users = User.query.all()
    if users:
        user_list = [user_to_dict(user) for user in users]
        return jsonify({'users': user_list})
    return "No users in database", 404

@app.route('/users/<user_id>', methods=['GET'])
def get_user(user_id):
    """
    Retrieve a particular user from the server.
    :param user_id: Username of the desired user (String)
    """
    user = db.session.query(User).filter_by(username=user_id).first()
    if user:
        return jsonify({'user': user_to_dict(user)})
    return ("User not found in database", 404)


@app.route('/users/<user_id>', methods=['DELETE'])
@jwt_required
def delete_user(user_id):
    """
    Delete a user.
    :param user_id: Username of the desired user (String)
    """
    if get_jwt_identity() != 'admin':
        return "Unauthorized user trying to access administrative function", 401
    user_to_delete = db.session.query(User).filter_by(username=user_id).first()
    if user_to_delete:
        db.session.delete(user_to_delete)
        db.session.commit()
        return("User has been deleted", 200)
    return ("User not found in database", 404)


@app.route('/users/search/<user_id>', methods=['GET'])
def search_user(user_id):
    """
    Search users after their username.
    :param user_id: Username of the desired user (String)
    """
    #The % operator is used to get users even if the search term isn't complete
    search_string = "%{}%".format(user_id)
    search_results = db.session.query(User).filter(User.username.like(search_string)).all()
    if search_results:
        results_list = [user_to_dict(user) for user in search_results]
        return jsonify({'users': results_list})
    return "Search yielded no results", 404


@app.route('/users/<user_id>/tourneys')
def get_registered_tourneys(user_id):
    """
    Get every tournament a user has registered itself as a participant to.
    :param user_id: Username of the desired user (String)
    """
    registered_tourneys = db.session.query(Tourney).join(Tourney.participants).filter_by(username=user_id).all()
    if registered_tourneys:
        registered_tourneys = [tourney_and_game_dict(tourney) for tourney in registered_tourneys]
        return jsonify({'tourneys': registered_tourneys})
    return "User hasn't registered for any tourneys", 404


@app.route('/users/<user_id>/likes')
def get_liked_tourneys(user_id):
    """
    Get every tournament a user has 'liked'.
    :param user_id: Username of the desired user (String)
    """
    liked_tourneys = db.session.query(Tourney).join(Tourney.liked_by).filter_by(username=user_id).all()
    if liked_tourneys:
        liked_tourneys = [tourney_and_game_dict(tourney) for tourney in liked_tourneys]
        return jsonify({'tourneys': liked_tourneys})
    return "User hasn't liked any tourneys", 404


@app.route('/users/<user_id>/hosted_tourneys')
def get_hosted_tourneys(user_id):
    """
    Get every tournament a user has hosted - in other words, the ones they have created.
    :param user_id: Username of the desired user (String)
    """
    hosted_tourneys = db.session.query(Tourney).join(Tourney.tourney_hosts).filter_by(username=user_id).all()
    if hosted_tourneys:
        hosted_tourneys = [tourney_and_game_dict(tourney) for tourney in hosted_tourneys]
        return jsonify({'tourneys': hosted_tourneys})
    return "User hasn't created any tourneys", 404


@app.route('/users/login', methods=['POST'])
def login():
    """
    Log into the server as a particular registered user.
    """
    if request.json:
        json_dict = request.json
        login_user = User.query.filter_by(username=json_dict['username']).first()
        if login_user:
            # Check that the specified password is the same as the account's hashed and salted password.
            salted_password = login_user.password_salt + json_dict['password']
            if bcrypt.check_password_hash(login_user.password_hash, salted_password):
                token = create_access_token(identity=login_user.username)
                return jsonify({'token': token})
            return "Password is incorrect", 402
        return "Username is incorrect", 401
    return "No JSON in request", 400


@app.route('/users/logout', methods=['POST'])
@jwt_required
def logout():
    """
    Log the currently logged in user out of the server.
    """
    # Add the current access token to the list of blacklisted tokens, so that the token cannot be used again
    current_jti = get_raw_jwt()['jti']
    new_blist_entry = Blacklist(jti=current_jti)
    db.session.add(new_blist_entry)
    db.session.commit()
    return 'Access token revoked', 200

#
# GAMES
#


@app.route('/games', methods=['POST'])
@jwt_required
def add_game():
    """
    Creates a Game object from JSON data sent with request, and adds it to the server.
    """
    if get_jwt_identity() != 'admin':
        return "Unauthorized user trying to access administrative function", 401
    if request.json:
        json_dict = request.json
        essential_data  = ['id', 'title', 'release_year', 'publisher']
        for row in essential_data:
            if row not in json_dict:
                return "Request JSON missing essential data", 400
        if db.session.query(Game).filter_by(title=json_dict['title']).first() or db.session.query(Game).filter_by(id=json_dict['id']).first():
            return "Game already exists", 400
        if len(json_dict['id']) != 5:
            return "Game's ID must be exactly 5 characters long", 400

        # The following 4 attributes are not necessary, and will be left blank if not in JSON data
        if 'release_month' not in json_dict:
            json_dict['release_month'] = None
        if 'release_day' not in json_dict:
            json_dict['release_day'] = None
        if 'publisher' not in json_dict:
            json_dict['publisher'] = None
        if 'img' not in json_dict:
            json_dict['img'] = None

        new_game = Game(json_dict['id'], json_dict['title'], json_dict['release_year'], json_dict['release_month'],
                        json_dict['release_day'], json_dict['publisher'], json_dict['img'])

        # Adds platforms and alternative titles to the game if they are given
        if 'platforms' in json_dict:
            add_platforms(new_game, json_dict['platforms'])
        if 'alt_titles' in json_dict:
            add_alternative_titles(new_game, json_dict['alt_titles'])
        db.session.add(new_game)
        db.session.commit()
        return"Successfully added game", 200
    return "No JSON in request", 400


@app.route('/games/<game_id>', methods=['DELETE'])
@jwt_required
def delete_game(game_id):
    """
    Delete the given game from the server
    :param game_id: Unique ID of the desired game (String)
    """
    if get_jwt_identity() != 'admin':
        return "Unauthorized user trying to access administrative function", 401
    searched_game = db.session.query(Game).filter_by(id=game_id).first()
    if searched_game:
        db.session.delete(searched_game)
        db.session.commit()
        return "Game has been deleted", 200
    return "Game not found in database", 404


@app.route('/games/<game_id>/edit', methods=['POST'])
@jwt_required
def edit_game(game_id):
    """
    Edits attributes a given game. The attributes are updated with the new info specified in the request JSON.
    JSON can be any given data type, as it is unpacked during the function. Thus, if a String is to be delivered in the
    JSON, the JSON string must have nestled quotation marks.
    :param game_id: Unique ID of the desired game (String)
    """
    if get_jwt_identity() != 'admin':
        return "Unauthorized user trying to access administrative function", 401
    game = db.session.query(Game).filter_by(id=game_id).first()
    json_dict = request.json
    valid_attributes = ['id', 'title', 'release_year', 'release_month', 'release_day', 'publisher', 'platform']
    for attribute in json_dict.keys():
        if attribute not in valid_attributes:
            return "Not an editable attribute", 400
    if game:
        for current_edit in json_dict.keys():
            edit = "game.{} = json_dict[current_edit]".format(current_edit)
            exec(edit)
        db.session.commit()
        return "Edited game", 200
    return "Game not found in database", 404


@app.route('/games/<game_id>', methods=['GET'])
def get_game(game_id):
    """
    Retrieve a single game from the server.
    :param game_id: Unique ID of the desired game (String)
    """
    searched_game = db.session.query(Game).filter_by(id=game_id).first()
    if searched_game:
        return jsonify({'game': game_to_dict(searched_game)})
    return "Game not found in database", 404


@app.route('/games', methods=['GET'])
def get_all_games():
    """
    Retrieve all games in the server.
    """
    games = Game.query.all()
    if games:
        game_list = [game_to_dict(game) for game in games]
        return jsonify({'games': game_list})
    return "No games in database", 404


@app.route('/games/sort/<direction>/<condition>', methods=['GET'])
def sort_games(direction, condition):
    """
    Sorts a list of games after a condition, in either ascending or descending order.

    :param direction: String of the sorting direction
    :param condition: String of the condition to sort by
    """

    available_conditions = ['id', 'title', 'publisher', 'release_year']
    if condition in available_conditions:
        # If a list of games is supplied in the form of JSON data, that list will be sorted. Otherwise, all
        # games in the server will be sorted. This is so sort_games can be used in tandem with search_games
        if request.json:
            games_list = eval(request.json)['games']
        else:
            games = db.session.query(Game).all()
            games_list = [game_to_dict(game) for game in games]
        if games_list:
            games_list = sorted_games(games_list, condition, direction)
            return jsonify({'games': games_list})
        return "No games in database", 404
    return "Not a viable sorting condition. Games can be sorted in order of ID, title, publisher and release date.", 400


@app.route('/games/search/<condition>/<search_term>', methods=['GET'])
def search_games(condition, search_term):
    """
    Search for a particular game using one of several search conditions.

    :param condition: String of the search condition
    :param search_term: String of the search term
    """

    valid_conditions = ['title', 'release_year', 'publisher', 'platform']
    if condition not in valid_conditions:
        return "Not a valid search condition", 400

    if condition == 'title':
        search_string = "%{}%".format(search_term)
        search_results = db.session.query(Game).filter(Game.title.like(search_string)).all()
        # If search term doesn't correspond to game's main title, check if it does to any alternative title
        if not search_results:
            search_results = db.session.query(Game).join(Game.alt_titles).filter(AltTitle.name.like(search_string)).all()
    elif condition == 'release_year':
        search_results = Game.query.filter_by(release_year=search_term).all()
    elif condition == 'publisher':
        search_results = Game.query.filter_by(publisher=search_term).all()
    elif condition == 'platform':
        search_string = "%{}%".format(search_term)
        search_results = db.session.query(Game).join(Game.platforms).filter(Platform.name.like(search_string)).all()

    if search_results:
        results_list = [game_to_dict(game) for game in search_results]
        return jsonify({'games': results_list})
    return "Search yielded no results", 404


if __name__ == '__main__':
    app.run()

db.drop_all()
db.create_all()