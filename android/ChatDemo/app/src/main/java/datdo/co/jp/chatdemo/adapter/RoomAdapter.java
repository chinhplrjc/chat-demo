package datdo.co.jp.chatdemo.adapter;

import android.content.Context;

import com.datdo.mobilib.adapter.MblUniversalAdapter;

import java.util.List;

import datdo.co.jp.chatdemo.sdk.Room;

/**
 * Created by dat on 2015/10/17.
 */
public class RoomAdapter extends MblUniversalAdapter {

    public RoomAdapter(Context context) {
        super(context);
    }

    public void changeDataForRoomList(final List<Room> rooms) {
        changeDataSafely(new Runnable() {
            @Override
            public void run() {
                getData().clear();
                for (Room r : rooms) {
                    getData().add(new RoomItem(r));
                }
                notifyDataSetChanged();
            }
        });
    }
}
