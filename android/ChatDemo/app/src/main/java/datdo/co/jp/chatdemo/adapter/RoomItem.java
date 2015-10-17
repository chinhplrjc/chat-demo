package datdo.co.jp.chatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.datdo.mobilib.adapter.MblUniversalItem;
import com.datdo.mobilib.util.MblUtils;

import datdo.co.jp.chatdemo.R;
import datdo.co.jp.chatdemo.TalkActivity;
import datdo.co.jp.chatdemo.sdk.Message;
import datdo.co.jp.chatdemo.sdk.Room;
import datdo.co.jp.chatdemo.util.ChatUtil;
import datdo.co.jp.chatdemo.util.Util;

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
        // name
        ((TextView)view.findViewById(R.id.name_text)).setText(mRoom.getName());

        // member count
        ((TextView)view.findViewById(R.id.member_count_text)).setText(
                MblUtils.getCurrentContext().getString(R.string.xxx_members, mRoom.getUserIds().length));

        // latest message
        TextView latestMessageBodyText = (TextView) view.findViewById(R.id.latest_message_body_text);
        TextView latestMessageTimeText = (TextView) view.findViewById(R.id.latest_message_time_text);
        Message latestMessage = ChatUtil.getRoomLatestMessage(mRoom.getId());
        if (latestMessage != null) {
            latestMessageBodyText.setVisibility(View.VISIBLE);
            latestMessageTimeText.setVisibility(View.VISIBLE);
            latestMessageBodyText.setText(latestMessage.getBody());
            latestMessageTimeText.setText(Util.getTimeString(latestMessage.getTime()));
        } else {
            latestMessageBodyText.setVisibility(View.INVISIBLE);
            latestMessageTimeText.setVisibility(View.INVISIBLE);
        }

        // click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalkActivity.start(mRoom);
            }
        });
    }
}
