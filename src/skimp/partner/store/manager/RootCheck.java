package skimp.partner.store.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2018-04-30.
 */

public class RootCheck {

    private static final String[] ENGMODE_STRING = {"test-keys"};
    private static final String[] ROOTING_APK = {"/system/app/SuperUser.apk", "/mnt/sdcard/system/app/SuperUser.apk"};
    private static final String[] ROOTING_PACKAGE =
            {"com.flywithu.fakeroot", "com.noshufou.android.su"};
    public static String[] ROOTING_FODLER = {
        "/sbin/su",
        "/su/bin/su",
        "/system/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/system/app/Superuser.apk",
        "/system/usr/su-backup",
        "/system/xbin/mu",
        "/system/bin/.ext/.su",
        "/system/app/Superuser.apk",
        "/system/app/su.apk",
        "/system/app/Spapasu.apk",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/data/data/com.noshufou.android.su",
        "/mnt/sdcard/system/bin/su",
        "/mnt/sdcard/system/xbin/su",
        "/mnt/sdcard/system/app/SuperUser.apk",
        "/mnt/sdcard/data/data/com.noshufou.android.su"
    };


    public static boolean isDeviceRooted( Context context ) {
        return checkEngMode() || checkRootingApk() || checkFolder() || checkPackage(context);
    }

    /* 루팅이 된 기기는 일반적으로 Build.TAGS 값이 제조사 키값이 아닌 test 키 값을 가지고 있습니다. */
    private static boolean checkTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public static boolean checkEngMode() {
        boolean bIsRoot = false;
        String sBuildTags = Build.TAGS;
        if (sBuildTags != null && sBuildTags.length() > 0) {
            String[] var6 = ENGMODE_STRING;
            int var5 = ENGMODE_STRING.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                String sEndMode = var6[var4];
                Log.d("rootCheck", "[checkEngMode] -> sEndMode = " + sEndMode);
                if (sBuildTags.equals(sEndMode)) {
                    bIsRoot = true;
                    break;
                }
            }
        }

        Log.d("rootCheck", "[checkEngMode] -> bIsRoot = " + bIsRoot);
        return bIsRoot;
    }

    public static boolean checkRootingApk() {
        boolean bIsRoot = false;
        String[] var5 = ROOTING_APK;
        int var4 = ROOTING_APK.length;

        for(int var3 = 0; var3 < var4; ++var3) {
            String sRootingApk = var5[var3];
            Log.d("rootCheck", "[checkRootingApk] -> sRootingApk = " + sRootingApk);
            File oFile = new File(sRootingApk);
            if (oFile != null && oFile.exists()) {
                bIsRoot = true;
                break;
            }
        }

        Log.d("rootCheck", "[checkRootingApk] -> bIsRoot = " + bIsRoot);
        return bIsRoot;
    }


    private static boolean checkFolder() {
        boolean bIsRoot = false;
        String[] var5 = ROOTING_FODLER;
        int var4 = ROOTING_FODLER.length;

//        Logger.d("################# rootCheck ##################");
        for(int var3 = 0; var3 < var4; ++var3) {
            String sFolder = var5[var3];
//            Logger.d("[checkFolder] -> sFolder = " + sFolder);
            File oFile = new File(sFolder);
            if (oFile.exists()) {
                bIsRoot = true;
                break;
            }
        }

//        Logger.d("[checkFolder] -> bIsRoot = " + bIsRoot);
//        Logger.d("#############################################");
        return bIsRoot;
    }


    public static boolean checkPackage(Context a_oContext) {
        boolean bIsRoot = false;
        String[] var6 = ROOTING_PACKAGE;
        int var5 = ROOTING_PACKAGE.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            String sPackage = var6[var4];

            try {
                PackageInfo oInfo = a_oContext.getPackageManager().getPackageInfo(sPackage, 0);
                if (oInfo != null) {
                    bIsRoot = true;
                    break;
                }
            } catch (Exception var8) {
                Log.d("rootCheck", var8.toString());
            }
        }

        Log.d("rootCheck", "[checkPackage] -> bIsRoot = " + bIsRoot);
        return bIsRoot;
    }


}
