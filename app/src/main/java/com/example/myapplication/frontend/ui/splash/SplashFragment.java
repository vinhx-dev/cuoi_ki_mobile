package com.example.myapplication.frontend.ui.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.frontend.navigation.AppRouter;
import com.example.myapplication.frontend.ui.base.BaseFragment;
import com.example.myapplication.frontend.utils.SessionManager;

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
        boolean loggedIn = new SessionManager(requireContext()).isLoggedIn();
        view.postDelayed(() -> {
            if (!isUiActive()) return;
            if (loggedIn) AppRouter.toHome(this);
            else AppRouter.toLogin(this);
        }, 500);
    }
}
