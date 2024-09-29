
package skimp.partner.store.nativeex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.annotation.NonNull;

public class PrefUtil {

	private static final String KEY_IS_FIRST_REQUEST_PRIFIX = "KEY_IS_FIRST_REQUEST_";

	public static void setFirstRequest(Context context, @NonNull String[] permissions) {
		for (String permission : permissions) {
			setFirstRequest(context, permission);
		}
	}

	private static void setFirstRequest(Context context, String permission) {
		putBoolean(context, getPrefsNamePermission(permission), false);
	}

	public static boolean isFirstRequest(Context context, @NonNull String[] permissions) {
		for (String permission : permissions) {
			if (!isFirstRequest(context, permission)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isFirstRequest(Context context, String permission) {
		return getBoolean(context, getPrefsNamePermission(permission), true);
	}

	private static String getPrefsNamePermission(String permission) {
		return KEY_IS_FIRST_REQUEST_PRIFIX + permission;
	}

	public static void clearSharedPreference(Context context) {
		Editor editor = getEditor(context);
		editor.clear();
		editor.commit();
	}
	
	private static void remove(Context context, String key) {
		Editor editor = getEditor(context);
		editor.remove(key);
		editor.commit();
	}
	
	public static void removeAll(Context context) {
		Editor editor = getEditor(context);
		editor.clear();
		editor.commit();
	}
	
	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}
	
	private static Editor getEditor(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.edit();
	}
	
	private static void putString(Context context, String key, String value) {
		Editor editor = getEditor(context);
		editor.putString(key, value);
		editor.commit();
	}
	
	private static String getString(Context context, String key, String defValue) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getString(key, defValue);
	}
	
	private static String getString(Context context, String key) {
		return getString(context, key, null);
	}
	
	private static void putInt(Context context, String key, int value) {
		Editor editor = getEditor(context);
		editor.putInt(key, value);
		editor.commit();
	}
	
	private static int getInt(Context context, String key, int defValue) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getInt(key, defValue);
	}
	
	private static int getInt(Context context, String key) {
		return getInt(context, key, 0);
	}
	
	private static void putLong(Context context, String key, long value) {
		Editor editor = getEditor(context);
		editor.putLong(key, value);
		editor.commit();
	}
	
	private static long getLong(Context context, String key, long defValue) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getLong(key, defValue);
	}
	
	private static long getLong(Context context, String key) {
		return getLong(context, key, 0);
	}
	
	private static void putFloat(Context context, String key, float value) {
		Editor editor = getEditor(context);
		editor.putFloat(key, value);
		editor.commit();
	}
	
	private static float getFloat(Context context, String key, float defValue) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getFloat(key, defValue);
	}
	
	private static float getFloat(Context context, String key) {
		return getFloat(context, key, 0);
	}
	
	private static void putBoolean(Context context, String key, boolean value) {
		Editor editor = getEditor(context);
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private static boolean getBoolean(Context context, String name, boolean defValue) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getBoolean(name, defValue);
	}
	
	private static boolean getBoolean(Context context, String key) {
		return getBoolean(context, key, false);
	}
}