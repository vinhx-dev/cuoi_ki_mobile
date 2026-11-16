package com.example.ecom.feature.auth.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;
import com.example.ecom.core.data.SessionManager;

import org.json.JSONObject;

/** Đăng nhập / đăng ký. Thành công thì lưu phiên đăng nhập (token + tên) vào máy. ≈ api/auth.js */
public class AuthApi {

    private final ApiClient api;
    private final SessionManager session;

    public AuthApi(Context context) {
        this.api = ApiClient.getInstance(context);
        this.session = new SessionManager(context);
    }

    /** POST /auth/login -> trả về tên người dùng */
    public void login(String phone, String password, ApiCallback<String> cb) {
        api.post("/auth/login", ApiClient.json("phone", phone, "password", password),
                this::saveSession, cb);
    }

    /** POST /auth/register -> trả về tên người dùng */
    public void register(String name, String phone, String password, ApiCallback<String> cb) {
        api.post("/auth/register", ApiClient.json("name", name, "phone", phone, "password", password),
                this::saveSession, cb);
    }

    /** data = { token, user: { name, ... } } */
    private String saveSession(Object data) throws org.json.JSONException {
        JSONObject json = (JSONObject) data;
        String token = json.getString("token");
        String name = json.getJSONObject("user").optString("name");
        session.saveSession(token, name);
        return name;
    }
}
