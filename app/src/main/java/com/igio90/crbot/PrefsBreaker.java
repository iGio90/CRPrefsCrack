package com.igio90.crbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by igio90 on 03/10/17.
 */

class PrefsBreaker {
    private final Cipher mUnknownCipherTwo;
    private final Cipher mUnknownCipherThree;
    private final Cipher mUnknownCipherFour;
    private final SharedPreferences mPrefs;

    /**
     * CR Prefs + key 1.9.3:
     *
     * storage 4c3c4f0b854f0b3a
     * storage_new 3;G39;A:<N6MI9726MM<AGE35
     * local_prefs 4c3c4f0b854f0b3a
     *
     * Basic usage:
     *
     * PrefsBreaker prefsBreaker = new PrefsBreaker(this, "storage", "4c3c4f0b854f0b3a");
     * for (Map.Entry<String, ?> entry : prefsBreaker.getMap().entrySet()) {
     *     String key = entry.getKey();
     *     String decodedKey = prefsBreaker.decodeKey(key);
     *     String value = prefsBreaker.getString(key, true);
     * }
     */
    PrefsBreaker(final Context context, final String prefsName, final String key) {
            try {
                Cipher unknownCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                mUnknownCipherTwo = Cipher.getInstance("AES/CBC/PKCS5Padding");
                mUnknownCipherThree = Cipher.getInstance("AES/ECB/PKCS5Padding");
                mUnknownCipherFour = Cipher.getInstance("AES/ECB/PKCS5Padding");
                final byte[] array = new byte[unknownCipher.getBlockSize()];
                System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, array, 0, unknownCipher.getBlockSize());
                final IvParameterSpec ivParameterSpec = new IvParameterSpec(array);
                final MessageDigest instance = MessageDigest.getInstance("SHA-256");
                instance.reset();
                final SecretKeySpec secretKeySpec = new SecretKeySpec(instance.digest(key.getBytes("UTF-8")), "AES/CBC/PKCS5Padding");
                unknownCipher.init(1, secretKeySpec, ivParameterSpec);
                mUnknownCipherTwo.init(2, secretKeySpec, ivParameterSpec);
                mUnknownCipherThree.init(1, secretKeySpec);
                mUnknownCipherFour.init(2, secretKeySpec);
                mPrefs = context.getSharedPreferences(prefsName, 0);
            } catch (GeneralSecurityException | UnsupportedEncodingException ignored) {
                throw new RuntimeException("err");
            }
    }

    private String encodeKey(final String key) {
        return encodeCipher(key, mUnknownCipherThree);
    }

    String decodeKey(String key) {
        return decodeCipher(key, mUnknownCipherFour);
    }

    private String encodeCipher(final String value, final Cipher cipher) {
        try {
            return Base64.encodeToString(finalize(cipher, value.getBytes("UTF-8")), 2);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String decodeCipher(final String value, final Cipher cipher) {
        try {
            byte[] b = Base64.decode(value.getBytes("UTF-8"), 2);
            return new String(finalize(cipher, b));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private byte[] finalize(final Cipher cipher, final byte[] array) {
        try {
            return cipher.doFinal(array);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    final String getString(String key, boolean encoded) {
        if (!encoded) {
            key = encodeKey(key);
        }
        if (mPrefs.contains(key)) {
            final String b = decodeCipher(mPrefs.getString(key, ""), mUnknownCipherTwo);
            if (b.isEmpty()) {
                mPrefs.edit().remove(key).apply();
            }
            return b;
        }
        return "";
    }

    Map<String, ?> getMap() {
        return mPrefs.getAll();
    }
}
