package datdo.co.jp.chatdemo.util;

import android.util.Log;

import com.datdo.mobilib.event.MblCommonEvents;
import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblStrongEventListener;
import com.datdo.mobilib.util.MblSerializer;
import com.datdo.mobilib.util.MblUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import datdo.co.jp.chatdemo.BuildConfig;
import datdo.co.jp.chatdemo.sdk.ChatSdk;
import datdo.co.jp.chatdemo.sdk.ChatSdk.*;
import datdo.co.jp.chatdemo.sdk.Listener;
import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.sdk.Room;

/**
 * Created by dat on 2015/10/17.
 */
public class ChatUtil {

    private static final String TAG = MblUtils.getTag(ChatUtil.class);
    private static final String PREF_USER_ID = "Chat.user_id";

    private static List<Room> sRooms = new ArrayList<>();
    private static Map<String, List<Message>> sRoomMessages = new HashMap<>();
    private static boolean sConnected;
    private static boolean sConnecting;
    private static String sUserId;

    public static void initialize() {
        // init Chat SDK
        ChatSdk.initialize(
                BuildConfig.HTTP_SERVER,
                BuildConfig.WS_SERVER,
                new Listener() {
                    @Override
                    public void onConnect() {
                        sConnected = true;
                        sConnecting = false;
                        MblEventCenter.postEvent(null, Event.CHAT_CONNECTED);

                        if (getUserId() != null) {
                            login();
                        }
                    }

                    @Override
                    public void onDisconnect() {
                        sConnected = false;
                        sConnecting = false;
                        MblEventCenter.postEvent(null, Event.CHAT_DISCONNECTED);
                    }

                    @Override
                    public void onNewRoom(String roomId) {
                        ChatSdk.getInstance().getRoom(roomId, new GetRoomCallback() {
                            @Override
                            public void onSuccess(Room room) {
                                sRooms.add(room);
                                MblEventCenter.postEvent(null, Event.CHAT_ON_NEW_ROOM, room);
                            }

                            @Override
                            public void onError(int err) {}
                        });
                    }

                    @Override
                    public void onNewMessage(Message message) {
                        List<Message> roomMessages = sRoomMessages.get(message.getRoomId());
                        if (roomMessages == null) {
                            roomMessages = new ArrayList<>();
                            sRoomMessages.put(message.getRoomId(), roomMessages);
                        }
                        roomMessages.add(0, message);
                        MblEventCenter.postEvent(null, Event.CHAT_ON_NEW_MESSAGE, message);
                    }
                });

        // event to automatically connect/disconnect
        MblEventCenter.addListener(new MblStrongEventListener() {
            @Override
            public void onEvent(Object sender, String name, Object... args) {
                if (name == MblCommonEvents.NETWORK_ON) {
                    if (MblUtils.isAppInForeGround()) {
                        connect();
                    }
                }
                if (name == MblCommonEvents.NETWORK_OFF) {
                    disconnect();
                }
                if (name == MblCommonEvents.GO_TO_FOREGROUND) {
                    connect();
                }
                if (name == MblCommonEvents.GO_TO_BACKGROUND) {
                    disconnect();
                }
                if (name == Event.REGISTER_SUCCESS) {
                    connect();
                }
            }
        }, new String[] {
                MblCommonEvents.NETWORK_ON,
                MblCommonEvents.NETWORK_OFF,
                MblCommonEvents.GO_TO_FOREGROUND,
                MblCommonEvents.GO_TO_BACKGROUND,
                Event.REGISTER_SUCCESS
        });
    }

    public static String getUserId() {
        if (sUserId == null) {
            sUserId = MblUtils.getPrefs().getString(PREF_USER_ID, null);
        }
        return sUserId;
    }

    public static void setUserId(String userId) {
        MblUtils.getPrefs().edit().putString(PREF_USER_ID, userId).commit();
        sUserId = userId;
    }

    public static List<Room> getAllRooms() {
        return new ArrayList<>(sRooms);
    }

    public static Message getRoomLatestMessage(String roomId) {
        List<Message> messages = sRoomMessages.get(roomId);
        if (!MblUtils.isEmpty(messages)) {
            return messages.get(0);
        } else {
            return null;
        }
    }

    public static void getRoomAllMessages(final String roomId, final GetManyMessagesCallback callback) {
        ChatSdk.getInstance().getRoomMessages(roomId, 0, new GetManyMessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                sRoomMessages.put(roomId, new ArrayList<>(messages));
                if (callback != null) {
                    callback.onSuccess(messages);
                }
            }

            @Override
            public void onError(int err) {
                if (callback != null) {
                    callback.onError(err);
                }
            }
        });
    }

    public static void connect() {
        if (sConnecting || sConnected) {
            return;
        }
        if (getUserId() == null) {
            return;
        }
        sConnecting = true;
        ChatSdk.getInstance().connect();
    }

    public static void disconnect() {
        if (!sConnected) {
            return;
        }
        ChatSdk.getInstance().disconnect();
    }

    public static void createRoom(List<String> userIds, String name, final IdCallback callback) {
        ChatSdk.getInstance().createRoom(
                userIds.toArray(new String[userIds.size()]),
                name,
                callback);
    }

    public static void createMessage(String roomId, String body, IdCallback callback) {
        ChatSdk.getInstance().createMessage(roomId, body, callback);
    }

    private static void login() {
        ChatSdk.getInstance().login(getUserId(), new LoginCallback() {

            @Override
            public void onSuccess(List<Room> rooms) {
                sRooms.clear();
                sRooms.addAll(rooms);
                sRoomMessages.clear();

                MblSerializer serializer = new MblSerializer();
                MblSerializer.Task[] tasks = new MblSerializer.Task[rooms.size()];
                for (int i = 0; i < rooms.size(); i++) {
                    final Room r = rooms.get(i);
                    tasks[i] = new MblSerializer.Task() {
                        @Override
                        public void run(final Runnable finishCallback) {
                            ChatSdk.getInstance().getRoomMessages(r.getId(), 1, new GetManyMessagesCallback() {
                                @Override
                                public void onSuccess(List<Message> messages) {
                                    sRoomMessages.put(r.getId(), messages);
                                    finishCallback.run();
                                }

                                @Override
                                public void onError(int err) {
                                    Log.e(TAG, "Failed to get latest message for room: " + r.getId());
                                    finishCallback.run();
                                }
                            });
                        }
                    };
                }
                serializer.run(tasks);
                serializer.run(new MblSerializer.Task() {
                    @Override
                    public void run(Runnable finishCallback) {
                        MblEventCenter.postEvent(null, Event.CHAT_LOGIN_SUCCESS);
                        finishCallback.run();
                    }
                });

            }

            @Override
            public void onError(int err) {
                Log.e(TAG, "Failed to login");
                MblEventCenter.postEvent(null, Event.CHAT_LOGIN_ERROR, err);
            }
        });
    }
}
