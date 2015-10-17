package datdo.co.jp.chatdemo.sdk;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.datdo.mobilib.api.MblApi;
import com.datdo.mobilib.api.MblApi.MblApiCallback;
import com.datdo.mobilib.api.MblApi.Method;
import com.datdo.mobilib.api.MblRequest;
import com.datdo.mobilib.api.MblResponse;

import junit.framework.Assert;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dat on 2015/10/16.
 */
public class ChatSdk {

    private static final String TAG = ChatSdk.class.getSimpleName();

    public static ChatSdk           sInstance;
    private static Handler          sMainThreadHandler = new Handler(Looper.getMainLooper());
    private WebSocketClient         mWebSocketClient;
    private AtomicInteger           mSeq;
    private Map<Integer, Callback>  mCallbacks = new ConcurrentHashMap<>();
    private Listener                mListener;
    private Handler                 mHandler;
    private String                  mHttpServer;
    private String                  mWsServer;

    public static void initialize(String httpServer, String wsServer, Listener listener) {
        sInstance = new ChatSdk(httpServer, wsServer, listener);
    }

    public static ChatSdk getInstance() {
        return sInstance;
    }

    private ChatSdk(String httpServer, String wsServer, Listener listener) {

        // create thread to process JSON
        HandlerThread thread = new HandlerThread("ChatSdk");
        thread.start();
        mHandler = new Handler(thread.getLooper());

        // request ID
        mSeq = new AtomicInteger();

        // servers
        mHttpServer = httpServer;
        mWsServer = wsServer;

        // listener
        mListener = listener;
    }

    private class _WebSocketClient extends WebSocketClient {

        public _WebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            Log.d(TAG, "onOpen");
            onConnect();
        }

        @Override
        public void onMessage(final String s) {
            Log.d(TAG, "onMessage: s=" + s);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONObject json = new JSONObject(s);
                        int tag = json.optInt("tag", -1);
                        if (tag != -1) {
                            onCallback(tag, json);
                        } else {
                            String event = json.getString("event");
                            if (TextUtils.equals(event, "on_join")) {
                                ChatSdk.this.onJoin(json);
                            }
                            else if (TextUtils.equals(event, "on_message")) {
                                ChatSdk.this.onMessage(json);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            });
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            Log.d(TAG, "onClose: i=" + i + ", s=" + s + ", b=" + b);
            onDisconnect();
        }

        @Override
        public void onError(Exception e) {
            Log.d(TAG, "onError: s=" + e.getMessage());
            onDisconnect();
        }
    }

    private void onConnect() {
        if (mListener == null) {
            return;
        }
        sMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onConnect();
            }
        });
    }

    private void onDisconnect() {
        if (mListener == null) {
            return;
        }
        sMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onDisconnect();
            }
        });
    }

    private void onCallback(int tag, JSONObject json) throws Exception {
        final Callback callback = mCallbacks.get(tag);
        if (callback == null) {
            return;
        }
        final int err = json.getInt("err");
        if (err == 0) {
            try {
                callback.invoke(json);
            } catch (Exception e) {
                Log.e(TAG, "", e);
                sMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(Error.ERR_INTERNAL);
                    }
                });
            }
        } else {
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(err);
                }
            });
        }
    }

    private void onJoin(JSONObject json) throws JSONException {
        if (mListener == null) {
            return;
        }
        final String roomId = json.getString("room_id");
        sMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onJoin(roomId);
            }
        });
    }

    private void onMessage(JSONObject json) throws JSONException {
        if (mListener == null) {
            return;
        }
        final Message message = Message.fromJSON(json);
        sMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onMessage(message);
            }
        });
    }

    public void register(String userId, final SimpleCallback callback) {
        MblApi.run(new MblRequest()
                .setMethod(Method.POST)
                .setUrl(mHttpServer + "/api/register")
                .setParams("user_id", userId)
                .setCallbackHandler(mHandler)
                .setCallback(getApiCallback(callback)));
    }

    public void connect() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
        mWebSocketClient = new _WebSocketClient(URI.create(mWsServer));
        mWebSocketClient.connect();
    }

    public void disconnect() {
        mWebSocketClient.close();
    }

    public void login(String userId, LoginCallback callback) {
        send("login", callback, "user_id", userId);
    }

    public void createRoom(String[] userIds, IdCallback callback) {
        send("create_room", callback, "users", userIds);
    }

    public void getRoom(String roomId, GetRoomCallback callback) {
        send("get_room", callback, "room_id", roomId);
    }

    public void createMessage(String roomId, String body, IdCallback callback) {
        send("create_message", callback, "room_id", roomId, "body", body);
    }

    public void getRoomMessages(String roomId, int limit, GetManyMessagesCallback callback) {
        send("get_room_messages", callback, "room_id", roomId, "limit", limit);
    }

    public static abstract class SimpleCallback extends Callback {

        public abstract void onSuccess();

        @Override
        protected void invoke(JSONObject json) throws Exception {
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess();
                }
            });
        }
    }

    public static abstract class IdCallback extends Callback {

        public abstract void onSuccess(String id);

        @Override
        protected void invoke(JSONObject json) throws Exception {
            final String id = json.getString("id");
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(id);
                }
            });
        }
    }

    public static abstract class LoginCallback extends Callback {

        public abstract void onSuccess(List<Room> rooms);

        @Override
        protected void invoke(JSONObject json) throws Exception {
            JSONArray ja = json.getJSONArray("rooms");
            final List<Room> rooms = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                rooms.add(Room.fromJSON(ja.getJSONObject(i)));
            }
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(rooms);
                }
            });
        }
    }

    public static abstract class GetRoomCallback extends Callback {

        public abstract void onSuccess(Room room);

        @Override
        protected void invoke(JSONObject json) throws Exception {
            final Room room = Room.fromJSON(json);
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(room);
                }
            });
        }
    }

    public static abstract class GetManyMessagesCallback extends Callback {

        public abstract void onSuccess(List<Message> messages);

        @Override
        protected void invoke(JSONObject json) throws Exception {
            JSONArray ja = json.getJSONArray("messages");
            final List<Message> messages = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                messages.add(Message.fromJSON(ja.getJSONObject(i)));
            }
            sMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(messages);
                }
            });
        }
    }

    private void send(String action, final Callback callback, Object... params) {
        try {
            Assert.assertTrue(params.length % 2 == 0);
            int seq = mSeq.incrementAndGet();
            JSONObject data = new JSONObject();
            data.put("tag", seq);
            data.put("action", action);
            for (int i = 0; i < params.length; i += 2) {
                String key = (String) params[i];
                Object val = params[i+1];
                if (val instanceof Object[]) {
                    Object[] arr = (Object[]) val;
                    JSONArray ja = new JSONArray();
                    for (int k = 0; k < arr.length; k++) {
                        ja.put(k, arr[k]);
                    }
                    val = ja;
                }
                data.put(key, val);
            }
            mCallbacks.put(seq, callback);
            mWebSocketClient.send(data.toString());
        } catch (Exception e) {
            Log.e(TAG, "", e);
            if (callback != null) {
                sMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(Error.ERR_INTERNAL);
                    }
                });
            }
        }
    }

    private MblApiCallback getApiCallback(final Callback callback) {
        return new MblApiCallback() {
            @Override
            public void onSuccess(MblResponse response) {
                if (callback == null) {
                    return;
                }
                try {
                    JSONObject json = new JSONObject(new String(response.getData()));
                    final int err = json.getInt("err");
                    if (err == 0) {
                        callback.invoke(json);
                    } else {
                        sMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(err);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onFailure(MblResponse response) {
                if (callback == null) {
                    return;
                }
                sMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(Error.ERR_INTERNAL);
                    }
                });
            }
        };
    }
}
