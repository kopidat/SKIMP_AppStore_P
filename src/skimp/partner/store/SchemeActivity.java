package skimp.partner.store;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import m.client.android.library.core.common.CommonLibHandler;
import m.client.android.library.core.managers.ActivityHistoryManager;
import m.client.android.library.core.utils.CommonLibUtil;
import m.client.android.library.core.utils.PLog;
import m.client.android.library.core.view.MainActivity;
import skimp.partner.store.common.Utils;


/**
 * scheme 으로 실행할 투명 Activity
 * 개별앱에서 라이브러리를 통해 로그인이나 개별앱 업데이트를 위해 스토어앱을 실행했을때 전달한 data 저장
 * ("scheme" : "개별앱스킴", "loginType" : "login" or "update")
 */

public class SchemeActivity extends Activity {
	
	private static final String TAG = SchemeActivity.class.getSimpleName();
	private CommonLibHandler commLibHandle = CommonLibHandler.getInstance();

    private static final String EXTRA_KEY_scheme = "scheme";
    private static final String EXTRA_KEY_loginType = "type";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(Bundle savedInstanceState) = " + savedInstanceState);

    	super.onCreate(savedInstanceState);

        final MainActivity topAct = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
        Log.e(TAG, "topAct = " + topAct);

        Log.e(TAG, "commLibHandle.getIsProcessAppInit() = " + commLibHandle.getIsProcessAppInit());
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            String scheme = intent.getStringExtra(EXTRA_KEY_scheme);
            String loginType = intent.getStringExtra(EXTRA_KEY_loginType);;
            saveStartFromOtherAppData(scheme, loginType);

            Utils.debugUri(TAG, intent.getData());
        }

        Utils.debugIntent(TAG + " getIntent() = ", getIntent());

        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent(Intent intent) = " + intent);
        Utils.debugIntent(TAG + " intent = ", intent);
        Utils.debugIntent(TAG + " getIntent() = ", getIntent());
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy()");
        super.onDestroy();

        final MainActivity topAct = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
        Log.e(TAG, "topAct = " + topAct);

        if(topAct == null) {
            // delay줘야함. delay 안주면 타앱에서 scheme으로 실행시 SchemeActivity 한번 실행된 후
            // 이후 타앱에서 scheme으로 호출했을 때 SchemeActivity가 실행안됨.
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    commLibHandle.processAppInit(SchemeActivity.this);
                }
            }, 100);
        } else {
            callback();
        }
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause()");
        super.onPause();
    }

    public static void checkStartFromOtherApp() {
        if(needCallback()) {
            callback();
        }
    }

    private static boolean needCallback() {
        String scheme = CommonLibUtil.getVariable(EXTRA_KEY_scheme);
        if(TextUtils.isEmpty(scheme)) {
            return false;
        } else {
            return true;
        }
    }

    private static void callback() {
        Log.e(TAG, "callback()");

        final String scheme = CommonLibUtil.getVariable(EXTRA_KEY_scheme);
        final String loginType = CommonLibUtil.getVariable(EXTRA_KEY_loginType);

        final MainActivity topAct = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
        topAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String format = "javascript:%s('%s', '%s');";
                String script = String.format(format, "fn_storeopen", scheme, loginType);
                PLog.e(TAG, "script = " + script);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    topAct.getWebView().evaluateJavascript(script, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            PLog.i(TAG, "onReceiveValue(String value) = " + value);
                        }
                    });
                } else {
                    topAct.getWebView().loadUrl(script);
                }
            }
        });
        // callback 했으니 data 삭제
        removeStartFromOtherAppData();
    }

    private void saveStartFromOtherAppData(String scheme, String loginType) {
        CommonLibUtil.setVariable(EXTRA_KEY_scheme, scheme);
        CommonLibUtil.setVariable(EXTRA_KEY_loginType, loginType);
    }

    private static void removeStartFromOtherAppData() {
        CommonLibUtil.setVariable(EXTRA_KEY_scheme, "");
        CommonLibUtil.setVariable(EXTRA_KEY_loginType, "");
    }

}
