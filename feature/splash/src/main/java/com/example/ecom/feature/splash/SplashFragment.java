package com.example.ecom.feature.splash;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

/**
 * Trang khởi động. ≈ SplashPage.jsx
 * Chỉ cần mở trang chủ: trang chủ khai báo LOGIN_REQUIRED trong AppPages,
 * nên chưa đăng nhập thì AppRouter tự chuyển sang trang Login.
 */
public class SplashFragment extends BaseFragment {

    public SplashFragment() {
        super(R.layout.fragment_splash);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> {
            if (isUiActive()) AppRouter.toHome(this);
        }, 500);
    }
}
