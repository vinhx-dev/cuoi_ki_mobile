package com.example.ecom.feature.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.data.repository.AuthRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

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
            textPhone.setText(getString(R.string.profile_phone, user.phone));
            String address = user.address.isEmpty() ? getString(R.string.profile_no_address) : user.address;
            textAddress.setText(getString(R.string.profile_address, address));
        }));
    }
}
