package com.example.ecom.feature.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

/** Trang tài khoản. ≈ ProfilePage.jsx */
public class ProfileFragment extends BaseFragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel vm = viewModel(ProfileViewModel.class);

        TextView textName = view.findViewById(R.id.textName);
        TextView textPhone = view.findViewById(R.id.textPhone);
        TextView textAddress = view.findViewById(R.id.textAddress);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Sự kiện
        btnLogout.setOnClickListener(v -> {
            vm.logout();
            AppRouter.toLogin(this);
        });

        // Render
        observe(vm.getUser(), user -> {
            textName.setText(user.name);
            textPhone.setText(getString(R.string.profile_phone, user.phone));
            String address = user.address.isEmpty() ? getString(R.string.profile_no_address) : user.address;
            textAddress.setText(getString(R.string.profile_address, address));
        });

        vm.load();
    }
}
