package datdo.co.jp.chatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.datdo.mobilib.adapter.MblUniversalItem;

import datdo.co.jp.chatdemo.R;
import datdo.co.jp.chatdemo.sdk.Room;

/**
 * Created by dat on 2015/10/17.
 */
class RoomItem implements MblUniversalItem {

    Room mRoom;

    RoomItem(Room room) {
        mRoom = room;
    }

    @Override
    public View create(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.cell_room, null);
    }

    @Override
    public void display(View view) {
        
    }
}
