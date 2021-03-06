(function() {

    var ChatDemo = {};
    window.ChatDemo = ChatDemo;

    EVENT_CHAT_LOGIN_SUCCESS    = 1;
    EVENT_CHAT_ON_NEW_ROOM      = 2;
    EVENT_CHAT_ON_NEW_MESSAGE   = 3;

    var $rooms = [];
    var $roomMessages = {};
    var $userId;
    var $currentRoomId;

    var locationPath = location.href.split("/")[2].split(":")[0];

    ChatSdk.initialize("http://" + locationPath + ":3000", "ws://" + locationPath + ":8080", {
        onConnect: function() {},
        onDisconnect: function() {},
        onNewRoom: function(roomId) {
            ChatSdk.getRoom(roomId, {
                onSuccess: function(r) {
                    $rooms.push(r);
                    SubPub.pub(EVENT_CHAT_ON_NEW_ROOM, r);
                },
                onError: function(err) {}
            });
        },
        onNewMessage: function(m) {
            var messages = $roomMessages[m.room_id];
            if (!messages) {
                messages = [];
                $roomMessages[m.room_id] = messages;
            }
            messages.splice(0, 0, m);
            SubPub.pub(EVENT_CHAT_ON_NEW_MESSAGE, m);
        }
    });

    ChatDemo.onLoginButtonClick = function() {
        var userId = $("#LOGIN_SCREEN #userIdLoginInput").val();
        if (_.isEmpty(userId)) {
            return;
        }
        login(userId);
    };

    ChatDemo.onRegisterButtonClick = function() {
        var userId = $("#LOGIN_SCREEN #userIdRegisterInput").val();
        if (_.isEmpty(userId)) {
            return;
        }
        ChatSdk.register(userId, {
            onSuccess: function() {
                login(userId);
            },
            onError: function(err) {
                if (err == ChatSdk.ERR_USER_ALREADY_EXIST) {
                    alert("User ID is already existing");
                }
            }
        });
    };

    ChatDemo.onRoomClick = function(roomId) {
        ChatSdk.getRoomMessages(roomId, 0, {
            onSuccess: function(messages) {
                $roomMessages[roomId] = messages;
                $currentRoomId = roomId;
                displayRooms();
                displayMessages();
            },
            onError: function(err) {}
        });
    };

    ChatDemo.onSendButtonClick = function() {
        
        if (!$currentRoomId) {
            return;
        }

        var input = $("#MESSAGE_INPUT input");
        var body = input.val();
        if (_.isEmpty(body)) {
            return;
        }

        ChatSdk.createMessage($currentRoomId, body, {
            onSuccess: function(id) {
                input.val("");
            },
            onError: function(err) {
                alert("Failed! Please try later");
            }
        });
    };

    ChatDemo.onUserClick = function(userId) {
        $("#USER_LIST .user img[data-userid='" + userId + "']").toggle();
    };

    ChatDemo.onCreateRoomButtonClick = function() {
        var selectedUserIds = [];
        $("#USER_LIST .user img:visible").each(function(index, obj) {
            selectedUserIds.push($(obj).data("userid"));
        });
        if (selectedUserIds.length == 0) {
            return;
        }
        var name = prompt("Please input room name", "");
        if (_.isEmpty(name)) {
            return;
        }
        ChatSdk.createRoom(selectedUserIds, name, {
            onSuccess: function(id) {
                // automatically jump to new room when it is created
                SubPub.sub(EVENT_CHAT_ON_NEW_ROOM, function(r) {
                    if (id != r.id) {
                        return;
                    }
                    ChatDemo.onRoomClick(id);
                    SubPub.unsub(this, EVENT_CHAT_ON_NEW_ROOM);
                });

                // deselect all selected users
                $("#USER_LIST .user img:visible").hide();
            },
            onError: function(err) {
                alert("Failed! Please try later");
            }
        })
    };

    function getRoomLatestMessage(roomId) {
        var messages = $roomMessages[roomId];
        if (messages && messages.length > 0) {
            return messages[0];
        } else {
            return null;
        }
    }

    function login(userId) {
        ChatSdk.login(userId, {
            onSuccess: function(rooms) {
                $rooms = rooms;
                var c = 0;
                var check = function() {
                    if (c == $rooms.length) {
                        $userId = userId;
                        SubPub.pub(EVENT_CHAT_LOGIN_SUCCESS);
                    }
                };
                if ($rooms.length > 0) {
                    _.each($rooms, function(r) {
                        ChatSdk.getRoomMessages(r.id, 1, {
                            onSuccess: function(messages) {
                                $roomMessages[r.id] = messages;
                                c++;
                                check();
                            },
                            onError: function(err) {
                                c++;
                                check();
                            }
                        });
                    });
                } else {
                    check();
                }
            },
            onError: function(err) {
                if (err == ChatSdk.ERR_INVALID_USER_ID) {
                    alert("Invalid User ID");
                } else {
                    alert("Failed! Please try later");
                }
            }
        });
    }

    function displayRooms() {
        var container = $("#ROOM_LIST .panel-body");
        container.empty();

        _.each(
            _.sortBy($rooms, function(r) {
                var latestMessage = getRoomLatestMessage(r.id);
                return latestMessage ? -latestMessage.time : 0;
            }),
            function(r) {
                var latestMessage = getRoomLatestMessage(r.id);
                var clazz = r.id == $currentRoomId ? "bs-callout-danger" : "bs-callout-default";
                container.append(
                    '<div class="room bs-callout ' + clazz + '" data="' + r.id + '" onclick="ChatDemo.onRoomClick(\'' + r.id + '\');">' +
                        '<h5>' + r.name +'</h5>' +
                        (latestMessage ? latestMessage.body : "-") +
                    '</div>'
                )
            });
    }

    function displayMessages() {
        var container = $("#TALK_LAYOUT #MESSAGE_LIST");
        container.empty();

        var messages = $roomMessages[$currentRoomId];
        if (!messages) {
            return;
        }
        _.each(messages, function(m) {
            container.append('<pre>' + m.user_id + ": " + m.body + '</pre>');
        });
    }

    function displayUsers() {
        ChatSdk.getAllUsers({
            onSuccess: function(userIds) {
                userIds = _.without(userIds, $userId);
                var container = $("#USER_LIST .panel-body");
                container.empty();
                _.each(userIds, function(id) {
                    container.append(
                        '<div class="user" onclick="ChatDemo.onUserClick(\'' + id + '\');">' +
                            '<table style="width:100%">' +
                                '<tr>' +
                                    '<td>' +
                                        '<h5>' + id + '</h5>' +
                                    '</td>' +
                                    '<td>' +
                                        '<img src="check.png" data-userid="' + id + '"/>' +
                                    '</td>' +
                                '</tr>' +
                            '</table>' +
                        '</div>'
                    );
                });
                container.find("img").hide();
            },
            onError: function(err) {}
        });
    }

    $(function() {
        
        // display login screen
        $("#LOGIN_SCREEN").show();
        $("#TALK_SCREEN").hide();

        // trigger send when user press enter
        $("#MESSAGE_INPUT input").keypress(function(event) {
            if (event.keyCode == 13) {
              ChatDemo.onSendButtonClick();
              return false;
            } else {
              return true;
            }
        });

        // justify UI
        $(window).bind("resize", function() {
            var roomListPanel = $("#ROOM_LIST .panel");
            var roomListPanelHeading = $("#ROOM_LIST .panel-heading");
            $("#ROOM_LIST .panel-body").height(roomListPanel.height() - roomListPanelHeading.height() - 20);
            
            var talkLayout = $("#TALK_LAYOUT");
            var talkLayoutMessageInput = $("#MESSAGE_INPUT");
            $("#TALK_LAYOUT #MESSAGE_LIST").height(talkLayout.height() - talkLayoutMessageInput.height() - 10);

            var userListPanel = $("#USER_LIST .panel");
            var userListPanelHeading = $("#USER_LIST .panel-heading");
            $("#USER_LIST .panel-body").height(userListPanel.height() - userListPanelHeading.height() - 20);
        });
        $(window).trigger("resize");

        // listen for events
        SubPub.sub(EVENT_CHAT_LOGIN_SUCCESS, function(data) {
            
            // switch to Talk screen
            $("#LOGIN_SCREEN").hide();
            $("#TALK_SCREEN").show();
            $(window).trigger("resize");

            // display rooms
            displayRooms();

            // load and display users
            displayUsers();
        });
        SubPub.sub(EVENT_CHAT_ON_NEW_ROOM, function(room) {
            displayRooms();
        });
        SubPub.sub(EVENT_CHAT_ON_NEW_MESSAGE, function(m) {
            if (m.room_id == $currentRoomId) {
                displayMessages();
            }
            displayRooms();
        });
    });
})();