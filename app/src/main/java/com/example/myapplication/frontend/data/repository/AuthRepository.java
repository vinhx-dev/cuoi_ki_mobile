package com.example.myapplication.frontend.data.repository;

import android.content.Context;

import com.example.myapplication.frontend.data.api.ApiCallback;
import com.example.myapplication.frontend.data.api.ApiClient;
import com.example.myapplication.frontend.data.api.ApiEndpoints;
import com.example.myapplication.frontend.data.model.AuthResult;
import com.example.myapplication.frontend.data.model.User;
import com.example.myapplication.frontend.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Repository phía frontend: UI chỉ gọi các hàm ở đây, không gọi ApiClient trực tiếp.
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
