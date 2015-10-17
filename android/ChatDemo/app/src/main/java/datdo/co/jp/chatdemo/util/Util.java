package datdo.co.jp.chatdemo.util;

import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.util.ChatUtil;

/**
 * Created by dat on 2015/10/18.
 */
public class Util {

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM dd");
    private static SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm");

    public static String getTimeString(long time) {
        if (DateUtils.isToday(time)) {
            return sTimeFormat.format(new Date(time));
        } else {
            return sDateFormat.format(new Date(time));
        }
    }

    public static boolean isMine(Message m) {
        return TextUtils.equals(m.getUserId(), ChatUtil.getUserId());
    }
}
