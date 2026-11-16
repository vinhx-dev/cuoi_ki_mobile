package com.example.ecom.core.data;

import android.content.Context;

/** Phiên đăng nhập (token + tên user), lưu trong LocalStorage. */
public class SessionManager {

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "user_name";

    private final LocalStorage storage;

    public SessionManager(Context context) {
        this.storage = new LocalStorage(context);
    }

    public void saveSession(String token, String userName) {
        storage.set(KEY_TOKEN, token);
        storage.set(KEY_USER_NAME, userName);
    }

    public String getToken() {
        return storage.get(KEY_TOKEN);
    }

    public String getUserName() {
        String name = storage.get(KEY_USER_NAME);
        return name != null ? name : "";
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /** Đăng xuất: chỉ xoá phiên đăng nhập, giữ lại dữ liệu khác trong LocalStorage (vd. cài đặt). */
    public void clear() {
        storage.remove(KEY_TOKEN);
        storage.remove(KEY_USER_NAME);
    }
}
