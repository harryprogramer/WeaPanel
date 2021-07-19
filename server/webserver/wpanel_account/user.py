import secrets, time
from datetime import datetime
from datetime import timedelta

from mysql.connector import pooling

import utils
from errors.AccountNotActivated import AccountNotActivated
from errors.IncorrectAuthException import IncorrectAuthException
from errors.InvalidSessionException import InvalidSessionException
from errors.UserNotExistException import UserNotExistException

connection_pool = pooling.MySQLConnectionPool(pool_name="pool",
                                              pool_size=32,
                                              pool_reset_session=True,
                                              host='192.168.0.22',
                                              database='wpanel',
                                              user='root',
                                              password='password')


class User(object):
    uuid: str
    name: str
    email: str
    phone: int

    def __init__(self, uuid, name, email, phone):
        self.uuid = uuid
        self.name = name
        self.email = email
        self.phone = phone

    def resetPassword(self, new_password):
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        hashed_password = utils.hash_password(new_password)
        cursor.execute(f"UPDATE accounts SET password = '{hashed_password}' WHERE uuid = '{self.uuid}'")
        connection.commit()
        connection.close()

    def changeNumber(self, number: int) -> None:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"UPDATE accounts SET phone = '{number}' WHERE uuid = '{self.uuid}'")
        connection.commit()
        connection.close()

    def changeName(self, name: str) -> None:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"UPDATE accounts SET acc_name = '{name}' WHERE uuid = '{self.uuid}'")
        connection.commit()
        connection.close()

    def getApiKey(self) -> str:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"SELECT token FROM api_keys WHERE uuid = '{self.uuid}' ")
        result = cursor.fetchall()
        connection.close()

        return result[0][0]


class UserUtils:
    @staticmethod
    def generateSessionToken(uuid: str, ip: str) -> str:
        token = secrets.token_urlsafe(512)
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        expiration_date = datetime.now()
        expiration_date = expiration_date + timedelta(seconds=15)
        sql = f"INSERT INTO sessionsToken (token, uuid, allowedIP, expirationDate) VALUES ('{token}', '{uuid}'," \
              f" '{ip}' ,'{time.mktime(expiration_date.timetuple())}')"
        cursor.execute(sql)
        connection.commit()
        connection.close()
        return token

    @staticmethod
    def getUserBySession(session_id) -> User:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"SELECT account_uuid, expiration_date FROM wpanel_sessions where id = '{session_id}'")
        result = cursor.fetchall()
        if len(result) != 0:
            if not datetime.now() > datetime.fromtimestamp(float(result[0][1])):
                connection.close()
                return UserUtils.getUserByUUID(result[0][0])
            cursor.execute(f"DELETE FROM wpanel_sessions where id = '{session_id}'")
            connection.commit()

        connection.close()
        raise InvalidSessionException()

    @staticmethod
    def generateSessionID(uuid) -> str:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        try:
            sql = f"DELETE FROM wpanel_sessions where account_uuid='{uuid}'"
            cursor.execute(sql)
            connection.commit()
        except Exception:
            pass

        expiration_date = datetime.now()
        expiration_date = expiration_date + timedelta(minutes=15)
        token = secrets.token_urlsafe(256)
        sql = f"INSERT INTO wpanel_sessions (id, account_uuid, expiration_date) VALUES ('{token}', '{uuid}'," \
              f" '{time.mktime(expiration_date.timetuple())}')"
        cursor.execute(sql)
        connection.commit()
        connection.close()
        return token

    @staticmethod
    def getUserByUUID(uuid) -> User:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"SELECT email, phone, acc_name FROM accounts where uuid = '{uuid}'")
        result = cursor.fetchall()
        connection.close()
        if len(result) != 0:
            return User(uuid, result[0][2], result[0][0], result[0][1])
        raise UserNotExistException()

    @staticmethod
    def getUserByAuth(email, password) -> User:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"SELECT password, uuid, acc_name, phone, activated FROM accounts where email = '{email}'")
        result = cursor.fetchall()
        connection.close()
        if len(result) != 0:
            if utils.verify_password(result[0][0], password):
                if result[0][4] == "yes":
                    return User(result[0][1], result[0][2], email, result[0][3])
                raise AccountNotActivated()

        raise IncorrectAuthException()

    @staticmethod
    def getUUIDByEmail(email) -> str:
        connection = connection_pool.get_connection()
        cursor = connection.cursor()
        cursor.execute(f"SELECT uuid FROM accounts where email = '{email}'")
        result = cursor.fetchall()
        if len(result) != 0:
            return result[0][0]

        raise UserNotExistException()
