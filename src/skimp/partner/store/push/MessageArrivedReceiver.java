package skimp.partner.store.push;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import org.json.JSONObject;

import m.client.android.library.core.managers.ActivityHistoryManager;
import m.client.android.library.core.view.MainActivity;
import m.client.push.library.PushManager;
import m.client.push.library.common.PushConstants;

public class MessageArrivedReceiver extends BroadcastReceiver {
	private static final String TAG = MessageArrivedReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "MessageArrivedReceiver action:: " + intent.getAction());

		try {
			// TODO: 긴급 메시지 여부를 체크해야함
			// 알림에 대한 판단
			//String isAlarmOn = CommonLibUtil.getUserConfigInfomation("regiService", context);
			//Log.d("MessageArrivedReceiver", "MessageArrivedReceiver isAlarmOn:: " + isAlarmOn);
			//if (!TextUtils.isEmpty(isAlarmOn) && "true".equals(isAlarmOn)) {

			final String jsonData = intent.getExtras().getString("JSON");
			final String encryptData = intent.getExtras().getString(PushConstants.KEY_ORIGINAL_PAYLOAD_STRING);
			JSONObject jsonMsg = new JSONObject(jsonData);

			// 푸시 수신확인
			PushManager.getInstance().pushMessageReceiveConfirm(context, jsonData);

			String pushType;
			String badgeNo;
			if (intent.getAction().equals(context.getPackageName() + PushConstants.ACTION_UPNS_MESSAGE_ARRIVED)) {
				pushType = PushConstants.STR_UPNS_PUSH_TYPE;
				badgeNo = jsonMsg.getString("BADGENO");
			} else {
				pushType = PushConstants.STR_GCM_PUSH_TYPE;
				JSONObject aps = jsonMsg.getJSONObject("aps");
				badgeNo = aps.getString("badge");
			}

			// 뱃지 설정
			try {
				if (!TextUtils.isEmpty(badgeNo) && Integer.parseInt(badgeNo) > 0) {
					PushManager.getInstance().setDeviceBadgeCount(context, badgeNo);
				}
			} catch(NumberFormatException e) {
				// 존재하지 않거나 0보다 작다면 무시..
				e.printStackTrace();
			}

			// 푸시 수신 callback 호출
			callbackReceiveAndroidNotification(jsonData, encryptData, pushType);

			// 노티피케이션 생성
			PushNotificationManager.createNotification(context, intent, pushType);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void callbackReceiveAndroidNotification(String payload, String encPayload, String pushType) {
		Log.i(TAG, "callbackReceiveAndroidNotification(String payload, String encPayload, String pushType) = " + pushType);
		final Activity activity = ActivityHistoryManager.getInstance().getTopActivity();
		if (activity == null || !(activity instanceof MainActivity)) {
			Log.e(TAG, "if(activity == null || !(activity instanceof MainActivity)) {");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				callback(activity, payload, encPayload, pushType);
			}
		});
	}

	private void callback(Activity activity, String payload, String encPayload, String pushType) {
		Log.i(TAG, "callback(String payload, String encPayload, String pushType) = " + pushType);

		JSONObject tempJSON = new JSONObject();
		try {
			tempJSON.put("payload", payload);
			tempJSON.put("type", pushType);
			tempJSON.put("status", "ACTIVE");
			tempJSON.put("encrypt", encPayload);

			String callbackScript = "javascript:" + "onReceiveAndroidNotification" + "(" + tempJSON.toString() + ");";

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				((MainActivity) activity).getCurrentMPWebView().evaluateJavascript(callbackScript, new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						Log.i(TAG, "onReceiveValue(String value) = " + value);
					}
				});
			} else {
				((MainActivity) activity).getCurrentMPWebView().loadUrl(callbackScript);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
