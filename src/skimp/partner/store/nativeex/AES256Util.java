package skimp.partner.store.nativeex;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 양방향 암호화 알고리즘인 AES256 암호화를 지원하는 클래스
 */

public class AES256Util {
//    public static byte[] iv = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 };
    public static byte[] iv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    // 사용자 지정 키로 AES256 암호화
    public static String encrypt(String key, String plainText) throws Exception {
        return encByKey(key.getBytes(), plainText.getBytes());
    }

    // 사용자 지정 키로 AES256 복호화
    public static String encByKey(byte[] key, byte[] plainByte) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] randomKey = cipher.doFinal(plainByte);
        return Base64.encodeToString(randomKey, Base64.NO_WRAP);
    }

    // 사용자 지정 키로 AES256 복호화
    public static String decrypt(String key, String encText) throws Exception {
        return decByKey(key.getBytes(), Base64.decode(encText, Base64.NO_WRAP));
    }

    // 사용자 지정 키로 AES256 복호화
    public static String decByKey(byte[] key, byte[] encText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] secureKey = cipher.doFinal(encText);
        return new String(secureKey);
    }
}