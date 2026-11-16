package com.example.ecom.feature.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Trang cá nhân người dùng (Tôi). ≈ ProfilePage.jsx */
public class ProfileFragment extends BaseFragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel vm = viewModel(ProfileViewModel.class);

        // Thiết lập Bottom Navigation Bar
        BottomNavigationView bottomBar = view.findViewById(com.example.ecom.core.ui.R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setSelectedItemId(com.example.ecom.core.ui.R.id.tabProfile);
            bottomBar.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == com.example.ecom.core.ui.R.id.tabHome) {
                    AppRouter.toTab(this, "home");
                } else if (itemId == com.example.ecom.core.ui.R.id.tabOrders) {
                    AppRouter.toTab(this, "orders");
                } else if (itemId == com.example.ecom.core.ui.R.id.tabProfile) {
                    AppRouter.toTab(this, "profile");
                }
                return false;
            });
        }

        View btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> toast("Cài đặt tài khoản"));
        }

        TextView textName = view.findViewById(R.id.textName);
        TextView textMemberRank = view.findViewById(R.id.textMemberRank);
        TextView textPhone = view.findViewById(R.id.textPhone);

        TextView textTotalOrdersCount = view.findViewById(R.id.textTotalOrdersCount);
        TextView textTotalPaidAmount = view.findViewById(R.id.textTotalPaidAmount);
        TextView textActiveOrdersCount = view.findViewById(R.id.textActiveOrdersCount);

        View btnLogoutItem = view.findViewById(R.id.btnLogoutItem);
        if (btnLogoutItem != null) {
            btnLogoutItem.setOnClickListener(v -> {
                vm.logout();
                AppRouter.toLogin(this);
            });
        }

        // Render thông tin người dùng
        observe(vm.getUser(), user -> {
            if (textName != null) textName.setText(user.name);
            if (textMemberRank != null) textMemberRank.setText("Thành viên thân thiết");
            if (textPhone != null) {
                String contact = !user.phone.isEmpty() ? user.phone : "user@email.com";
                textPhone.setText(contact);
            }
            if (textTotalOrdersCount != null) textTotalOrdersCount.setText("12 đơn");
            if (textTotalPaidAmount != null) textTotalPaidAmount.setText("3.250.000đ");
            if (textActiveOrdersCount != null) textActiveOrdersCount.setText("2 đơn");
        });

        vm.load();
    }
}
