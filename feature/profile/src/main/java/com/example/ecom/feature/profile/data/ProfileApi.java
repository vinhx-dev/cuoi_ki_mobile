package com.example.ecom.feature.profile.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;
import com.example.ecom.core.data.SessionManager;

import org.json.JSONObject;

/** Thông tin tài khoản + đăng xuất. */
public class ProfileApi {

    private final ApiClient api;
    private final SessionManager session;

    public ProfileApi(Context context) {
        this.api = ApiClient.getInstance(context);
        this.session = new SessionManager(context);
    }

    /** GET /users/me */
    public void getMe(ApiCallback<User> cb) {
        api.get("/users/me", null, data -> User.fromJson((JSONObject) data), cb);
    }

    /** Đăng xuất: chỉ cần xoá phiên đăng nhập trên máy. */
    public void logout() {
        session.clear();
    }
}
