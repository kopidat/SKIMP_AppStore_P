package skimp.partner.store.nativeex;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.android.library.core.utils.PLog;

/**
 *<prev>
 *  안드로이드 네이티브 연동 클래스들
 * </prev>
 */

public abstract class NativeBase {
	private static final String TAG = NativeBase.class.getSimpleName();

	public static final String RESULT_KEY_CODE = "code";
	public static final String RESULT_KEY_MSG = "msg";
	public static final String RESULT_KEY_DATA = "resultData";

	/** 성공 */
	public static final int RESULT_CODE_SUCCESS = 0;

	/** 알수없는 에러 */
	public static final int RESULT_CODE_ERR_UNKNOWN = -1;
	/** 요청 파라미터 오류 - 기타 */
	public static final int RESULT_CODE_ERR_INVALID_PARAM_UNKNOWN = -100;
	/** 요청 파라미터 오류 - 잘못된 JSON 형식 */
	public static final int RESULT_CODE_ERR_INVALID_PARAM_JSON = -101;

	/** 성공 메세지 */
	public static final String RESULT_MSG_SUCCESS = "success";

	/** 알수없는 에러 메세지 */
	public static final String RESULT_MSG_UNKNOWN = "알수없는 에러";
	/** 요청 파라미터 오류 - 기타 메세지 */
	public static final String RESULT_MSG_INVALID_PARAM_UNKNOWN = "요청 파라미터 오류 - 기타";
	/** 요청 파라미터 오류 - 잘못된 JSON 형식 메세지 */
	public static final String RESULT_MSG_INVALID_PARAM_JSON = "요청 파라미터 오류 - 잘못된 JSON 형식";

	

	public interface ResultListener {
		void onResult(JSONObject result);
	}

	protected ResultListener mDefaultResultListener;

	protected void responseSuccess() {
		responseSimple(RESULT_CODE_SUCCESS, RESULT_MSG_SUCCESS);
	}

	protected void responseError(long errorCode) {
		responseSimple(errorCode, null);
	}

	protected void responseError(long errorCode, String errorMsg) {
		responseSimple(errorCode, errorMsg, true);
	}

	protected void responseSuccessWithData(String key, String value) {
		responseSuccessWithData(key, value, true);
	}

	protected void responseSuccessWithData(final JSONObject data) {
		responseSuccessWithData(data, true);
	}

    protected void responseSimple(long code, String msg) {
        responseSimple(code, msg, true);
    }

	protected void responseSimple(long code, String msg, boolean isComplete) {
		JSONObject resultJson = new JSONObject();
		makeResultCode(resultJson, code);
		if(!TextUtils.isEmpty(msg)) {
			makeResultMsg(resultJson, msg);
		}
		responseResult(resultJson, isComplete);
	}

	protected void responseSuccessWithData(final String key, final String value, boolean isComplete) {
		JSONObject resultJson = new JSONObject();
		makeResultCode(resultJson, RESULT_CODE_SUCCESS);
		makeResultData(resultJson, key, value);
		responseResult(resultJson, isComplete);
	}

	protected void responseSuccessWithData(final JSONObject data, boolean isComplete) {
		JSONObject resultJson = new JSONObject();
		makeResultCode(resultJson, RESULT_CODE_SUCCESS);
		makeResultData(resultJson, data);
		responseResult(resultJson, isComplete);
	}

	protected void responseResult(JSONObject resultJson, boolean isComplete) {
		if (mDefaultResultListener != null) {
			mDefaultResultListener.onResult(resultJson);

			if(isComplete) {
				mDefaultResultListener = null;
			}
		}
	}

	public static JSONObject makeResultCode(JSONObject resultJson, long code) {
		try {
			resultJson.put(RESULT_KEY_CODE, code);
			switch ((int) code) {
				case RESULT_CODE_SUCCESS:
					resultJson.put(RESULT_KEY_MSG, RESULT_MSG_SUCCESS);
					break;
				case RESULT_CODE_ERR_INVALID_PARAM_UNKNOWN:
					resultJson.put(RESULT_KEY_MSG, RESULT_MSG_INVALID_PARAM_UNKNOWN);
					break;
				case RESULT_CODE_ERR_INVALID_PARAM_JSON:
					resultJson.put(RESULT_KEY_MSG, RESULT_MSG_INVALID_PARAM_JSON);
					break;
				default:
					resultJson.put(RESULT_KEY_MSG, RESULT_MSG_UNKNOWN);
					break;
			}
		} catch (JSONException e) {
			PLog.e(TAG, "catch (JSONException e) {");
			try {
				resultJson.put(RESULT_KEY_CODE, RESULT_CODE_ERR_UNKNOWN);
				resultJson.put(RESULT_KEY_MSG, RESULT_MSG_UNKNOWN);
			} catch (JSONException e1) {
				PLog.e(TAG, "catch (JSONException e1) {");
			}
		}

		return resultJson;
	}

    protected static JSONObject makeResultMsg(JSONObject resultJson, String msg) {
        try {
            resultJson.put(RESULT_KEY_MSG, msg);
		} catch (JSONException e) {
			PLog.e(TAG, "catch (JSONException e) {");
            try {
                resultJson.put(RESULT_KEY_MSG, RESULT_MSG_UNKNOWN);
            } catch (JSONException e1) {
				PLog.e(TAG, "catch (JSONException e1) {");
            }
        }

        return resultJson;
    }

	public static void makeResultData(JSONObject result, final String key, final String value) {
		try {
			JSONObject resultData = new JSONObject();
			resultData.put(key, value);

			result.put(RESULT_KEY_DATA, resultData);
		} catch (JSONException e) {
			PLog.e(TAG, "catch (JSONException e) {");
		}
	}

	private static void makeResultData(JSONObject result, final JSONObject data) {
		try {
			result.put(RESULT_KEY_DATA, data);
		} catch (JSONException e) {
			PLog.e(TAG, "catch (JSONException e) {");
		}
	}

	private static void makeResultData(JSONObject result, final String key, final JSONObject value) {
		try {
			JSONObject resultData = new JSONObject();
			resultData.put(key, value);

			result.put(RESULT_KEY_DATA, resultData);
		} catch (JSONException e) {
			PLog.e(TAG, "catch (JSONException e) {");
		}
	}

	protected static JSONObject makeResult(JSONObject resultJson, long code, String msg) {
		makeResultCode(resultJson, code);
		makeResultMsg(resultJson, msg);
		return resultJson;
	}

	protected JSONObject getParamJson(final String params, String[] keys) throws JSONException, Exception {
		// 필수 파라미터 검증
		JSONObject jsonParams = null;
		try {
			jsonParams = new JSONObject(params);
			for (String key : keys) {
				if (!jsonParams.has(key)) {
					PLog.e(TAG, "getParamJson no has key = " + key);
					throw new JSONException(params);
				}
			}
		} catch (JSONException jsonEx) {
			PLog.e(TAG, "catch (JSONException jsonEx) {");
			throw jsonEx;
		} catch (Exception ex) {
			PLog.e(TAG, "catch (Exception ex) {");
			throw ex;
		}

		return jsonParams;
	}

	/**
	 * String 형태의 code 값
	 * @param code String 형태의 code 값
	 * @param msg 메세지
	 */
	protected void responseSimple(String code, String msg) {
		JSONObject resultJson = new JSONObject();
		try {
			resultJson.put(RESULT_KEY_CODE, code);
		} catch (Exception e) {
			PLog.e(TAG, "catch (Exception ex) {");
			try {
				resultJson.put(RESULT_KEY_CODE, RESULT_MSG_UNKNOWN);
			} catch (JSONException e1) {
				PLog.e(TAG, "catch (JSONException e1) {");
			}
		}

		if(!TextUtils.isEmpty(msg)) {
			makeResultMsg(resultJson, msg);
		}
		responseResult(resultJson, true);
	}
}
