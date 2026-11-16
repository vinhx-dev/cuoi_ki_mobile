package com.example.ecom.core.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu dữ liệu nhỏ trên máy, vẫn còn sau khi tắt app. ≈ localStorage trong trình duyệt.
 *
 *   React:   localStorage.setItem("token", t);   localStorage.getItem("token");
 *   Android: storage.set("token", t);            storage.get("token");
 *
 * Bên dưới dùng SharedPreferences của Android.
 */
public class LocalStorage {

    private static final String FILE_NAME = "ecom_app_prefs";

    private final SharedPreferences prefs;

    public LocalStorage(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** ≈ localStorage.getItem(key); trả null nếu chưa có. */
    public String get(String key) {
        return prefs.getString(key, null);
    }

    /** ≈ localStorage.setItem(key, value) */
    public void set(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    /** ≈ localStorage.removeItem(key) */
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    /** ≈ localStorage.clear() */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
