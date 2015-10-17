package datdo.co.jp.chatdemo.sdk;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dat on 2015/10/16.
 */
class Util {

    public static Object[] toArray(JSONArray ja) {
        if (ja != null) {
            Object[] arr = new Object[ja.length()];
            for (int i = 0; i < ja.length(); i++){
                arr[i] = ja.opt(i);
            }
            return arr;
        } else {
            return new Object[] {};
        }
    }
}
