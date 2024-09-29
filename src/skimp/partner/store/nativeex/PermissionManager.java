package skimp.partner.store.nativeex;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import m.client.android.library.core.utils.PLog;

/**
 * 안드로이드 퍼미션 관리
 */

public class PermissionManager extends NativeBase {
	private static final String TAG = PermissionManager.class.getSimpleName();

	public static final int PERMISSIONS_REQUEST = 1000;

	/** 권한 획득 실패 */
	public static final int RESULT_CODE_ERR_NOT_GRANTED = -1;
	/** 권한 획득 실패 */
	public static final String RESULT_MSG_NOT_GRANTED = "권한 획득 실패.";

	/** 권한이 필요한 이유 설명해야함. */
	public static final int RESULT_CODE_ERR_NEED_RATIONALE = 1000;
	/** 권한이 필요한 이유 설명해야함. */
	public static final String RESULT_MSG_NEED_RATIONALE = "권한요청해도 시스템 팝업 안뜸. 권한이 필요한 이유 설명하는 팝업 띄우고 확인시 앱 설정으로 보내야함. exWNGoAppSettings 호출";

	private static PermissionManager sInstance;
	public static synchronized PermissionManager getInstance() {
		if (sInstance == null) {
			sInstance = new PermissionManager();
		}
		return sInstance;
	}

	private PermissionManager() {
	}

	public boolean hasPermission(Context context, String permission) {
		PLog.e(TAG, "hasPermission(Context context, String permission) = " + permission);

		boolean granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
		PLog.e(TAG, "granted = " + granted);

		return granted;
	}

	public void requestPermissions(final Activity activity, final String params, final ResultListener resultListener) {
		PLog.i(TAG, "requestPermissions(final Activity activity, final String params, final ResultListener resultListener)");
		PLog.d(TAG, "params = " + params);
		PLog.d(TAG, "resultListener = " + resultListener);

		mDefaultResultListener = resultListener;

		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(params);
		} catch (JSONException jsonEx) {
			PLog.e(TAG, "catch (JSONException jsonEx) {");
			responseError(RESULT_CODE_ERR_INVALID_PARAM_JSON);
			return;
		} catch (Exception ex) {
			PLog.e(TAG, "catch (Exception ex) {");
			responseError(RESULT_CODE_ERR_INVALID_PARAM_UNKNOWN);
			return;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			responseSuccess();
			return;
		}

		Map<String, Boolean> permissionMap = new HashMap<>();
		Map<String, Boolean> rationaleMap = new HashMap<>();
		String[] permissions = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			permissions[i] = jsonArray.optString(i);
			PLog.e(TAG, permissions[i] + " need rationale = " + ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]));
			permissionMap.put(permissions[i], hasPermission(activity, permissions[i]));
			rationaleMap.put(permissions[i], ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]));
		}

		if (!permissionMap.containsValue(Boolean.FALSE)) {
			// 이미 획득한 권한요청 - 그냥 성공으로 callback 하자.
			responseSuccess();
		} else {
			// 최초 요청이면 바로 요청하자. 화면 callback은 onRequestPermissionsResult에서 함.
			if(isFirstRequest(activity, permissions)) {
				setFirstRequest(activity, permissions);
				ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST);
				return;
			}

			// shouldShowRequestPermissionRationale 처리
			// 최초 요청이면 false - 위에서 최초 요청 처리했으니 고려 안해도 됨.
			// 다시보지않기 체크후 거부이면 false
			// 체크없이 거부하면 true

			// 하나라도 true가 있으면 권한요청 해야함.
			// 그렇지 않으면 권한요청해도 시스템 요청팝업 안뜸. 따라서 설정으로 보내는 팝업띄워야함.
			if (rationaleMap.containsValue(Boolean.TRUE)) {
				ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST);
			} else {
				responseError(RESULT_CODE_ERR_NEED_RATIONALE, RESULT_MSG_NEED_RATIONALE);
			}
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		PLog.i(TAG, "onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)");
		PLog.d(TAG, "permissions = " + Arrays.deepToString(permissions));
		PLog.d(TAG, "grantResults = " + Arrays.toString(grantResults));

		// We have requested multiple permissions, so all of them need to be checked.
		if (verifyPermissions(grantResults)) {
			// All required permissions have been granted.
			responseSuccess();
		} else {
			PLog.e(TAG, "Permissions were NOT granted.");
			responseError(RESULT_CODE_ERR_NOT_GRANTED, RESULT_MSG_NOT_GRANTED);
		}
	}


	/**
	 * Check that all given permissions have been granted by verifying that each entry in the
	 * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
	 */
	private static boolean verifyPermissions(int[] grantResults) {
		// At least one result must be checked.
		if (grantResults.length < 1) {
			return false;
		}

		// Verify that each required permission has been granted, otherwise return false.
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	private static void setFirstRequest(Context context, String[] permissions) {
		PrefUtil.setFirstRequest(context, permissions);
	}

	private static boolean isFirstRequest(Context context, String[] permissions) {
		return PrefUtil.isFirstRequest(context, permissions);
	}
}
