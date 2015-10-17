package datdo.co.jp.chatdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import java.util.List;

import datdo.co.jp.chatdemo.adapter.UserAdapter;
import datdo.co.jp.chatdemo.sdk.ChatSdk;
import datdo.co.jp.chatdemo.util.ChatUtil;

/**
 * Created by dat on 2015/10/17.
 */
public class CreateRoomActivity extends BaseActivity {

    private UserAdapter mUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        // nav bar
        setNavBarTitle(getString(R.string.create_room));
        setNavBarBackButton();

        // buttons
        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
            }
        });

        // list
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mUserAdapter = new UserAdapter(this));

        // load users
        ChatSdk.getInstance().getAllUsers(new ChatSdk.GetAllUsersCallback() {

            @Override
            public void onSuccess(List<String> userIds) {
                userIds.remove(ChatUtil.getUserId());
                mUserAdapter.changeDataForCreateRoom(userIds);
            }

            @Override
            public void onError(int err) {
            }
        });
    }

    private void create() {

        // extract name
        EditText nameEdit = (EditText)findViewById(R.id.name_edit);
        String name = MblViewUtil.extractText(nameEdit);
        if (MblUtils.isEmpty(name)) {
            MblUtils.showToast(R.string.err_please_input_room_name, Toast.LENGTH_SHORT);
            nameEdit.requestFocus();
            return;
        }

        // get list of selected users
        List<String> selectedUsers = mUserAdapter.getSelectedUsers();
        if (selectedUsers.isEmpty()) {
            MblUtils.showToast(R.string.err_please_select_user, Toast.LENGTH_SHORT);
            return;
        }

        // create room
        ChatUtil.createRoom(selectedUsers, name, new ChatSdk.IdCallback() {
            @Override
            public void onSuccess(String id) {
                finish();
            }

            @Override
            public void onError(int err) {
                MblUtils.showAlert(R.string.error, R.string.err_failed_try_later, null);
            }
        });
    }
}
