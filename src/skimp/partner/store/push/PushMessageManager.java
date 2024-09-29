package skimp.partner.store.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.webkit.ValueCallback;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.android.library.core.common.CommonLibHandler;
import m.client.android.library.core.managers.ActivityHistoryManager;
import m.client.android.library.core.utils.CommonLibUtil;
import m.client.android.library.core.utils.PLog;
import m.client.android.library.core.view.MainActivity;
import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;
import skimp.partner.store.implementation.ExtendApplication;

public class PushMessageManager extends Activity {
	private static final String TAG = PushMessageManager.class.getSimpleName();

	private CommonLibHandler mCommLibHandle = CommonLibHandler.getInstance();
	private PushManager mPushManager = PushManager.getInstance();



	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("PushMessageManager", "onCreate(Bundle savedInstanceState)");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent intent = getIntent();
		String notification = intent.getStringExtra("JSON");
		final String pushType = intent.getStringExtra("PUSH_TYPE");
		final String pushStatus = intent.getStringExtra("PUSH_STATUS");
		final String originData = intent.getStringExtra("ENCRYPT");
		Logger.e("================ encrypt ================");
		Logger.e(originData);
		Logger.e("================ !encrypt ================");

		JSONObject payload;
		try {
			payload = new JSONObject(notification);
			payload.put("ns", pushType);
			notification = payload.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mPushManager.setPushJsonData(this, notification);

		// 지오펜싱 서비스가 실행중이면 mCommLibHandle.g_strExtWNIClassPackageName null이 아니다
		// 따라서 topActivity == null로 앱 실행여부를 체크해야함.
		final MainActivity topActivity = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
		Log.e(TAG, "mCommLibHandle.g_strExtWNIClassPackageName = " + mCommLibHandle.g_strExtWNIClassPackageName);
		Log.e(TAG, "topActivity = " + topActivity);
		// 앱 실행중이 아님.
		if (mCommLibHandle.g_strExtWNIClassPackageName == null || topActivity == null) {
			// 앱이 실행중이 아니기 때문에 스토리지에 저장후
			// mCommLibHandle.processAppInit만 수행하고 Startup -> BaseActivity onPageFinished 에서 푸시 처리함.
			savePushDataToStorage(notification, pushType, "appoff");

			// 프로그램이 정상적으로 구동되지 않고 바로 실행되는 경우에는 프로그램 초기화 처리를 거쳐야 한다.
			// mCommLibHandle.processAppInit만 수행하고 Startup -> BaseActivity onPageFinished 에서 푸시 처리함.
			mCommLibHandle.processAppInit(this);
		} else {  // 앱 실행중
			savePushDataToStorage(notification, pushType, "");

			callback();
		}
		finish();
	}

	public static void checkStartFromPushNotiClick() {
		Log.e(TAG, "checkStartFromPushNotiClick()");
		if(needPushProcess()) {
			callback();
		}
	}

	private static boolean needPushProcess() {
		Log.e(TAG, "needPushProcess()");
		String pushPayload = CommonLibUtil.getUserConfigInfomation("payload", ExtendApplication.getInstance());
		Log.e(TAG, "pushPayload = " + pushPayload);
		if(TextUtils.isEmpty(pushPayload)) {
			return false;
		} else {
			return true;
		}
	}

	private static void callback() {
		Log.e(TAG, "callback()");

//		String payload = CommonLibUtil.getVariable("payload");
//		String type = CommonLibUtil.getVariable("type");
//		String status = CommonLibUtil.getVariable("status");

		String payload = CommonLibUtil.getUserConfigInfomation("payload", ExtendApplication.getInstance());
		String type = CommonLibUtil.getUserConfigInfomation("type", ExtendApplication.getInstance());
//		String status = CommonLibUtil.getUserConfigInfomation("status", ExtendApplication.getInstance());
		String status = CommonLibUtil.getUserConfigInfomation("pushcallback", ExtendApplication.getInstance());

		final MainActivity topAct = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
		final JSONObject pushJson = new JSONObject();

		try {
			pushJson.put("payload", payload);
			pushJson.put("type", type);
//			pushJson.put("status", status);
			pushJson.put("pushcallback", status);
			Log.e("PushMessageManager", "Push message!!" + pushJson.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		final String pushDataString = pushJson.toString();

		topAct.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String callbackScript = "javascript:" + "onReceiveNotification" + "(" + pushDataString + ");";
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					topAct.getWebView().evaluateJavascript(callbackScript, new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							PLog.i(TAG, "onReceiveValue(String value) = " + value);
						}
					});
				} else {
					topAct.getWebView().loadUrl(callbackScript);
				}
			}
		});
		// callback 했으니 push data 삭제
		removePushDataFromStorage();
	}

	private static void savePushDataToStorage(String payload, String type, String status) {
		CommonLibUtil.setUserConfigInfomation("payload", payload, ExtendApplication.getInstance());
		CommonLibUtil.setUserConfigInfomation("type", type, ExtendApplication.getInstance());
//		CommonLibUtil.setUserConfigInfomation("status", status, ExtendApplication.getInstance());
		CommonLibUtil.setUserConfigInfomation("pushcallback", status, ExtendApplication.getInstance());
	}

	private static void removePushDataFromStorage() {
		CommonLibUtil.setUserConfigInfomation("payload", "", ExtendApplication.getInstance());
		CommonLibUtil.setUserConfigInfomation("type", "", ExtendApplication.getInstance());
//		CommonLibUtil.setUserConfigInfomation("status", "", ExtendApplication.getInstance());
		// 화면에서 제거하기로 함.
//		CommonLibUtil.setUserConfigInfomation("pushcallback", "", ExtendApplication.getInstance());
	}

}
