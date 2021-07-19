const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql2/promise')
const request = require('sync-request');

const connection = mysql.createPool({
    connectionLimit: 32,
    host: '192.168.0.22',
    user: 'root',
    password: 'password',
    database: 'wpanel',
    waitForConnections: true
});

const apiOptions = {
    hostname: '192.168.0.13',
    port: 25740,
    path: '/api/v2',
    method: 'GET'
}

let issueStatus = false;


const app = express();

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.text({ type: 'application/json' }))

function generateUUID()
{
    let d = new Date().getTime();

    if( window.performance && typeof window.performance.now === "function" )
    {
        d += performance.now();
    }

    return 'xxxxxxx-xxxx-xxxx-axxx-xxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

function isInt(value) {
    return !isNaN(value) && (function(x) { return (x | 0) === x; })(parseFloat(value))
}

app.get('/api/v2', async function (req, res) {
    let id;
    res.setHeader("Content-Type", "application/json");
    if (req.method === "GET") {
        console.log("Request from [" + req.ip + "/" + req.hostname + "] " + req.method + " " + req.url);
        if (issueStatus) {
            res.status(503);
            res.send('');
            return;
        }
        let token;
        let account_uuid;

        token = req.query.token;
        account_uuid = req.query.uuid;
        id = parseInt(req.query.id, 10);
        let response = {}

        if (token === undefined) {
            response = {
                message: "Key token not found in JSONObject",
                status: 400
            }
        }

        if (account_uuid === undefined) {
            response = {
                message: "Key uuid not found in JSONObject",
                status: 400
            }
        }
        console.log(typeof (id) + ": " + id) ;
        if(id === undefined || typeof (id) !== 'number'){
            response = {
                message: "Key id not found or is not int in JSONObject",
                status: 400
            }
        }

        if (response.status === 400) {
            res.status(response.status);
            res.send(JSON.stringify(response));
            return;
        }

        switch (id) {
            case 1: {
                const result = await connection.execute('SELECT (uuid) FROM api_keys WHERE token = "' + token + '";');

                if (result[0].length === 0) {
                    const response = {
                        message: "Invalid API token or account uuid",
                        code: 403
                    }
                    res.send(JSON.stringify(response));
                    res.status(response.code);
                    return;
                }

                if (result[0][0].uuid !== account_uuid) {
                    const response = {
                        message: "Invalid API token or account uuid",
                        code: 403
                    }
                    res.status(response.code);
                    res.send(JSON.stringify(response));
                    return;
                }
                const apires = request(apiOptions.method, "http://" + apiOptions.hostname + ":" + apiOptions.port + apiOptions.path);
                if (apires.statusCode === 503){
                    res.status(503);
                    res.send('')
                    return;
                }
                res.send(JSON.stringify(JSON.parse(apires.body.toString()).data));
                break;
            }

            case 2: {
                const result = await connection.execute('SELECT (uuid) FROM api_keys WHERE token = "' + token + '";');

                if (result[0].length === 0) {
                    const response = {
                        message: "Invalid API token or account uuid",
                        code: 403
                    }
                    res.status(response.code);
                    res.send(JSON.stringify(response));
                    return;
                }

                if (result[0][0].uuid !== account_uuid) {
                    const response = {
                        message: "Invalid API token or account uuid",
                        code: 403
                    }
                    res.status(response.code);
                    res.send(JSON.stringify(response));
                    return;
                }

                res.status(200);
                res.send({message: "Auth ok", code: 200})
                break;
            }

            default : {
                const response = {
                    message: "Invalid id",
                    code: 400
                }
                res.send(JSON.stringify(response));
                res.status(response.code);
            }
        }
    } else {
        res.status(405);
    }
});

app.get('/api/v2/status', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    if(req.method === "GET"){
        //TODO
        const json_response = {
            status: "OK",
            message: "System nie znalazł żadnej usterki"
        };
        res.status(200);
        res.end(JSON.stringify(json_response));
    }else {
        res.status(400);
    }
});

app.get('/', function (req, res){
    res.status(400);
    res.send('');
});

app.listen(8080);