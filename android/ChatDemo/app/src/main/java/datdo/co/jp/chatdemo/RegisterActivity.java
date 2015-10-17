package datdo.co.jp.chatdemo;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblStrongEventListener;
import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import datdo.co.jp.chatdemo.sdk.*;
import datdo.co.jp.chatdemo.util.ChatUtil;
import datdo.co.jp.chatdemo.util.Event;
import datdo.co.jp.chatdemo.sdk.Error;

/**
 * Created by dat on 2015/10/17.
 */
public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // nav bar
        setNavBarTitle(getString(R.string.register));
        setNavBarBackButton();

        // login button
        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        // justify UI
        MblViewUtil.fixScrollViewContentHeight((ScrollView) findViewById(R.id.scroll));
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

    private void register() {

        // extract user id
        final String userId = MblViewUtil.extractText((EditText)findViewById(R.id.user_id_edit));
        if (MblUtils.isEmpty(userId)) {
            return;
        }

        // call register request
        ChatSdk.getInstance().register(userId, new ChatSdk.SimpleCallback() {
            @Override
            public void onSuccess() {
                login(userId);
            }

            @Override
            public void onError(int err) {
                if (err == Error.ERR_USER_ALREADY_EXIST) {
                    MblUtils.showAlert(R.string.error, R.string.err_user_already_exist, null);
                }
            }
        });
    }

    private void login(String userId) {
        ChatUtil.setUserId(userId);
        ChatUtil.connect();
    }
}
