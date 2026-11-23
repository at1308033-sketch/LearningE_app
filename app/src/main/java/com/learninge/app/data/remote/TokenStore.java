package com.learninge.app.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Token phiên trong SharedPreferences riêng tư, mã hóa AES-GCM bằng khóa Android Keystore (NFR-S4, TM-06).
 * Không ghi token ra log. Gọi trên luồng nền.
 */
public final class TokenStore {
    private static final String TAG = "TokenStore";
    private static final String FILE = "token";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IV = "iv";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "learninge_token";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;

    private final Context context;
    private String cached;
    private boolean isLoaded;

    public TokenStore(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * null nếu chưa có token.
     */
    public synchronized String token() {
        if (!isLoaded) {
            cached = read();
            isLoaded = true;
        }
        return cached;
    }

    public synchronized void save(String token, long expiresAtMillis) {
        cached = token;
        isLoaded = true;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            prefs().edit()
                    .putString(KEY_TOKEN, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                    .putString(KEY_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                    .putLong(KEY_EXPIRES_AT, expiresAtMillis)
                    .apply();
        } catch (GeneralSecurityException | IOException unavailable) {
            // Keystore hỏng: phiên vẫn dùng được tới khi tắt app, lần sau đăng nhập lại.
            Log.w(TAG, "cannot encrypt token: " + unavailable.getClass().getSimpleName());
            prefs().edit().clear().apply();
        }
    }

    public synchronized void clear() {
        cached = null;
        isLoaded = true;
        prefs().edit().clear().apply();
    }

    private String read() {
        String encrypted = prefs().getString(KEY_TOKEN, null);
        String iv = prefs().getString(KEY_IV, null);
        if (encrypted == null || iv == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key(),
                    new GCMParameterSpec(GCM_TAG_BITS, Base64.decode(iv, Base64.NO_WRAP)));
            return new String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException unreadable) {
            Log.w(TAG, "cannot decrypt token: " + unreadable.getClass().getSimpleName());
            return null;
        }
    }

    private static SecretKey key() throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance(KEYSTORE);
        store.load(null);
        SecretKey existing = (SecretKey) store.getKey(KEY_ALIAS, null);
        if (existing != null) {
            return existing;
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
