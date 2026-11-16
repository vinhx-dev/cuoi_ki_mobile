package com.example.ecom.feature.auth.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.auth.R;

/** Đăng nhập. ≈ LoginPage.jsx */
public class LoginFragment extends BaseFragment {

    private EditText inputAccount;
    private EditText inputPassword;
    private LoginViewModel vm;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = viewModel(LoginViewModel.class);

        inputAccount = view.findViewById(R.id.inputAccount);
        inputPassword = view.findViewById(R.id.inputPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView textRegister = view.findViewById(R.id.textRegister);
        TextView textForgot = view.findViewById(R.id.textForgot);
        View layoutSocial = view.findViewById(R.id.layoutSocialIcons);

        btnLogin.setOnClickListener(v -> login());
        if (textRegister != null) {
            textRegister.setOnClickListener(v -> AppRouter.toRegister(this));
        }
        if (textForgot != null) {
            textForgot.setOnClickListener(v -> toast(getString(R.string.msg_forgot_coming_soon)));
        }
        if (layoutSocial != null) {
            layoutSocial.setOnClickListener(v -> toast(getString(R.string.msg_social_coming_soon)));
        }
    }

    private void login() {
        String phone = inputAccount.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (phone.isEmpty() || password.isEmpty()) {
            toast(getString(R.string.err_empty_fields));
            return;
        }

        vm.login(phone, password, apiCallback(userName -> {
            toast(getString(R.string.msg_hello, userName));
            AppRouter.toHome(this);
        }));
    }
}
