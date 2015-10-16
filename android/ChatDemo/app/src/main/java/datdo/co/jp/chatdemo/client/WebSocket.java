package datdo.co.jp.chatdemo.client;

import android.os.Build;
import android.util.Log;

import com.datdo.mobilib.util.MblUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Handler;

/**
 * Created by dat on 2015/10/15.
 */
class WebSocket {

    private static final String TAG = MblUtils.getTag(WebSocket.class);

    public interface Listener {
        void onConnect();
        void onDisconnect();
        void onJoin(String roomId, String userId);
        void onLeave(String roomId, String userId);
        void onMessage(String userId, String message);
    }

    private WebSocketClient mWebSocketClient;

    WebSocket(String uri, Handler handler, Listener listener) {


        mWebSocketClient = new WebSocketClient(URI.create(uri)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.d(TAG, "onOpen");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                Log.d(TAG, "onMessage: s=" + s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.d(TAG, "onClose: i=" + i + ", s=" + s + ", b=" + b);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onError: s=" + e.getMessage());
            }
        };
    }

    void connect() {
        mWebSocketClient.connect();
    }

    void disconnect() {
        mWebSocketClient.close();
    }

    void send(String data) {
        mWebSocketClient.send(data);
    }
}
