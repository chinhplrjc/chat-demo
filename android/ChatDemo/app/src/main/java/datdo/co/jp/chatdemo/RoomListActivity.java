package datdo.co.jp.chatdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.datdo.mobilib.event.MblEventCenter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import datdo.co.jp.chatdemo.adapter.RoomAdapter;
import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.sdk.Room;
import datdo.co.jp.chatdemo.util.ChatUtil;
import datdo.co.jp.chatdemo.util.Event;

/**
 * Created by dat on 2015/10/17.
 */
public class RoomListActivity extends BaseActivity {

    private RoomAdapter mRoomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        // nav bar
        setNavBarTitle(getString(R.string.room_list));
        setNavBarRightButton(getString(R.string.create), new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(RoomListActivity.this, CreateRoomActivity.class));
            }
        });

        // list
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mRoomAdapter = new RoomAdapter(this));

        // event
        MblEventCenter.addListener(this, new String[] {
                Event.CHAT_LOGIN_SUCCESS,
                Event.CHAT_ON_NEW_ROOM,
                Event.CHAT_ON_NEW_MESSAGE
        });
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        super.onEvent(sender, name, args);
        if (name == Event.CHAT_LOGIN_SUCCESS
                || name == Event.CHAT_ON_NEW_ROOM
                || name == Event.CHAT_ON_NEW_MESSAGE) {
            reload();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ChatUtil.getUserId() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void reload() {
        List<Room> rooms = ChatUtil.getAllRooms();
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                Message lm1 = ChatUtil.getRoomLatestMessage(r1.getId());
                Message lm2 = ChatUtil.getRoomLatestMessage(r1.getId());
                if (lm1 == null && lm2 == null) {
                    return 0;
                }
                if (lm1 == null) {
                    return 1;
                }
                if (lm2 == null) {
                    return -1;
                }
                if (lm1.getTime() > lm2.getTime()) {
                    return -1;
                }
                if (lm1.getTime() < lm2.getTime()) {
                    return 1;
                }
                return 0;
            }
        });
        mRoomAdapter.changeDataForRoomList(rooms);
    }
}
