package datdo.co.jp.chatdemo.util;

/**
 * Created by dat on 2015/10/17.
 */
public class Event {
    public static final String REGISTER_SUCCESS     = Event.class + "#REGISTER_SUCCESS";

    public static final String CHAT_CONNECTED       = Event.class + "#CHAT_CONNECTED";
    public static final String CHAT_DISCONNECTED    = Event.class + "#CHAT_DISCONNECTED";
    public static final String CHAT_LOGIN_SUCCESS   = Event.class + "#CHAT_LOGIN_SUCCESS";
    public static final String CHAT_LOGIN_ERROR     = Event.class + "#CHAT_LOGIN_ERROR";
    public static final String CHAT_ON_JOIN         = Event.class + "#CHAT_ON_JOIN";
    public static final String CHAT_ON_MESSAGE      = Event.class + "#CHAT_ON_MESSAGE";
}
