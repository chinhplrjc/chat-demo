package datdo.co.jp.chatdemo.sdk;

import org.json.JSONObject;

/**
 * Created by dat on 2015/10/16.
 */
public class Message {

    private String mId;
    private String mBody;
    private long mTime;
    private String mRoomId;
    private String mUserId;

    static Message fromJSON(JSONObject json) {
        Message m = new Message();
        m.mId = json.optString("id");
        m.mBody = json.optString("body");
        m.mTime = json.optLong("time") * 1000;
        m.mRoomId = json.optString("room_id");
        m.mUserId = json.optString("user_id");
        return m;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }
}
