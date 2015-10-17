package datdo.co.jp.chatdemo.sdk;

import org.json.JSONObject;

/**
 * Created by dat on 2015/10/16.
 */
public class Room {

    private String mId;
    private String mName;
    private String[] mUserIds;

    static Room fromJSON(JSONObject json) {
        Room r = new Room();
        r.mId = json.optString("id");
        r.mName = json.optString("name");
        r.mUserIds = (String[]) Util.toArray(json.optJSONArray("users"));
        return r;
    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String[] getUserIds() {
        return mUserIds;
    }

    public void setUserIds(String[] userIds) {
        mUserIds = userIds;
    }
}
