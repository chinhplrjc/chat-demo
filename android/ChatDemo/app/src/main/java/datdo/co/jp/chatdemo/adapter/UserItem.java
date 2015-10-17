package datdo.co.jp.chatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.datdo.mobilib.adapter.MblUniversalItem;

import datdo.co.jp.chatdemo.R;

/**
 * Created by dat on 2015/10/17.
 */
class UserItem implements MblUniversalItem {

    String mUserId;
    boolean mChecked;

    UserItem(String userId) {
        mUserId = userId;
    }

    @Override
    public View create(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.cell_user, null);
    }

    @Override
    public void display(View view) {

        // user id
        ((TextView) view.findViewById(R.id.user_id_text)).setText(mUserId);

        // check box
        displayCheck(view);

        // click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChecked = !mChecked;
                displayCheck(view);
            }
        });
    }

    private void displayCheck(View view) {
        view.findViewById(R.id.check_image).setVisibility(mChecked ? View.VISIBLE : View.INVISIBLE);
    }
}
