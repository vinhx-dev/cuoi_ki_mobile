package com.example.ecom.core.data;

import android.content.Context;

/** Phiên đăng nhập (token + tên user), kết nối với TokenManager. */
public class SessionManager {

    private final TokenManager tokenManager;

    public SessionManager(Context context) {
        this.tokenManager = TokenManager.getInstance(context);
    }

    public void saveSession(String token, String userName) {
        tokenManager.saveTokens(token, null);
    }

    public String getToken() {
        return tokenManager.getAccessToken();
    }

    public String getUserName() {
        return tokenManager.getUserName();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public void clear() {
        tokenManager.clear();
    }
}
