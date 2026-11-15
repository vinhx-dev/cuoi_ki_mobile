package com.example.ecom.feature.auth;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.data.repository.AuthRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

public class RegisterFragment extends BaseFragment {

    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPasswordReg;
    private EditText inputRePassword;
    private CheckBox checkboxTerms;
    private TextView tvError;
    private AuthRepository authRepository;

    private boolean isPass1Visible = false;
    private boolean isPass2Visible = false;

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

        inputUsername = view.findViewById(R.id.inputUsername);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputPasswordReg = view.findViewById(R.id.inputPasswordReg);
        inputRePassword = view.findViewById(R.id.inputRePassword);
        checkboxTerms = view.findViewById(R.id.checkboxTerms);
        tvError = view.findViewById(R.id.tvError);

        ImageView btnTogglePass1 = view.findViewById(R.id.btnTogglePass1);
        ImageView btnTogglePass2 = view.findViewById(R.id.btnTogglePass2);
        Button btnRegisterSubmit = view.findViewById(R.id.btnRegisterSubmit);
        TextView textGoLogin = view.findViewById(R.id.textGoLogin);

        if (btnTogglePass1 != null) {
            btnTogglePass1.setOnClickListener(v -> {
                isPass1Visible = !isPass1Visible;
                togglePasswordVisibility(inputPasswordReg, btnTogglePass1, isPass1Visible);
            });
        }

        if (btnTogglePass2 != null) {
            btnTogglePass2.setOnClickListener(v -> {
                isPass2Visible = !isPass2Visible;
                togglePasswordVisibility(inputRePassword, btnTogglePass2, isPass2Visible);
            });
        }

        if (btnRegisterSubmit != null) {
            btnRegisterSubmit.setOnClickListener(v -> register());
        }

        if (textGoLogin != null) {
            textGoLogin.setOnClickListener(v -> AppRouter.back(this));
        }
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean show) {
        if (show) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_eye);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_eye_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void register() {
        if (tvError != null) tvError.setVisibility(View.GONE);

        String name = inputUsername.getText().toString().trim();
        String phone = inputEmail.getText().toString().trim();
        String password = inputPasswordReg.getText().toString();
        String rePassword = inputRePassword.getText().toString();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            showError(getString(R.string.err_empty_fields));
            return;
        }

        if (!password.equals(rePassword)) {
            showError(getString(R.string.err_password_match));
            return;
        }

        if (checkboxTerms != null && !checkboxTerms.isChecked()) {
            showError(getString(R.string.err_terms_not_agreed));
            return;
        }

        authRepository.register(name, phone, password, apiCallback(user -> {
            toast(getString(R.string.msg_register_success));
            AppRouter.toHome(this);
        }));
    }

    private void showError(String message) {
        if (tvError != null) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(message);
        } else {
            toast(message);
        }
    }
}
