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
public class MessageRightItem implements MblUniversalItem {

    Message mMessage;

    MessageRightItem(Message message) {
        mMessage = message;
    }

    @Override
    public View create(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.cell_message_right, null);
    }

    @Override
    public void display(View view) {
        // time
        ((TextView)view.findViewById(R.id.time_text)).setText(Util.getTimeString(mMessage.getTime()));

        // body
        ((TextView)view.findViewById(R.id.body_text)).setText(mMessage.getBody());
    }
}
