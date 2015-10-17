package datdo.co.jp.chatdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import java.util.Collections;
import java.util.List;

import datdo.co.jp.chatdemo.adapter.MessageAdapter;
import datdo.co.jp.chatdemo.sdk.ChatSdk;
import datdo.co.jp.chatdemo.sdk.ChatSdk.GetManyMessagesCallback;
import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.sdk.Room;
import datdo.co.jp.chatdemo.util.ChatUtil;
import datdo.co.jp.chatdemo.util.Event;
import datdo.co.jp.chatdemo.util.Util;

/**
 * Created by dat on 2015/10/18.
 */
public class TalkActivity extends BaseActivity {

    private static final String EXTRA_ROOM = "room";

    private Room mRoom;
    private ListView mListView;
    private MessageAdapter mMessageAdapter;
    private boolean mAtBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        // get room from common bundle
        mRoom = (Room) MblUtils.removeFromCommonBundle(getIntent().getStringExtra(EXTRA_ROOM));

        // nav bar
        setNavBarTitle(mRoom.getName());
        setNavBarBackButton();
        setNavBarRightButton(
                getString(R.string.xxx_members, mRoom.getUserIds().length),
                new Runnable() {
                    @Override
                    public void run() {
                        showMembers();
                    }
                });

        // list
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mMessageAdapter = new MessageAdapter(this));
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mAtBottom = firstVisibleItem + visibleItemCount >= totalItemCount;
            }
        });

        // send button
        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });

        // events
        MblEventCenter.addListener(this, new String[] {
                Event.CHAT_LOGIN_SUCCESS,
                Event.CHAT_ON_NEW_MESSAGE
        });

        // load all messages
        loadAllMessages();
    }

    private void loadAllMessages() {
        ChatUtil.getRoomAllMessages(mRoom.getId(), new GetManyMessagesCallback() {

            @Override
            public void onSuccess(List<Message> messages) {
                Collections.reverse(messages);
                mMessageAdapter.changeDataForTalk(messages);
                MblUtils.scrollListViewToBottom(mListView);
            }

            @Override
            public void onError(int err) {
                MblUtils.showAlert(R.string.error, R.string.err_failed_try_later, new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });
    }

    private void send() {

        // extract body
        final EditText bodyEdit = (EditText)findViewById(R.id.body_edit);
        String body = MblViewUtil.extractText(bodyEdit);
        if (MblUtils.isEmpty(body)) {
            return;
        }

        // send
        ChatUtil.createMessage(mRoom.getId(), body, new ChatSdk.IdCallback() {
            @Override
            public void onSuccess(String id) {
                bodyEdit.setText(null);
            }

            @Override
            public void onError(int err) {
                MblUtils.showAlert(R.string.error, R.string.err_failed_try_later, null);
            }
        });
    }

    private void showMembers() {
        new AlertDialog.Builder(this)
                .setItems(mRoom.getUserIds(), null)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        super.onEvent(sender, name, args);
        if (name == Event.CHAT_LOGIN_SUCCESS) {
            loadAllMessages();
        }
        if (name == Event.CHAT_ON_NEW_MESSAGE) {
            Message message = (Message) args[0];
            if (!TextUtils.equals(message.getRoomId(), mRoom.getId())) {
                return;
            }
            boolean atBottom = mAtBottom;
            mMessageAdapter.appendMessage(message);
            if (Util.isMine(message) || atBottom) {
                MblUtils.scrollListViewToBottom(mListView);
            }
        }
    }

    public static void start(Room room) {
        Context context = MblUtils.getCurrentContext();
        Intent intent = new Intent(context, TalkActivity.class);
        intent.putExtra(EXTRA_ROOM, MblUtils.putToCommonBundle(room));
        context.startActivity(intent);
    }
}
