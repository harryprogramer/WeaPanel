import datetime
import secrets

import requests
import user
from flask import *

from errors.AccountNotActivated import AccountNotActivated
from errors.IncorrectAuthException import IncorrectAuthException
from errors.InvalidSessionException import InvalidSessionException
from errors.UserNotExistException import UserNotExistException

app = Flask(__name__)

api_url = "http://127.0.0.1:/api/v2"

key = """
aoDPEBTIdbHOc-tNdvYQvDO-FwHscVMfsWxrIw4KEJ8ZmQma1EPVMtogwSL-l0gDomdwGtrYVJqJ1CLRV6MSYsM5bJICy196On6JQVdsI-3g8CAczQ6oj5IKkrxGi-0lC2potFv3pPjfxfuNp__aarTIDUYoTD8JbLInEB_0XYn6O2xkwgCzSn_A_cy2K57kFbMW6i-CwuuYkdVqmyeIOmV5VbEVsEqpHBawNyLSRhps7gou5UayZ5DZEK3cNjHplCvl3TQiTbeD09ioNu-kXHzKvgG-G78wYZSCp1vxD01ucfsYC3ufnTCiw58uPWjQuCtbQu5hptnySwNz-uoTibZgkRMksdlLdZu_Epc3RbZ7-KMStJvneqdjVORp0Yj7pm-PB6qyyWgd5Q0KixA19iHmw8PRJ8HySVSqslWuBrJFIBM0lDHXlM_Qy8k8QUiRAjEx_GQu-h9mEbLoj16fDSWQscX9KGiMhxxJejpHb3xF0BEjZv8KE_O3-2eRp-lY4AVnAGPMmUHk4QFlJyt-oX7P8lQFtncGlNllZLEyihdnzgofL6-tYGPcEXvwpyPK1bJWLNM2krZGbnBKzGX27pCWD6d-xEy_jFGgzqzjo5HfHGCNM7nqm0XsZQ2wNdKdkvIf1X10WZLFYf4gQQbDtigT_LhSusffVy5eIafy_gQO5hCA3Pc9WUWBcfTua-ioKOrBu1_UqOOW9MtY3PVorZUDGsEz2D-3sieNFkx1wTG_J_WiJi0g015uCO0E4cmRda9zfa-vdWIkLdtxSzLrDiT6GxfbsQmcHZ2uAzL_kPkC4tFuhl7lJLeZaI8OUyRM38sQ015Utc41SRNKJ3wjMp4bFspADr7OGuSttikSkqok753cxhMGxBS1D7-7O_H3pipRaB3rmkOowhKUud127K5TPJb28bPtOzqIOYMoNUfVJXBm1wvbaFy1nqUi1whVxwkmQnuiQJg6IpgH8FeoecFJQL-G8pnTTDUpQ0wLQsZ4p41NeQZCfcwu6jFgNQm2qgFyTgZvBxjIhAxzWeIoJnhavKj52rXu-nDalqZyRauMQ7AglNGf5gPE7K5vrKHBSJ2mZ6Z8Ku8za0HMyJuUFFsBnnH5vWYIq3bIQKXYijzSH2h4fnqEwWAjHImag_mzT1yT2pR1NPT2bolkBl6JwxtK8hrQxZ0Kw7shEgTqMpTe-4rzQDPrMuCfbp2hcD9wP0scBzHm7PYAjLjEbvKWVCfuCFLegPWe-mzj7kidPhpPdG0YgMIA8HBmwlRBA8MDk9naYpNulqJH1LUUMkeuELblKtsid5hE7NrIiL-XM9wmVFnayjVb4YqbhNkDs3_096Y4neJTFpuhX4cnWU9M2ielc7pZhn1ipAeuRhJeVn4-u-JCGy9OkUnX-qC3-6NvhyYQmd2IHk_SFlAyO6hqpqoVhQsZYaJfoA72HR6UHPqrNCDdi9APc50SQ0cUJKSK4AUa827AgWjEVczLDcmV546zzd4NXCv1Lbpcs6nmOQeovpDUm6hCNFnZykR0S4Vex5cdo5SPYxXaTQt1riC8jNqETQ7XJYWNNvpkWV1litgel4iNdEVDLw6ZbSkL31HHO5krmVEEh0YPx0jxDr5e6VLQPG_MkKOTzKflPRXQTcwcUU_7nMzAAk3vxsRBjbmjoVu7JeOBMOM0--2Ui0P6Bh67vy_BT7fBVxbZr0syHsGCHdEiLFrFe8T-m8tTmt-eByVA4U1Wo4fZcf7W9vLorztfg-Z4l2QCc74ydv7HuXg4Z4Kt680H8iqKR7_DCLN0MrZMAJwP9oGUVQIHJoRjdbYlBug9GxwAeGDosEdO152nNygD7N6hM3W61Cd1Zp9cz_g2WI9CQB2kqF5-WNw7wVe5EPxD68wq9tLhmb_cg-PJGIq6vpFTJwWLuC4B8_ygK803ytz1fmzqEmDxWp8xiFLuS7QrAGD0SSGE6vrq5bEC-ggkHRNKQ6WajQEgL_srf9GjHG6kg6JhkE_8W04FmFIoNk8rqBE7Am25r4Lv6kT8BPPsekXxbU4ITlTYXcOnBnGzbAbPdgI6ym63pf_hEgkwQYMhGry0KeApFIQacNuXzZcfaaS9fYxakYjChqfsP7TwtBebR982bn5vzZchN0C8on-2Do51PI1CRtXH_6Bg0dygtAOamEFJcW23AdShqLKrToIrA6HLQxaHEHELj4h-xG4UfeH5rV0cpgNoEUE8DJ6uabqvpK1LFloS7wtLr-YbYItzHk_cT8DrPBHAwKtbB9zUJOM-AJo1vEaaq1Fd1RLAk1H6MU1CmXsIZg8zvHdM30yKPxgQXt0IaBOV2adwvXsglWA58M_-pN5Z3MAYMoBjweJDS_n7p9ynHwnosBuAaZvCwVISNCDmSWjgQPx8JG9GZpgD5KWgBlbSdlzeZOowVrF2-Q26jzWjYWjYSqrmcY6LdiYKPSExc3FdHSmfFVwqLC6Zj472lKFKVZfBHwTCRTZa-lx88sMnfgU6wDyheoKmI0QN4hvSi5Vn2VILxWSyFzO12MH26q3Tq5zG6QSDye-6GLxqW36vE8izmCHpze8hMmrjMTNFSoDL9gsrBZztuARI1MHLo2a4rZkobAqYqQYTLzREzId0gTYYdfQF6IwrWy8DvxeEJ2OyQliPEBUAusTDZzH1JRAfO7CP_hSSgStic6eJUlbYDguXxNDqzBM9oyTmro2-h-AMR7T8MDFKNtvagtUA6zRWDQi2qvI6JghMgxNgK4Roth-_ZnNiX0O0-Gn0iHa0vQwk18igh2fmLlnEBxmmzR6WfBMLSi8OrHNpeG4faGMEYgFGGa7AYyIposAnhjDz8XVywOTPNwkCcaupf7CywkU1dp6ASuxG3F3UU-yksOEWKTZaXXxmHNJ3i3LfNWt2kqxDaeeOhkRDz2ZQ69yIJEmZnZ2QbE475waU9ip2vF0uZ_HiMmVwDVR84RJMSW8thK0dSdawRO6oSboeZN9bJPqYcAX-bbpZMfBv3kknH9EPSCmUSHfSUdqfk754t6V_9Pz2GxBOovM_CyGgMLs2yI-hmebiR0MaLA83949vPIt4zVoX5orhtG1i6p1VR5nQD9duAOwxvAklQ556npSDLLknlma-3ddyGcX_YpfxebowiMz4KwVH0e9ZuWq9ibrCbPAhFWRZOGYkG8bGP6qhbhUa2d52fNhrf0mKgvv-ROso3KwUBes6jY4fQITAaN16HHY9ZhS5w-hmNAp5pFRAOWnkl40n7iDmt1NPoCXpx4Z_dQrY1j0XJXFpvHd5P1BVjab9rL7XAV1Ki3qHsDQaujv1J3AILTHjh5cJJFJnP0fl13Cc4AP0XsU8ztzkTJBVoD2CEKZy8N31JlUr4Q9UblANwmpP5t-BTFOXPCqGSJKCLkqhM5L7sLXZGslqLiEoGGx-DbhqDHKISsbvu9PTNRDP0L4aq4EcdK-WN6jH0Qubr9KUD_81an0Z4GVQokuFg7-0sCXouQVJ-vCdH8djBWnSJv-goMt7ZV-mRVIn7rkbwlkNiQnUQCq1kgtEkMfUY4BgA9eiHYQ7nFncvAESVHTobo0ZQliQBorybhPyU-eaROg0oYAYWLWCeo6m1m4qj_MGhONYgK-qcXJVV07UwcSdhgvwq686gMuyk_uY9_XMBwoVlaWgZ3D42PIys1Oa7Pv4UICSLIsJeOdOTwlli_xydMHM3w-B0ex2UkIJCSi_oUzYXHXyeLXcKQOhpk6KNK7XIK_XT2SPT1T6q9PBu3XFQQtjfTOMaz7i2ccFFlUbWjQwSq3er5w55iY308tZqZWHDlWtOFvvlASjVfc0N54bGL1_eIL4ZwQxTzoRHJhgHyxx5on8CUDyj01tO1MViSpw7Ia50MSn5933WNcW5lPMGK3MutNBMbdsDUZ8okR06MAxE189o0NyJLeKfpKpFIj8ehhZHLKAJG3u2Yr9TeF3iKQA-JIWA0cujDt3PXVzGgdbF4otOu1kJowUcsZFGesFi-rIIeJP6Im9cPULEkUmSwvd3IqvRMX3TbZwFRTP11uCelK0JdeKRgvr4RNKFKq019ey2_FVFjVb4F162Lpbo2NCgPPCZdb_CGMb5zAAIX6mUFNBf2lvxEOm6K6Ec3LEJHujr8OyhzbJthk1MOdURYgXf7pyFBjsZgsphYSsw1llLtrILESngLc6MYlZ4Qmfv6rKnS3navLjvPwk0V2rNBy6SjybYeSTSzfVAKrNf6yP-2cKGeg6WRhmXk6dkzkGbo9d7WQkGFxwIqKLfOR4SFV8W74IKMPjWM1pF3WNaAJleHIvm5nG2TbNc_HlMP5Y3nCAtINVqAfex_R1jaQO3l8T_AjAKkLw1-MEx6MblMrn-RwhvpeF9JOmxFKGPcNAuEQVYcrYa2ydMJVfvhsBp4N6SchmWFWQ2v5wOYfxkjY1WmDYpjRIjwBgWteVeh9FWHcmasi7QMRI65UBDsFbfCAW0PaU2dccXbDIMVih8AKnVo4GzLtBqWrKqk-Ylwgl-hnWot_38OJGRTcUQvt5Pt3-kJJeyF9Be10XhlQ_G1xRbE29iT2dR3iNsFXmj5JDTn9-NO1vQ9VAIvtB88xq-fyUHEyJ2UShZotms_MugjdgIUySNtRG5eJAng1PBrKAVO_TM-6S_Gvdr1HBKYGxracAvTExsevwosNL-ku6ydby2frzIfgLCHprkgMXSdaA3-hZVHVwGTemR6zD26KC4TGGwTc5JiNSCRWOoLDkXjNnOv9sz_iY4I8NgD1jdcydKFsRkk3dAr9oBcw7GzAGTqwmuI7myWLuFMbOUhIbFx8eIbv9ZeQiVRalAdzoGpuNjAhG2NcFFxBllVfIg2fGX8aMl7UyODhWyTQ4lp8XHPfm13wVYwD6bie1pjieUqygD9c76yX6UQ_oI4DXZvc9kPJ76w3X0J_WDRAXiqFlpuh5J0GvHKyH5q4Z916B8DqGDHYyfjpDhrUWEw9xWDVjS_lIagQhGCdQs2ZQeoH06FLwXFK9zXn_lMVsD6Mg3MUnCQLselueixSKawZwDnZsnr3aItYoLEUyb3ootwFSgWRvp208T78bvs5JT-0Uxr6TcAXyq2xFitR49ltjzCJhna8khMxg5Bs_PHDKflXCxo6p7l67nGgH3EQ2p0nkjLvHM3ahx1EcbO0fPpc6G3NNgF7QUgFkT5QInWifq3lyF-NfasJQXNMPcEzZcb_RfVCToYJLa8WFR2_8MgfXeI1KAZtbiOn4QiXQN8vTbwdTbCaA_0SgtEjOC3odWiydv6JU9sj1oK__bJbu0E1_ggekOlJ3_WD3me4NueOJ6rCyOBoWfR1a5JcgcLEe4EtSXAmjkj316MwZutKcn1Fsj39mQ6FsJMMOwXVnSet8XnQINHjc0J-o_N4rwda7y6xJhzw1uetcUWJ45d73rhCyiQcuUoVDfxvT2RgojQaonh8kRXnT9EL4ixXKs1uo-fZV1woeCg
"""

app.secret_key = key


@app.route('/')
def index():
    return redirect('/login')


@app.route('/login')
def login():
    try:
        user.UserUtils.getUserBySession(session["session"])
        return redirect('/app/dashboard', 303)
    except Exception:
        session.clear()

    return render_template("login_form.html")


@app.route('/static/<path:path>')
def send_js(path):
    return send_from_directory('static/', path)


@app.route('/auth/login', methods=['POST', 'GET'])
def login_api():
    if request.method == "POST":
        try:
            user_obj = user.UserUtils.getUserByAuth(request.json["username"], request.json["password"])
            jsonresponse = f'"token": "{user.UserUtils.generateSessionToken(user_obj.uuid, request.remote_addr)}"'
            return "{" + jsonresponse + "}", 200
        except IncorrectAuthException:
            return '{"message": "Invalid email or password"}', 401
        except AccountNotActivated:
            connection = user.connection_pool.get_connection()
            cursor = connection.cursor()
            uuid = user.UserUtils.getUUIDByEmail(request.json["username"])
            try:
                cursor.execute(f"DELETE FROM activate_tokens WHERE uuid = '{uuid}'")
                connection.commit()
            except:
                pass

            token = secrets.token_urlsafe(64)
            cursor.execute(f"INSERT INTO activate_tokens (token, uuid) VALUES ('{token}', '{uuid}')")
            connection.commit()
            connection.close()
            return '{"token": "' + token + '"}', 501
    else:
        return redirect(url_for('login'), 303)


@app.route('/app/dashboard', methods=['GET'])
def dashboard():
    idToken: str
    try:
        idToken = session["session"]
    except KeyError:
        return redirect('/login', 303)

    try:
        userObj = user.UserUtils.getUserBySession(idToken)
    except InvalidSessionException:
        session["session"] = "0"
        return redirect('/login', 303)
    return render_template("dashboard.html", name=userObj.name, email=userObj.email, apikey=userObj.getApiKey(),
                           uuid=userObj.uuid), 200


@app.route('/auth/performSession/<string:uuid_id>', methods=['GET', ])
def performSession(uuid_id):
    connection = user.connection_pool.get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute(f"SELECT uuid, allowedIP, expirationDate FROM sessionsToken where token = '{uuid_id}'")
        result = cursor.fetchall()
        if result[0][1] != request.remote_addr:
            raise Exception()
        date = datetime.datetime.fromtimestamp(result[0][2])
        if datetime.datetime.now() > date:
            sql = f"DELETE FROM sessionsToken where token='{uuid_id}'"
            cursor.execute(sql)
            connection.commit()
            connection.close()
            raise Exception()

        sql = f"DELETE FROM sessionsToken where token='{uuid_id}'"
        cursor.execute(sql)
        connection.commit()
        connection.close()
        session["session"] = user.UserUtils.generateSessionID(result[0][0])
        return redirect('/app/dashboard', 303)
    except Exception:
        response = app.response_class(
            response='',
            status=403,
            mimetype='application/json'
        )
        return response


@app.route('/auth/reset/password', methods=['POST'])
def resetPassword():
    connection = user.connection_pool.get_connection()
    cursor = connection.cursor()
    email = request.json["email"]
    if request.json["token"] is None:
        try:
            resetToken = secrets.token_urlsafe(512)
            cursor.execute(f"INSERT INTO forgotTokens (token, uuid) VALUES ('{resetToken}', "
                           f"'{user.UserUtils.getUUIDByEmail(email)}')")
            connection.commit()
            print(f'reset token for {user.UserUtils.getUUIDByEmail(email)} is {resetToken}')
            return app.response_class(
                response='{"message": "Check email box for next instructions"}',
                status=200,
                mimetype='application/json'
            )
        except UserNotExistException:
            return app.response_class(
                response='{"message": "Email not exist"}',
                status=400,
                mimetype='application/json'
            )
    else:
        newPassword = request.json["password"]
        resetToken = request.json["token"]
        cursor.execute(f"SELECT uuid FROM forgotTokens where token = '{resetToken}'")
        result = cursor.fetchall()
        connection.close()
        if len(result) == 0:
            return app.response_class(
                response='{"message": "Invalid reset token"}',
                status=400,
                mimetype='application/json'
            )
        userobj = user.UserUtils.getUserByUUID(result[0][0])
        userobj.resetPassword(newPassword)

        return app.response_class(
            response='{"message": "The password has been reset, you can back to login"}',
            status=200,
            mimetype='application/json'
        )


@app.route('/forgot', methods=['GET', ])
def forgot():
    return render_template('reset_password_form.html')


@app.route('/activate', methods=['GET', 'POST'])
def activate_Account():
    if request.args.get("token") is None:
        return redirect('/login', 303)

    return render_template("activate_account_form.html", activate_rules="""
        Przed pierwszym użyciem nowego konta musi
        zostać ono aktywowane. Upewnij się, że
        zapoznałeś
        się z wytycznymi korzystania z usługi
        WPanel i WPanel SDK/API, której właścicielem
        jest PogchampTS. Aktywując konto, zgadzasz się
        na podane wytyczne
        i zbieranie informacji diagnostycznych.
    """, activate_rules_title="Przed aktywowaniem konta")


@app.route('/logout', methods=['GET'])
def logout():
    session.clear()
    return redirect('/login', 303)


@app.route('/reset', methods=['GET'])
def resetPasswordForm():
    if request.args.get("token") is None:
        return redirect('/login', 303)
    return render_template('reset_password_final.html')


@app.route('/api/status', methods=['GET'])
def server_Status():
    try:
        return requests.get(api_url + "/status").content
    except Exception:
        return "", 504


@app.route('/auth/activate', methods=["POST"])
def activateAccountApi():
    connection = user.connection_pool.get_connection()
    cursor = connection.cursor()
    token = request.json["token"]
    cursor.execute(f"SELECT uuid FROM activate_tokens WHERE token = '{token}'")
    result = cursor.fetchall()

    if len(result) != 0:
        account_name = request.json["name"]
        phone_number = request.json["number"]
        password = request.json["password"]

        userObj = user.UserUtils.getUserByUUID(result[0][0])
        userObj.changeName(account_name)
        userObj.changeNumber(phone_number)
        userObj.resetPassword(password)
        cursor.execute(f"UPDATE accounts SET activated = 'yes' WHERE uuid = '{result[0][0]}' ")
        cursor.execute(f"DELETE FROM activate_tokens where uuid = '{result[0][0]}'")
        connection.commit()
        try:
            cursor.execute(f"DELETE FROM api_keys where uuid = '{result[0][0]}'")
            connection.commit()
        except:
            pass
        cursor.execute(f"INSERT INTO api_keys (token, uuid) VALUES ('{secrets.token_urlsafe(64)}', '{result[0][0]}')")
        connection.commit()
        connection.close()
        return '{"message": "Konto zostało aktywowane"}', 201
    return '{"message": "Konto nie znalezione"}', 404


@app.route('/user/api/resetApiKey', methods=["GET"])
def resetApiToken():
    idToken: str
    try:
        idToken = session["session"]
    except KeyError:
        return redirect('/login', 303)

    try:
        userObj = user.UserUtils.getUserBySession(idToken)
    except InvalidSessionException:
        session["session"] = "0"
        return redirect('/login', 303)
    connection = user.connection_pool.get_connection()
    cursor = connection.cursor()

    cursor.execute(f"UPDATE api_keys SET token = '{secrets.token_urlsafe(64)}' WHERE uuid = '{userObj.uuid}'")
    connection.commit()
    connection.close()
    return redirect('/app/dashboard')
