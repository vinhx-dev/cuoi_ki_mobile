package com.example.ecom.core.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Quản lý lưu trữ phiên đăng nhập: Access Token, Refresh Token, Thông tin User.
 */
public class TokenManager {

    private static final String PREF_NAME = "auth_session_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_JSON = "user_json";

    private static volatile TokenManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context);
                }
            }
        }
        return instance;
    }

    public synchronized void saveAuthResponse(AuthResponse response) {
        if (response == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        if (response.accessToken != null) {
            editor.putString(KEY_ACCESS_TOKEN, response.accessToken);
        }
        if (response.refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, response.refreshToken);
        }
        if (response.user != null) {
            editor.putString(KEY_USER_JSON, gson.toJson(response.user));
        }
        editor.apply();
    }

    public synchronized void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        if (accessToken != null) editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public synchronized void saveUser(User user) {
        if (user != null) {
            prefs.edit().putString(KEY_USER_JSON, gson.toJson(user)).apply();
        }
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserName() {
        User u = getUser();
        return u != null ? u.getDisplayName() : "";
    }

    public String getUserEmail() {
        User u = getUser();
        return u != null ? u.email : "";
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && !getAccessToken().isEmpty();
    }

    public synchronized void clear() {
        prefs.edit().clear().apply();
    }
}
