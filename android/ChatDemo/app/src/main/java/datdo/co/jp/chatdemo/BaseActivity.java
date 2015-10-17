package datdo.co.jp.chatdemo;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseActivity;
import com.datdo.mobilib.event.MblEventListener;

/**
 * Created by dat on 2015/10/17.
 */
public class BaseActivity extends MblBaseActivity implements MblEventListener {

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

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        // placeholder
    }
}
