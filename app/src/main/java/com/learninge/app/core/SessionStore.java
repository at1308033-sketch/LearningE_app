package com.learninge.app.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Người dùng cục bộ đang đăng nhập (BR-004). Token nằm ở {@code data.remote.TokenStore}.
 */
public final class SessionStore {
    public static final long NO_USER = -1;

    private static final String FILE = "session";
    private static final String KEY_USER_ID = "user_id";

    private final Context context;

    public SessionStore(Context context) {
        this.context = context.getApplicationContext();
    }

    public long userId() {
        return prefs().getLong(KEY_USER_ID, NO_USER);
    }

    public boolean isSignedIn() {
        return userId() != NO_USER;
    }

    public void signIn(long userId) {
        prefs().edit().putLong(KEY_USER_ID, userId).apply();
    }

    public void signOut() {
        prefs().edit().remove(KEY_USER_ID).apply();
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
