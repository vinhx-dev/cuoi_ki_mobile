package com.example.ecom.core.data.repository;

import android.content.Context;

import com.example.ecom.core.data.api.ApiCallback;
import com.example.ecom.core.data.api.ApiClient;
import com.example.ecom.core.data.api.ApiEndpoints;
import com.example.ecom.core.data.model.AuthResult;
import com.example.ecom.core.data.model.User;
import com.example.ecom.core.data.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Đăng nhập / đăng ký / phiên đăng nhập. UI chỉ gọi các hàm ở đây, không gọi ApiClient trực tiếp.
 */
public class AuthRepository {

    private final ApiClient api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        this.api = ApiClient.getInstance(context);
        this.session = new SessionManager(context);
    }

    public void login(String phone, String password, ApiCallback<User> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone).put("password", password);
        } catch (JSONException ignored) { }
        api.post(ApiEndpoints.LOGIN, body, data -> AuthResult.fromJson((JSONObject) data), saveSession(cb));
    }

    public void register(String name, String phone, String password, ApiCallback<User> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("name", name).put("phone", phone).put("password", password);
        } catch (JSONException ignored) { }
        api.post(ApiEndpoints.REGISTER, body, data -> AuthResult.fromJson((JSONObject) data), saveSession(cb));
    }

    public void getMe(ApiCallback<User> cb) {
        api.get(ApiEndpoints.ME, null, data -> User.fromJson((JSONObject) data), cb);
    }

    public void logout() {
        session.clear();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    public String getUserName() {
        return session.getUserName();
    }

    private ApiCallback<AuthResult> saveSession(ApiCallback<User> cb) {
        return new ApiCallback<AuthResult>() {
            @Override
            public void onSuccess(AuthResult result) {
                session.saveSession(result.token, result.user.name);
                cb.onSuccess(result.user);
            }

            @Override
            public void onError(String message, int statusCode) {
                cb.onError(message, statusCode);
            }
        };
    }
}
