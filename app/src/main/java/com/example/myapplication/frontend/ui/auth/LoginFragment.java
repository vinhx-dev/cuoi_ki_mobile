package com.example.myapplication.frontend.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.frontend.navigation.AppRouter;
import com.example.myapplication.frontend.ui.base.BaseFragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.frontend.data.repository.AuthRepository;

public class LoginFragment extends BaseFragment {

    private EditText inputSdt;
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

        inputSdt = view.findViewById(R.id.inputSdt);
        inputPassword = view.findViewById(R.id.inputPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView textGoRegister = view.findViewById(R.id.textGoRegister);

        btnLogin.setOnClickListener(v -> login());
        textGoRegister.setOnClickListener(v -> AppRouter.toRegister(this));
    }

    private void login() {
        String phone = inputSdt.getText().toString().trim();
        String password = inputPassword.getText().toString();

        authRepository.login(phone, password, apiCallback(user -> {
            toast("Xin chào " + user.name);
            AppRouter.toHome(this);
        }));
    }
}
