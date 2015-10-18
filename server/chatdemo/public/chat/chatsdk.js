(function() {

    var ChatSdk = {};
    window.ChatSdk = ChatSdk;

    // define error codes
    ChatSdk.ERR_BAD_REQUEST         = 400;
    ChatSdk.ERR_INVALID_USER_ID     = 100;
    ChatSdk.ERR_USER_ALREADY_EXIST  = 101;
    ChatSdk.ERR_INVALID_ROOM_ID     = 200;

    // internal variables
    var httpServer;
    var wsServer;
    var listener;
    var ws;
    var seq = 1;
    var callbacks = {};

    function onCallback(tag, json) {
        var cb = callbacks[tag];
        if (!cb) {
            return;
        }
        delete callbacks[tag];
        var err = json["err"];
        if (err == 0) {
            cb.invoke(json);
        } else {
            cb.onError(err);
        }
    }

    function onNewRoom(json) {
        var roomId = json["room_id"];
        listener.onNewRoom(roomId);
    }

    function onNewMessage(json) {
        message = json;
        listener.onNewMessage(message);
    }

    function getApiCallback(cb, invoke) {
        if (cb) {
            cb.invoke = invoke;
        }
        return function(json) {
            if (!cb) {
                return;
            }
            var err = json["err"];
            if (err == 0) {
                cb.invoke(json);
            } else {
                cb.onError(err);
            }
        };
    }

    function send(action, cb, params, invoke) {
        var tag = seq;
        seq++;
        data = {
            tag: tag,
            action: action
        };
        _.extend(data, params);
        if (cb) {
            cb.invoke = invoke;
            callbacks[tag] = cb;
        }
        ws.send(JSON.stringify(data));
        console.debug("ws.send", data);
    }

    ChatSdk.initialize = function(_httpServer, _wsServer, _listener) {
        httpServer = _httpServer;
        wsServer   = _wsServer;
        listener   = _listener;

        if (!listener) {
            listener = {
                onConnect: function() {},
                onDisconnect: function() {},
                onNewRoom: function(roomId) {},
                onNewMessage: function(message) {}
            };
        }

        ws = new WebSocket(wsServer);
        ws.onopen = function(e) {
            listener.onConnect();
        };
        ws.onmessage = function(e) {
            console.debug("ws.onmessage", e.data);
            var json = JSON.parse(e.data);
            var tag = json["tag"];
            if (tag) {
                onCallback(tag, json);
            } else {
                var evt = json["event"];
                if (evt == "on_new_room") {
                    onNewRoom(json);
                }
                else if (evt == "on_new_message") {
                    onNewMessage(json);
                }
            }
        };
        ws.onclose = function(e) {
            listener.onDisconnect();
        }
        ws.onerror = function(e) {
            listener.onDisconnect();
        }
    }

    ChatSdk.register = function(userId, cb) {
        $.ajax({
            type: "POST",
            url: httpServer + "/api/register",
            data: { user_id: userId },
            success: getApiCallback(cb, function(json) {
                cb.onSuccess();
            })
        });
    };

    ChatSdk.getAllUsers = function(cb) {
        $.ajax({
            url: httpServer + "/api/get_all_users",
            success: getApiCallback(cb, function(json) {
                var userIds = [];
                for (var i = 0; i < json["users"].length; i++) {
                    userIds.push(json["users"][i]["user_id"]);
                }
                cb.onSuccess(userIds);
            })
        });
    };

    ChatSdk.login = function(userId, cb) {
        send("login", cb, { user_id: userId }, function(json) {
            cb.onSuccess(json["rooms"]);
        });
    };

    ChatSdk.createRoom = function(userIds, name, cb) {
        send("create_room", cb, { users: userIds, name: name }, function(json) {
            cb.onSuccess(json["id"]);
        });
    };

    ChatSdk.getRoom = function(roomId, cb) {
        send("get_room", cb, { room_id: roomId }, function(json) {
            cb.onSuccess(json);
        });
    };

    ChatSdk.createMessage = function(roomId, body, cb) {
        send("create_message", cb, { room_id: roomId, body: body }, function(json) {
            cb.onSuccess(json["id"]);
        });
    };

    ChatSdk.getRoomMessages = function(roomId, limit, cb) {
        send("get_room_messages", cb, { room_id: roomId, limit: limit }, function(json) {
            cb.onSuccess(json["messages"]);
        });
    }
})();