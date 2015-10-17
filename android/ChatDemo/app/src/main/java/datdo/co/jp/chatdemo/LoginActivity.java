package datdo.co.jp.chatdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblStrongEventListener;
import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import datdo.co.jp.chatdemo.sdk.Error;
import datdo.co.jp.chatdemo.util.ChatUtil;
import datdo.co.jp.chatdemo.util.Event;

/**
 * Created by dat on 2015/10/17.
 */
public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // nav bar
        setNavBarTitle(getString(R.string.login));

        // login button
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // register link
        findViewById(R.id.register_link_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // justify UI
        MblViewUtil.fixScrollViewContentHeight((ScrollView)findViewById(R.id.scroll));
        MblViewUtil.makeEditTextAutoScrollOnFocused(getDecorView(), (ScrollView) findViewById(R.id.scroll));

        // events
        MblEventCenter.addListener(this, Event.CHAT_LOGIN_SUCCESS);
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        super.onEvent(sender, name, args);
        if (name == Event.CHAT_LOGIN_SUCCESS) {
            finish();
        }
    }

    private void login() {

        // extract user id
        String userId = MblViewUtil.extractText((EditText)findViewById(R.id.user_id_edit));
        if (MblUtils.isEmpty(userId)) {
            return;
        }

        // login
        ChatUtil.setUserId(userId);
        MblEventCenter.addListener(new MblStrongEventListener() {
            @Override
            public void onEvent(Object sender, String name, Object... args) {
                int err = (Integer) args[0];
                if (err == Error.ERR_INVALID_USER_ID) {
                    MblUtils.showAlert(R.string.error, R.string.err_invalid_user_id, null);
                }
                terminate();
            }
        }, new String[]{
                Event.CHAT_LOGIN_ERROR
        });
        ChatUtil.connect();
    }
}
