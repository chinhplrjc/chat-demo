package datdo.co.jp.chatdemo.sdk;

/**
 * Created by dat on 2015/10/17.
 */
public interface Listener {
    void onConnect();
    void onDisconnect();
    void onJoin(String roomId);
    void onMessage(Message message);
}
