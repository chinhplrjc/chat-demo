package datdo.co.jp.chatdemo;

import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseApplication;
import com.datdo.mobilib.util.MblViewUtil;

import datdo.co.jp.chatdemo.util.ChatUtil;

/**
 * Created by dat on 2015/10/17.
 */
public class Application extends MblBaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // init chat
        ChatUtil.initialize();

        // global view iterator
        MblViewUtil.setGlobalViewProcessor(new MblViewUtil.MblInterateViewDelegate() {

            StateListDrawable stateListDrawable = new StateListDrawable();

            @Override
            public void process(View view) {

                // text
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;

                    // remove dummy text
                    String text = tv.getText().toString();
                    if (text.matches("\\{.+\\}")) {
                        tv.setText(null);
                    }
                }

                // listview
                if (view instanceof ListView) {
                    ListView lv = (ListView) view;
                    if (lv.getSelector() != stateListDrawable) {
                        lv.setSelector(stateListDrawable);
                    }
                }
            }
        });
    }

    @Override
    public void onVersionCodeChanged(int oldVersionCode, int newVersionCode) {}

    @Override
    public void onVersionNameChanged(String oldVersionName, String newVersionName) {}
}
