package com.example.myapplication.frontend.ui.profile;

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
import android.widget.TextView;

import com.example.myapplication.frontend.data.repository.AuthRepository;

public class ProfileFragment extends BaseFragment {

    private AuthRepository authRepository;
    private TextView textName, textPhone, textAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        textName = view.findViewById(R.id.textName);
        textPhone = view.findViewById(R.id.textPhone);
        textAddress = view.findViewById(R.id.textAddress);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            AppRouter.toLogin(this);
        });

        authRepository.getMe(apiCallback(user -> {
            textName.setText(user.name);
            textPhone.setText("SĐT: " + user.phone);
            textAddress.setText("Địa chỉ: " + (user.address.isEmpty() ? "(chưa có)" : user.address));
        }));
    }
}
