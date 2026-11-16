package com.example.ecom.feature.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.profile.data.ProfileApi;
import com.example.ecom.feature.profile.data.User;

/** ≈ useProfile(): thông tin tài khoản + đăng xuất. */
public class ProfileViewModel extends BaseViewModel {

    private final ProfileApi profileApi;
    private final MutableLiveData<UiState<User>> user = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application app) {
        super(app);
        profileApi = new ProfileApi(app);
    }

    public LiveData<UiState<User>> getUser() {
        return user;
    }

    public void load() {
        if (user.getValue() != null) return;
        fetch(user, profileApi::getMe);
    }

    public void logout() {
        profileApi.logout();
    }
}
