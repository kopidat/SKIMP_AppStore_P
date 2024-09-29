package skimp.partner.store.implementation;

import m.client.android.library.core.bridge.InterfaceJavascript;
import m.client.android.library.core.utils.PLog;
import m.client.android.library.core.view.AbstractActivity;
import m.client.android.library.core.view.MainActivity;
import skimp.partner.store.R;
import skimp.partner.store.common.Const;
import skimp.partner.store.nativeex.AES256Util;
import skimp.partner.store.nativeex.LogoutNotiClickActivity;
import skimp.partner.store.nativeex.NativeBase;
import skimp.partner.store.nativeex.PermissionManager;
import skimp.partner.store.patternlock.activity.ConfirmPatternLayoutActivity;
import skimp.partner.store.patternlock.activity.SetPatternLayoutActivity;
import skimp.partner.store.patternlock.util.PatternLockUtils;
import skimp.partner.store.pinlock.activity.PinActivity;
import skimp.partner.store.provider.AuthContentProvider;
import skimp.partner.store.provider.DBManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ExtendWNInterface Class
 * 
 * @author 류경민(<a mailto="kmryu@uracle.co.kr">kmryu@uracle.co.kr</a>)
 * @version v 1.0.0
 * @since Android 2.1 <br>
 *        <DT><B>Date: </B>
 *        <DD>2011.04</DD>
 *        <DT><B>Company: </B>
 *        <DD>Uracle Co., Ltd.</DD>
 * 
 * 사용자 정의 확장 인터페이스 구현
 * 
 * Copyright (c) 2011-2013 Uracle Co., Ltd. 
 * 166 Samseong-dong, Gangnam-gu, Seoul, 135-090, Korea All Rights Reserved.
 */
public class ExtendWNInterface extends InterfaceJavascript {
	private static final String TAG = ExtendWNInterface.class.getSimpleName();

	/**
	 * 아래 생성자 메서드는 반드시 포함되어야 한다. 
	 * @param callerObject
	 * @param webView
	 */
	public ExtendWNInterface(AbstractActivity callerObject, WebView webView) {
		super(callerObject, webView);
	}
	
	/**
	 * 보안 키보드 데이터 콜백 함수 
	 * @param data 
	 */
	public void wnCBSecurityKeyboard(Intent data) {  
		// callerObject.startActivityForResult(newIntent,libactivities.ACTY_SECU_KEYBOARD);
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// 사용자 정의 네이티브 확장 메서드 구현
	
	//
	// 아래에 네이티브 확장 메서드들을 구현한다.
	// 사용 예
	//
	public String exWNTestReturnString(String receivedString) {
		String newStr = "I received [" + receivedString + "]";
		return newStr;
	}
	
	/**
	 * WebViewClient의 shouldOverrideUrlLoading()을 확장한다.
	 * @param view
	 * @param url
	 * @return 
	 * 		InterfaceJavascript.URL_LOADING_RETURN_STATUS_NONE	확장 구현을하지 않고 처리를 모피어스로 넘긴다.
	 * 		InterfaceJavascript.URL_LOADING_RETURN_STATUS_TRUE	if the host application wants to leave the current WebView and handle the url itself
	 * 		InterfaceJavascript.URL_LOADING_RETURN_STATUS_FALSE	otherwise return false.
	 */
	public int extendShouldOverrideUrlLoading(WebView view, String url) {

		// Custom url schema 사용 예
//		if(url.startsWith("custom:")) {
//		    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//		    callerObject.startActivity( intent ); 
//    		return InterfaceJavascript.URL_LOADING_RETURN_STATUS_FALSE;
//    	}
		
		return InterfaceJavascript.URL_LOADING_RETURN_STATUS_NONE;
	}
	
	/**
	 * 페이지 로딩이 시작되었을때 호출된다.
	 * @param view
	 * @param url
	 * @param favicon
	 */
	public void onExtendPageStarted (WebView view, String url, Bitmap favicon) {
		PLog.i("", ">>> 여기는 ExtendWNInterface onPageStarted입니다!!!");
	}
	
	/**
	 * 페이지 로딩이 완료되었을때 호출된다.
	 * @param view
	 * @param url
	 */
	public void onExtendPageFinished(WebView view, String url) {
		PLog.i("", ">>> 여기는 ExtendWNInterface onPageFinished!!!");
	}
	
	/**
	 * Give the host application a chance to handle the key event synchronously
	 * @param view
	 * @param event
	 * @return True if the host application wants to handle the key event itself, otherwise return false
	 */
	public boolean extendShouldOverrideKeyEvent(WebView view, KeyEvent event) {
		
		return false;
	}
	
	/**
	 * onActivityResult 발생시 호출.
	 */
	public void onExtendActivityResult(Integer requestCode, Integer resultCode, Intent data) {
		PLog.i("", ">>> 여기는 ExtendWNInterface onExtendActivityResult!!!  requestCode => [ " + requestCode + " ], resultCode => [ " + resultCode + " ]");
	}
	
	/*
	public String syncTest(String param1, String param2) {
		return param1 + param2;
	}
	
	public void asyncTest(final String callback) {
		callerObject.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String format = "javascript:%s('abc', 1, {'a':'b'});";
				String jsString = String.format(format, callback);
				PLog.d("asyncTest", jsString);
    			webView.loadUrl(jsString);
			}
		});
	}
	*/
	
	// 루팅 체크
	public void exWNRootCheck() {
		if(new skimp.partner.store.manager.RootCheck().isDeviceRooted(callerObject))
		{
			Toast.makeText(callerObject, "본 서비스 루팅된 단말기에서는 사용할수 없습니다. 정상단말기에서 사용하시기 바랍니다.", Toast.LENGTH_SHORT).show();
			callerObject.finish();
		}
	}
	
	// 앱 위변조 체크
	public void exWNVaildApp() {
		PackageManager pm = callerObject.getPackageManager();
		String packageName = callerObject.getPackageName();
		String cert = null;
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			Signature certSignature = packageInfo.signatures[0];
			MessageDigest msgDigest = MessageDigest.getInstance("SHA");
			msgDigest.update(certSignature.toByteArray());
			
			cert = Base64.encodeToString(msgDigest.digest(), Base64.NO_WRAP);

			String dbAppHashKey = DBManager.getInstance(callerObject).getHashKey();

			if (!cert.equals(dbAppHashKey)) {
				Toast.makeText(callerObject, "시스템이 위변조되었습니다.앱을 삭제 후 재설치하시기 바랍니다.", Toast.LENGTH_SHORT).show();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					callerObject.finishAffinity();
				} else {
					ActivityCompat.finishAffinity(callerObject);
				}
			} else {
				// 이후 로직 수행
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			//Log.e("JB", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getAppKeyHash() {
		try {
			PackageInfo info = callerObject.getPackageManager().getPackageInfo(callerObject.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.e("Hash key", something);
				return something;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("name not found", e.toString());
		}
		return "";
	}
	
	/**
	 * 결과 콜백
	 *
	 * @param resultCallback 콜백
	 * @param result         결과(JSON 형식)
	 */
	private void callback(final String resultCallback, final JSONObject result) {
		PLog.i(TAG, "callback(final String resultCallback, final JSONObject result) resultCallback = " + resultCallback);

		callerObject.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String callbackScript = "javascript:" + resultCallback + "(" + result + ");";
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					((MainActivity) callerObject).getWebView().evaluateJavascript(callbackScript, new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							PLog.i(TAG, "onReceiveValue(String value) = " + value);
						}
					});
				} else {
					((MainActivity) callerObject).getWebView().loadUrl(callbackScript);
				}
			}
		});
	}

	/**
	 * 앱 설정 화면으로 이동
	 */
	public void exWNGoAppSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse("package:" + callerObject.getPackageName()));
		callerObject.startActivity(intent);
	}

	/**
	 * 안드로이드 권한 획득여부
	 * @param permission 안드로이드 퍼미션 이름 ex) "android.permission.CAMERA"
	 * @return 권한 획득여부
	 */
	public String exWNHasPermissionYN(String permission) {
		PLog.i(TAG, "exWNHasPermissionYN(String permission) = " + permission);

		boolean hasPermission = PermissionManager.getInstance().hasPermission(callerObject, permission);
		if(hasPermission) {
			return "Y";
		} else {
			return "N";
		}
	}

	/**
	 * 안드로이드 권한 요청 - BaseActivity의 onRequestPermissionsResult에서 결과 받음.
	 * @param params 요청할 퍼미션명들 JSONArray 형태
	 */
	public void exWNRequestPermissions(final String params, final String resultCallback) {
		PLog.i(TAG, "exWNRequestPermissions(final String params, final String resultCallback)");
		PLog.d(TAG, "params = " + params);
		PLog.d(TAG, "resultCallback = " + resultCallback);

		PermissionManager.getInstance().requestPermissions(callerObject, params, new NativeBase.ResultListener() {
			@Override
			public void onResult(JSONObject result) {
				PLog.i(TAG, "onResult(JSONObject result) = " + result);
				callback(resultCallback, result);
			}
		});
	}

	/***********************
	 * 간편인증 (패턴, 핀)
	 ***********************/
	// Pin lock screen
	public void exWNPinLockActivity(String jsonData) throws JSONException {
		PLog.i(TAG, "exWNPinLockActivity(String jsonData) = " + jsonData);
		JSONObject jsonObj = new JSONObject(jsonData);
		String method = jsonObj.optString("callback");
		String length = jsonObj.optString("length");
		String type = jsonObj.optString("type");
		Const.JS_PinAuthManager = method;

		Intent intent = new Intent(callerObject, PinActivity.class);
		intent.putExtra("command", type);
		intent.putExtra("length", length);
		intent.putExtra("callback", method);
		callerObject.startActivityForResult(intent, Const.REQ_AUTH_PIN);
	}

	// Pattern lock screen
	public void exWNPatternLockActivity(String jsonData) throws JSONException {
		PLog.i(TAG, "exWNPatternLockActivity(String jsonData) = " + jsonData);
		JSONObject jsonObj = new JSONObject(jsonData);
		String method = jsonObj.getString("callback");
		String type = jsonObj.getString("type");
		Const.JS_PatternAuthManager = method;
		if (type.equals("reset")) {
			Intent intent = new Intent(callerObject, SetPatternLayoutActivity.class);
			intent.putExtra("callbackName", method);
			callerObject.startActivityForResult(intent, Const.REQ_AUTH_PATTERN);
		} else {
			if (PatternLockUtils.hasPattern(callerObject) == true) {
				Intent intent = new Intent(callerObject, ConfirmPatternLayoutActivity.class);
				intent.putExtra("callbackName", method);
				callerObject.startActivityForResult(intent, Const.REQ_AUTH_PATTERN);
			} else {
				Intent intent = new Intent(callerObject, SetPatternLayoutActivity.class);
				intent.putExtra("callbackName", method);
				callerObject.startActivityForResult(intent, Const.REQ_AUTH_PATTERN);
			}
		}
	}

	/**
	 * 스토어 로그인 정보 저장 확장함수
	 */
	public void exWNSetUserInfo(String userInfoJsonString) {
		PLog.i(TAG, "exWNSetUserInfo(String userInfoJsonString) = " + userInfoJsonString);

		try {
			JSONObject jsonObj = new JSONObject(userInfoJsonString);
			String userid = jsonObj.optString(DBManager.TBL_LOGIN_USER.COL_userId);
			String token = jsonObj.optString(DBManager.TBL_LOGIN_USER.COL_token);
			String encPwd = jsonObj.optString(DBManager.TBL_LOGIN_USER.COL_encPwd);
			ContentValues values = new ContentValues();
			values.put(DBManager.TBL_LOGIN_USER.COL_userId, userid);
			values.put(DBManager.TBL_LOGIN_USER.COL_token, token);
			values.put(DBManager.TBL_LOGIN_USER.COL_encPwd, encPwd);
			callerObject.getContentResolver().insert(AuthContentProvider.URI_LOGIN_USER, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String exWNAES256Encrypt(String plainText) {
		PLog.i(TAG, "exWNAES256Encrypt(String plainText) = " + plainText);
		try {
//			String key = DBManager.getInstance(callerObject).getKey();
			String key = "0000000000000000g!.?eK9A4,Ea3Yl*";
//			String key = "g!.?eK9A4,Ea3Yl*";
			String encText = AES256Util.encrypt(key, plainText);
			byte[] testByte = {0x00, 0x00, 0x00, 0x00, 0x00, 48};
			String testString = new String(testByte);
			PLog.e(TAG, "testString = " + testString);
			PLog.e(TAG, "encText = " + encText);
			return encText;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String exWNAES256Decrypt(String encText) {
		PLog.i(TAG, "exWNAES256Decrypt(String encText) = " + encText);
		try {
			String key = DBManager.getInstance(callerObject).getKey();
			String plainText = AES256Util.decrypt(key, encText);
			PLog.e(TAG, "plainText = " + plainText);
			return plainText;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public void exWNRegisterLogoutNoti() {
		PLog.i(TAG, "exWNRegisterLogoutNoti()");

		int icon = R.drawable.ic_launcher;
		//long when = System.currentTimeMillis();

		String title = callerObject.getString(R.string.app_name);
		Intent notificationIntent = new Intent(callerObject, LogoutNotiClickActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		PendingIntent pendingIntent = PendingIntent.getActivity(callerObject, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		NotificationManager notificationManager = (NotificationManager) callerObject.getSystemService(Context.NOTIFICATION_SERVICE);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(callerObject.getApplicationContext(), ExtendApplication.sLogoutNotiChannelId)
				.setOngoing(true)
				.setAutoCancel(false)
				.setContentIntent(pendingIntent)
				.setSmallIcon(icon)
				.setContentTitle(title)
				.setContentText("로그아웃 하시겠습니까?")
				.setTicker(title + " Ticker")
				.setSound(soundUri);

		//mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		notificationManager.notify("logout_alarm", 12345, mBuilder.build());
	}

	public void exWNUnregisterLogoutNoti() {
		PLog.i(TAG, "exWNUnregisterLogoutNoti()");
		NotificationManager notificationManager = (NotificationManager) callerObject.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel("logout_alarm", 12345);
	}
}
