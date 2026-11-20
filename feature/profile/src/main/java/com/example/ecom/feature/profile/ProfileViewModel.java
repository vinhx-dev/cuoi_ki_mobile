package com.example.ecom.feature.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.profile.data.ProfileApi;
import com.example.ecom.feature.profile.data.User;

/** ≈ useProfile(): thông tin tài khoản + thống kê + đăng xuất. */
public class ProfileViewModel extends BaseViewModel {

    private final ProfileApi profileApi;
    private final MutableLiveData<UiState<User>> user = new MutableLiveData<>();
    private final MutableLiveData<UiState<ProfileApi.ShoppingStats>> stats = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application app) {
        super(app);
        profileApi = new ProfileApi(app);
    }

    public LiveData<UiState<User>> getUser() {
        return user;
    }

    public LiveData<UiState<ProfileApi.ShoppingStats>> getStats() {
        return stats;
    }

    public void load() {
        fetch(user, profileApi::getMe);
        fetch(stats, profileApi::getShoppingStats);
    }

    public void logout(ApiCallback<Object> cb) {
        profileApi.logout(cb);
    }
}
