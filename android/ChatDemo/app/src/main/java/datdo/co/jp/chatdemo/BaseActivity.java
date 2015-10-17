package datdo.co.jp.chatdemo;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseActivity;
import com.datdo.mobilib.event.MblEventListener;
import com.datdo.mobilib.util.MblUtils;

/**
 * Created by dat on 2015/10/17.
 */
public class BaseActivity extends MblBaseActivity implements MblEventListener {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        MblUtils.focusNothing(this);
    }

    protected void setNavBarTitle(String title) {
        TextView middleText = (TextView) findViewById(R.id.nav_bar).findViewById(R.id.middle_text);
        middleText.setText(title);
    }

    protected void setNavBarBackButton() {
        Button leftButton = (Button) findViewById(R.id.nav_bar).findViewById(R.id.left_button);
        leftButton.setText(R.string.back);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    protected void setNavBarRightButton(String text, final Runnable action) {
        Button rightButton = (Button) findViewById(R.id.nav_bar).findViewById(R.id.right_button);
        rightButton.setText(text);
        if (action != null) {
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action.run();
                }
            });
        }
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        // placeholder
    }
}
