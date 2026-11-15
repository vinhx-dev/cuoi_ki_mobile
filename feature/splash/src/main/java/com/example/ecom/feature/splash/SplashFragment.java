package com.example.ecom.feature.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.data.repository.AuthRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

/** Trang khởi động: kiểm tra đã đăng nhập chưa rồi điều hướng. */
public class SplashFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean loggedIn = new AuthRepository(requireContext()).isLoggedIn();
        view.postDelayed(() -> {
            if (!isUiActive()) return;
            if (loggedIn) AppRouter.toHome(this);
            else AppRouter.toLogin(this);
        }, 500);
    }
}
