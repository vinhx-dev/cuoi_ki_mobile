package com.example.ecom.feature.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.data.repository.AuthRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

public class LoginFragment extends BaseFragment {

    private EditText inputAccount;
    private EditText inputPassword;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        inputAccount = view.findViewById(R.id.inputAccount);
        inputPassword = view.findViewById(R.id.inputPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView textRegister = view.findViewById(R.id.textRegister);
        TextView textForgot = view.findViewById(R.id.textForgot);
        View layoutSocial = view.findViewById(R.id.layoutSocialIcons);

        view.findViewById(R.id.btnAdminPreview).setOnClickListener(v -> AppRouter.toAdminDashboard(this));

        btnLogin.setOnClickListener(v -> login());
        if (textRegister != null) {
            textRegister.setOnClickListener(v -> AppRouter.toRegister(this));
        }
        if (textForgot != null) {
            textForgot.setOnClickListener(v -> toast("Tính năng quên mật khẩu đang được phát triển"));
        }
        if (layoutSocial != null) {
            layoutSocial.setOnClickListener(v -> toast("Đăng nhập mạng xã hội đang được phát triển"));
        }
    }

    private void login() {
        String phone = inputAccount.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (phone.isEmpty() || password.isEmpty()) {
            toast(getString(R.string.err_empty_fields));
            return;
        }

        authRepository.login(phone, password, apiCallback(user -> {
            toast(getString(R.string.msg_hello, user.name));
            AppRouter.toHome(this);
        }));
    }
}
