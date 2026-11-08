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

public class RegisterFragment extends BaseFragment {

    private EditText inputName;
    private EditText inputSdt;
    private EditText inputPassword;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        inputName = view.findViewById(R.id.inputName);
        inputSdt = view.findViewById(R.id.inputSdt);
        inputPassword = view.findViewById(R.id.inputPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView textGoLogin = view.findViewById(R.id.textGoLogin);

        btnRegister.setOnClickListener(v -> register());
        textGoLogin.setOnClickListener(v -> AppRouter.back(this));
    }

    private void register() {
        authRepository.register(
                inputName.getText().toString(),
                inputSdt.getText().toString(),
                inputPassword.getText().toString(),
                apiCallback(user -> {
                    toast("Đăng ký thành công");
                    AppRouter.toHome(this);
                }));
    }
}
