package com.example.ecom.feature.auth.register;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.feature.auth.data.AuthApi;

/** ≈ useRegister(): chỉ có hành động đăng ký. */
public class RegisterViewModel extends BaseViewModel {

    private final AuthApi authApi;

    public RegisterViewModel(@NonNull Application app) {
        super(app);
        authApi = new AuthApi(app);
    }

    public void register(String name, String phone, String password, ApiCallback<String> callback) {
        authApi.register(name, phone, password, callback);
    }
}
