package com.example.myapplication.frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Lưu phiên đăng nhập (token + thông tin user cơ bản) vào SharedPreferences. */
public class SessionManager {

    private static final String PREF_NAME = "ecom_app_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userName) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_USER_NAME, userName).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
