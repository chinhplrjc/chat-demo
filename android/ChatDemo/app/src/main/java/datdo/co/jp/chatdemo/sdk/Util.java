package datdo.co.jp.chatdemo.sdk;

import android.os.Handler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dat on 2015/10/16.
 */
class Util {

    public static String[] toStringArray(JSONArray ja) {
        if (ja != null) {
            String[] arr = new String[ja.length()];
            for (int i = 0; i < ja.length(); i++){
                arr[i] = ja.optString(i);
            }
            return arr;
        } else {
            return new String[] {};
        }
    }

    public static Runnable repeatDelayed(final Handler handler, final long delayMillis, final Runnable action) {

        if (action == null || delayMillis <= 0) {
            return new Runnable() {
                @Override
                public void run() {}
            };
        }

        final Runnable hookedAction = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, delayMillis);
                action.run();
            }
        };

        handler.postDelayed(hookedAction, delayMillis);

        return new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(hookedAction);
            }
        };
    }
}
