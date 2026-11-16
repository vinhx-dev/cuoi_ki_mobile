package com.example.ecom.feature.auth.login;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.feature.auth.data.AuthApi;

/** ≈ useLogin(): chỉ có hành động đăng nhập, không có dữ liệu cần hiển thị. */
public class LoginViewModel extends BaseViewModel {

    private final AuthApi authApi;

    public LoginViewModel(@NonNull Application app) {
        super(app);
        authApi = new AuthApi(app);
    }

    public void login(String phone, String password, ApiCallback<String> callback) {
        authApi.login(phone, password, callback);
    }
}
