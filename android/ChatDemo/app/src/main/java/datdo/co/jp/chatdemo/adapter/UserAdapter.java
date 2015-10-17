package datdo.co.jp.chatdemo.adapter;

import android.content.Context;

import com.datdo.mobilib.adapter.MblUniversalAdapter;
import com.datdo.mobilib.adapter.MblUniversalItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dat on 2015/10/17.
 */
public class UserAdapter extends MblUniversalAdapter {

    public UserAdapter(Context context) {
        super(context);
    }

    public void changeDataForCreateRoom(final List<String> usersIds) {
        changeDataSafely(new Runnable() {
            @Override
            public void run() {
                getData().clear();
                for (String id : usersIds) {
                    getData().add(new UserItem(id));
                }
                notifyDataSetChanged();
            }
        });
    }

    public List<String> getSelectedUsers() {
        List<String> ret = new ArrayList<>();
        for (MblUniversalItem item : getData()) {
            UserItem userItem = (UserItem) item;
            if (userItem.mChecked) {
                ret.add(userItem.mUserId);
            }
        }
        return ret;
    }
}
