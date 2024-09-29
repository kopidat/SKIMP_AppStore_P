package skimp.partner.store.nativeex;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.ValueCallback;

import m.client.android.library.core.common.CommonLibHandler;
import m.client.android.library.core.managers.ActivityHistoryManager;
import m.client.android.library.core.utils.CommonLibUtil;
import m.client.android.library.core.utils.PLog;
import m.client.android.library.core.view.MainActivity;
import skimp.partner.store.implementation.ExtendApplication;

public class LogoutNotiClickActivity extends Activity {
	private static final String TAG = LogoutNotiClickActivity.class.getSimpleName();

	private static final String KEY_is_logout_noti_click_YN = "is_logout_noti_click_YN";

	private CommonLibHandler mCommLibHandle = CommonLibHandler.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate(Bundle savedInstanceState)");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 지오펜싱 서비스가 실행중이면 mCommLibHandle.g_strExtWNIClassPackageName null이 아니다
		// 따라서 topActivity == null로 앱 실행여부를 체크해야함.
		final MainActivity topActivity = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();
		Log.e(TAG, "mCommLibHandle.g_strExtWNIClassPackageName = " + mCommLibHandle.g_strExtWNIClassPackageName);
		Log.e(TAG, "topActivity = " + topActivity);
		// 앱 실행중이 아님.
		if (mCommLibHandle.g_strExtWNIClassPackageName == null || topActivity == null) {
			// 앱이 실행중이 아니기 때문에 스토리지에 저장후
			// mCommLibHandle.processAppInit만 수행하고 Startup -> BaseActivity onPageFinished 에서 처리함.
			saveDataToStorage();

			// 프로그램이 정상적으로 구동되지 않고 바로 실행되는 경우에는 프로그램 초기화 처리를 거쳐야 한다.
			// mCommLibHandle.processAppInit만 수행하고 Startup -> BaseActivity onPageFinished 에서 처리함.
			// delay줘야함. delay 안주면 노티클릭으로 LogoutNotiClickActivity 한번 실행된 후 이후 노티클릭하면 실행안됨.
			new Handler(getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					mCommLibHandle.processAppInit(LogoutNotiClickActivity.this);
				}
			}, 100);
		} else {  // 앱 실행중
			saveDataToStorage();

			callback();
		}
		finish();
	}

	public static void checkStartFromNotiClick() {
		Log.e(TAG, "checkStartFromNotiClick()");
		if(needProcess()) {
			callback();
		}
	}

	private static boolean needProcess() {
		Log.e(TAG, "needProcess()");
		String isLogoutNotiClickYN = CommonLibUtil.getUserConfigInfomation(KEY_is_logout_noti_click_YN, ExtendApplication.getInstance());
		Log.e(TAG, "isLogoutNotiClickYN = " + isLogoutNotiClickYN);
		if("Y".equals(isLogoutNotiClickYN)) {
			return true;
		} else {
			return false;
		}
	}

	private static void callback() {
		Log.e(TAG, "callback()");

		final MainActivity topAct = (MainActivity) ActivityHistoryManager.getInstance().getTopActivity();

		topAct.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String callbackScript = "javascript:" + "onLogoutNotiClicked();";
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
		// callback 했으니 data 삭제
		removeDataFromStorage();
	}

	private static void saveDataToStorage() {
		CommonLibUtil.setUserConfigInfomation(KEY_is_logout_noti_click_YN, "Y", ExtendApplication.getInstance());
	}

	private static void removeDataFromStorage() {
		CommonLibUtil.setUserConfigInfomation(KEY_is_logout_noti_click_YN, "", ExtendApplication.getInstance());
	}

}
