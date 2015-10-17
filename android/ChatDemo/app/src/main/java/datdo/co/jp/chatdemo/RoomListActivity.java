package datdo.co.jp.chatdemo;

import android.content.Intent;
import android.os.Bundle;

import com.datdo.mobilib.event.MblEventCenter;

import datdo.co.jp.chatdemo.util.ChatUtil;

/**
 * Created by dat on 2015/10/17.
 */
public class RoomListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        // nav bar
        setNavBarTitle(getString(R.string.room_list));
        setNavBarBackButton();

        // event
        MblEventCenter.addListener(this, new String[] {

        });
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        super.onEvent(sender, name, args);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ChatUtil.getUserId() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
