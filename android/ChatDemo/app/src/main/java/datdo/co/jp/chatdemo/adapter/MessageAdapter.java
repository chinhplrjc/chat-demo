package datdo.co.jp.chatdemo.adapter;

import android.content.Context;

import com.datdo.mobilib.adapter.MblUniversalAdapter;

import java.util.List;

import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.util.Util;

/**
 * Created by dat on 2015/10/18.
 */
public class MessageAdapter extends MblUniversalAdapter {

    public MessageAdapter(Context context) {
        super(context);
    }

    public void changeDataForTalk(final List<Message> messages) {
        changeDataSafely(new Runnable() {
            @Override
            public void run() {
                getData().clear();
                for (Message m : messages) {
                    if (Util.isMine(m)) {
                        getData().add(new MessageRightItem(m));
                    } else {
                        getData().add(new MessageLeftItem(m));
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void appendMessage(final Message m) {
        changeDataSafely(new Runnable() {
            @Override
            public void run() {
                if (Util.isMine(m)) {
                    getData().add(new MessageRightItem(m));
                } else {
                    getData().add(new MessageLeftItem(m));
                }
                notifyDataSetChanged();
            }
        });
    }
}
