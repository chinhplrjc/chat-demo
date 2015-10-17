package datdo.co.jp.chatdemo.sdk;

import org.json.JSONObject;

/**
 * Created by dat on 2015/10/17.
 */
abstract class Callback {
    protected abstract void invoke(JSONObject json) throws Exception;
    public abstract void onError(int err);
}
