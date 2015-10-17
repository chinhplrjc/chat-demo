package datdo.co.jp.chatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.datdo.mobilib.adapter.MblUniversalItem;

import datdo.co.jp.chatdemo.R;
import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.util.Util;

/**
 * Created by dat on 2015/10/18.
 */
public class MessageLeftItem implements MblUniversalItem {

    Message mMessage;

    MessageLeftItem(Message message) {
        mMessage = message;
    }

    @Override
    public View create(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.cell_message_left, null);
    }

    @Override
    public void display(View view) {

        // user id
        ((TextView)view.findViewById(R.id.user_id_text)).setText(mMessage.getUserId());

        // time
        ((TextView)view.findViewById(R.id.time_text)).setText(Util.getTimeString(mMessage.getTime()));

        // body
        ((TextView)view.findViewById(R.id.body_text)).setText(mMessage.getBody());
    }
}
