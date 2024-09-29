package skimp.partner.store;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import m.client.android.library.core.utils.PLog;
import m.client.android.library.core.view.MainActivity;
import skimp.partner.store.common.Const;
import skimp.partner.store.common.Utils;
import skimp.partner.store.implementation.ExtendApplication;
import skimp.partner.store.implementation.ExtendWNInterface;
import skimp.partner.store.manager.InterfaceManager;
import skimp.partner.store.nativeex.LogoutNotiClickActivity;
import skimp.partner.store.nativeex.PermissionManager;
import skimp.partner.store.push.PushMessageManager;

/**
 * BaseActivity Class
 * 
 * @author 김태욱(<a mailto="tukim@uracle.co.kr">tukim@uracle.co.kr</a>)
 * @version v 1.0.0
 * @since Android 2.1 <br>
 *        <DT><B>Date: </B>
 *        <DD>2013.08.01</DD>
 *        <DT><B>Company: </B>
 *        <DD>Uracle Co., Ltd.</DD>
 * 
 * 모피어스 내에서 제공되는 모든 Web 페이지의 기본이 되는 Activity
 * html 화면은 모두 BaseActivity 상에서 출력된다.
 * 제어를 원하는 이벤트들은 overriding 하여 구현하며, 각각 페이지 별 이벤트는 화면 단위로 분기하여 처리한다.
 * 플랫폼 내부에서 사용하는 클래스로 해당 클래스의 이름은 변경할 수 없다.
 * 
 * Copyright (c) 2001-2011 Uracle Co., Ltd. 
 * 166 Samseong-dong, Gangnam-gu, Seoul, 135-090, Korea All Rights Reserved.
 */

public class BaseActivity extends MainActivity {
	private static final String TAG = BaseActivity.class.getSimpleName();

	/** MDM(MDS) 패키지명 */
	private static final String MDM_PACKAGE_NAME = "com.funnnew.android.skimds";

	/** MDM(MDS) 다운로드 페이지 url */
//	private static final String MDM_DOWNLOAD_PAGE_URL = "https://mds.skinnovation.com/downloads"; // 운영
	private static final String MDM_DOWNLOAD_PAGE_URL = "https://svr.funnnew.com:24005/downloads"; // 테스트

	@Override
	public void onCreate(Bundle savedInstanceState) {
		PLog.i(TAG, "onCreate(Bundle savedInstanceState)");
		super.onCreate(savedInstanceState);
		getWebView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// 인트로 페이지 이후에 MDM(MDS) 검사 하도록
		String targetUrl = (String) mParams.getParam("TARGET_URL");
		String introPageUrl = "www/html/SKIMP-FR-001.html";
		if(targetUrl != null && targetUrl.contains(introPageUrl)) {
			// do nothing
			return;
		}

		// MDM 설치 및 정책 ON 세부적으로 체크 및 안내팝업
		if(ExtendApplication.sCheckMDM) {
			// 1. 설치확인
			if(!Utils.isAppInstalled(this, MDM_PACKAGE_NAME)) {
				showMDMInstallDialog();
			} else if(!isMDMOn()) { // 2. 정책확인
				showMDMOnDialog();
			}
		}

		// 루팅 체크
//		new ExtendWNInterface(this, getWebView()).exWNRootCheck();
		// 앱 위변조 체크
		new ExtendWNInterface(this, getWebView()).exWNVaildApp();
		// 해시값 체크
//		new ExtendWNInterface(this, getWebView()).getAppKeyHash();
	
	}
	
	/**
	 * Webview가 시작 될 때 호출되는 함수
	 */
	@Override
	public void onPageStarted (WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
	}
	
	/**
	 * Webview내 컨텐츠가 로드되고 난 후 호출되는 함수
	 */
	@Override
	public void onPageFinished(WebView view, String url)  {
		super.onPageFinished(view, url);

		// 푸시 노티클릭으로 실행되었는지 체크
		PushMessageManager.checkStartFromPushNotiClick();

		// 개별앱에서 라이브러리를 통해 로그인이나 개별앱 업데이트를 위해 실행했는지 체크
		SchemeActivity.checkStartFromOtherApp();

		// 로그아웃 노티클릭
		LogoutNotiClickActivity.checkStartFromNotiClick();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == PermissionManager.PERMISSIONS_REQUEST) {
			PermissionManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		PLog.i(TAG, "onActivityResult(int requestCode, int resultCode, Intent data)");
		PLog.d(TAG, "requestCode = " + requestCode);
		PLog.d(TAG, "resultCode = " + resultCode);
		PLog.d(TAG, "data = " + data);

		Utils.debugIntent(TAG, data);

		if(data == null) return;

		super.onActivityResult(requestCode, resultCode, data);

		// 간편 인증
		if (requestCode == Const.REQ_AUTH_PIN) {
			Log.d("KDS", "result = "+data.getStringExtra("KEY_RESULT"));
			Log.d("KDS", "pin = "+data.getStringExtra("pin"));
			String result = data.getStringExtra("KEY_RESULT");
			String pin = data.getStringExtra("pin");
			String message = data.getStringExtra("message");
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", result);
				obj.put("message", TextUtils.isEmpty(message)? result:message);
				if(!TextUtils.isEmpty(pin) || resultCode == Const.REQ_AUTH_PIN_GET)
					obj.put("pin", pin);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			InterfaceManager.getInstance().loadUrl(this, Const.JS_PinAuthManager, result, obj);
		} else if (requestCode == Const.REQ_AUTH_PATTERN) {
			Log.d("KDS", "result = "+data.getStringExtra("KEY_RESULT"));
			Log.d("KDS", "KEY_CODE = "+data.getStringExtra("KEY_CODE"));
			String result = data.getStringExtra("KEY_RESULT");
			String KEY_CODE = data.getStringExtra("KEY_CODE");
			String message = data.getStringExtra("message");
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", result);
				obj.put("message", TextUtils.isEmpty(message)? result:message);
				if(!TextUtils.isEmpty(KEY_CODE)) obj.put("KEY_CODE", KEY_CODE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			InterfaceManager.getInstance().loadUrl(this, Const.JS_PatternAuthManager, result, obj);
		}
	}

	private boolean isMDMOn() {
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		List<ComponentName> activeAdmins = devicePolicyManager.getActiveAdmins();
		if (activeAdmins != null) {
			for (ComponentName admin : activeAdmins) {
				if (admin.getPackageName().compareTo(MDM_PACKAGE_NAME) == 0) {
					if (devicePolicyManager.getCameraDisabled(admin)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	AlertDialog mdmAlertDialog;
	private void showMDMInstallDialog() {
		if(mdmAlertDialog != null && mdmAlertDialog.isShowing()) {
			// do nothing
			return;
		}
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setCancelable(false);
		alertDialog.setTitle("알림");
		alertDialog.setMessage("보안앱(MDS)이 설치되지 않았습니다.\n설치 페이지로 이동합니다.");
		alertDialog.setNegativeButton("이동", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				goMDMInstallPage();
				// 앱 종료, 페이지 이동했을 경우 상위 액티비티만 종료하는게 아니라 앱을 종료하기 위해 finishAffinity() 사용
//				finish();
				finishAffinity();
			}
		});

		mdmAlertDialog = alertDialog.create();
		mdmAlertDialog.show();
	}

    private void showMDMOnDialog() {
		if(mdmAlertDialog != null && mdmAlertDialog.isShowing()) {
			// do nothing
			return;
		}
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setCancelable(false);
        alertDialog.setTitle("알림");
        alertDialog.setMessage("보안 OFF 상태입니다.\nON후 이용해주세요.");
        alertDialog.setNegativeButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Intent intent_mds = getPackageManager().getLaunchIntentForPackage("com.funnnew.android.skimds");
//                if (intent_mds != null) {
//                    startActivity(intent_mds);
//                }
				// 앱 종료, 페이지 이동했을 경우 상위 액티비티만 종료하는게 아니라 앱을 종료하기 위해 finishAffinity() 사용
//                finish();
				finishAffinity();
            }
        });

		mdmAlertDialog = alertDialog.create();
		mdmAlertDialog.show();
    }

	private void goMDMInstallPage() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MDM_DOWNLOAD_PAGE_URL));
		startActivity(intent);
	}

}
