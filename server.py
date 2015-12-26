﻿from flask import Flask
from flask_restful import reqparse, abort, Api, Resource
import random
import string

app = Flask(__name__)
api = Api(app)

USERS = {}

TOKENS = {}

def abort_if_token_doesnt_exist(token_id):
    if token_id not in TOKENS:
        abort(401, message='wrong token')

def abort_if_user_doesnt_exist(user_id):
    if user_id not in USERS:
        abort(404, message='user {} doesn\'t exist'.format(user_id))

def get_list_of_products_for_user(user_id):
    abort_if_user_doesnt_exist(user_id)
    return USERS[user_id]['products']

def abort_if_product_doesnt_exist(user_id, product_id):
    if product_id not in get_list_of_products_for_user(user_id):
        abort(404, message='product {} doesn\'t exist'.format(product_id))

def create_if_product_doesnt_exist(token_id, product_id):
    abort_if_token_doesnt_exist(token_id)      
    user_id = TOKENS[token_id][0]
    if product_id not in get_list_of_products_for_user(user_id):
        abort_if_user_doesnt_exist(user_id)
        parser = reqparse.RequestParser()
        parser.add_argument('name')
        args = parser.parse_args()
        get_list_of_products_for_user(user_id)[product_id] = {
            'name': product_id,
            'count': 0,
            'version': 1,
    	    'vers': { TOKENS[token_id][1]: 1},
    	    'diffs': { 1: 'created' },
            'wasRemoved': 'false'
            }

def generate_token():
    token = ''
    for i in range(32):
        token = token + random.choice(string.ascii_lowercase)
    return token

# user
class user(Resource):
    def get(self, user_id, password_value, device_id):
        if user_id in USERS:
            password_in_db = USERS[user_id]['password']
            if(password_value == password_in_db):
                token = generate_token()
                TOKENS[token] = [user_id, device_id]
                ans = { 'token' : token }
                return ans, 200
            else:
                abort(401, message='wrong password')
        else:
            USERS[user_id] = {
                'login': user_id,
                'password': password_value,
                'products': {}
                }
            token = generate_token()
            TOKENS[token] = [user_id, device_id]
            ans = { 'token' : token }
            return ans, 200

# list_of_products
class list_of_products(Resource):
    def get(self, token_id):
        abort_if_token_doesnt_exist(token_id)
        user_id = TOKENS[token_id][0]
        return get_list_of_products_for_user(user_id), 200

# product
class product(Resource):
    def get(self, token_id, product_id):
        abort_if_token_doesnt_exist(token_id)      
        user_id = TOKENS[token_id][0]
        abort_if_user_doesnt_exist(user_id)
        abort_if_product_doesnt_exist(user_id, product_id)
        return get_list_of_products_for_user(user_id)[product_id], 200

    def delete(self, token_id, product_id):  
        abort_if_token_doesnt_exist(token_id)
        user_id = TOKENS[token_id][0]
        if product_id not in get_list_of_products_for_user(user_id):
            return '', 200

        cur_ver = get_list_of_products_for_user(user_id)[product_id]['version'] + 1
        get_list_of_products_for_user(user_id)[product_id]['count'] = 0
        get_list_of_products_for_user(user_id)[product_id]['version'] = cur_ver
        get_list_of_products_for_user(user_id)[product_id]['wasRemoved'] = 'true'
        get_list_of_products_for_user(user_id)[product_id]['vers'][TOKENS[token_id][1]] = cur_ver
        get_list_of_products_for_user(user_id)[product_id]['diffs'][cur_ver] = 'removed'
        return '', 200

class product_with_count(Resource):
    def put(self, token_id, product_id, value, ver):
        abort_if_token_doesnt_exist(token_id)
        user_id = TOKENS[token_id][0]
        abort_if_user_doesnt_exist(user_id)
        create_if_product_doesnt_exist(token_id,product_id)
        count = get_list_of_products_for_user(user_id)[product_id]['count']
        count = int(int(count)+int(value))
        cur_ver = get_list_of_products_for_user(user_id)[product_id]['version']
        if TOKENS[token_id][1] in get_list_of_products_for_user(user_id)[product_id]['vers']:
            c = get_list_of_products_for_user(user_id)[product_id]['diffs'][cur_ver]
            if int(ver) < get_list_of_products_for_user(user_id)[product_id]['vers'][TOKENS[token_id][1]] and c != 'created' and c != 'removed':
                count = int(int(count)-int(c))
        if(count < 0):
            count = 0
        cur_ver = cur_ver + 1
        get_list_of_products_for_user(user_id)[product_id]['count'] = str(count)
        get_list_of_products_for_user(user_id)[product_id]['version'] = cur_ver
        get_list_of_products_for_user(user_id)[product_id]['wasRemoved'] = 'false'
        get_list_of_products_for_user(user_id)[product_id]['vers'][TOKENS[token_id][1]] = cur_ver
        get_list_of_products_for_user(user_id)[product_id]['diffs'][cur_ver] = 'removed'
        return '', 200

class print_users(Resource):
    def get(self):
        return USERS, 200

class print_tokens(Resource):
    def get(self):
        return TOKENS, 200

##
## Actually setup the Api resource routing here
##

api.add_resource(user, '/users/<user_id>/password/<password_value>/devices/<device_id>')
api.add_resource(list_of_products, '/token/<token_id>/products')
api.add_resource(product, '/token/<token_id>/products/<product_id>')
api.add_resource(product_with_count, '/token/<token_id>/products/<product_id>/diff/<value>/version/<ver>')
api.add_resource(print_users, '/print_users');
api.add_resource(print_tokens, '/print_tokens');

if __name__ == '__main__':
    app.run(debug=True)