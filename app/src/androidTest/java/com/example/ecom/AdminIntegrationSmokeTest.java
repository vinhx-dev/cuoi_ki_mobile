package com.example.ecom;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.activities.AdminActivity;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/** Exercises the migrated admin host without changing saved products, orders, or customer auth. */
public final class AdminIntegrationSmokeTest extends Instrumentation {
    private AdminActivity activity;
    private NavController nav;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private void destination(int expected, boolean footer) {
        waitForIdleSync();
        runOnMainSync(() -> {
            check(nav.getCurrentDestination().getId() == expected, "Unexpected admin destination");
            View bottom = activity.findViewById(com.example.ecom.feature.admin.R.id.adminBottomNav);
            check((bottom.getVisibility() == View.VISIBLE) == footer, "Admin footer visibility");
        });
    }

    private void tab(int id) {
        runOnMainSync(() -> activity.findViewById(id).performClick());
        destination(id, true);
    }

    private void back() {
        runOnMainSync(() -> activity.findViewById(com.example.ecom.feature.admin.R.id.adminMenu).performClick());
        waitForIdleSync();
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        String access = new com.example.ecom.core.data.TokenManager(getTargetContext()).getAccessToken();
        String refresh = new com.example.ecom.core.data.TokenManager(getTargetContext()).getRefreshToken();
        try {
            Intent launch = new Intent(getTargetContext(), AdminActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity = (AdminActivity) startActivitySync(launch);
            waitForIdleSync();
            runOnMainSync(() -> nav = ((NavHostFragment) activity.getSupportFragmentManager()
                    .findFragmentById(com.example.ecom.feature.admin.R.id.adminNavHost)).getNavController());
            destination(com.example.ecom.feature.admin.R.id.adminDashboardFragment, true);

            tab(com.example.ecom.feature.admin.R.id.adminProductsFragment);
            runOnMainSync(() -> activity.findViewById(com.example.ecom.feature.admin.R.id.btnAddProduct).performClick());
            destination(com.example.ecom.feature.admin.R.id.adminProductFormFragment, false);
            runOnMainSync(() -> {
                activity.findViewById(com.example.ecom.feature.admin.R.id.btnSaveProduct).performClick();
                TextInputLayout name = activity.findViewById(com.example.ecom.feature.admin.R.id.inputNameLayout);
                check(name.getError() != null, "Blank product name must be rejected");
                activity.findViewById(com.example.ecom.feature.admin.R.id.btnCancelProduct).performClick();
            });
            destination(com.example.ecom.feature.admin.R.id.adminProductsFragment, true);

            AtomicInteger product = new AtomicInteger();
            AtomicInteger order = new AtomicInteger();
            AtomicInteger customer = new AtomicInteger();
            runOnMainSync(() -> {
                AdminViewModel vm = new ViewModelProvider(activity).get(AdminViewModel.class);
                if (!vm.getProducts().isEmpty()) product.set(vm.getProducts().get(0).id);
                if (!vm.getOrders().isEmpty()) order.set(vm.getOrders().get(0).id);
                if (!vm.getCustomers().isEmpty()) customer.set(vm.getCustomers().get(0).id);
            });
            if (product.get() != 0) {
                runOnMainSync(() -> nav.navigate(com.example.ecom.feature.admin.R.id.adminProductDetailFragment,
                        id(AdminRouter.ARG_ADMIN_PRODUCT_ID, product.get())));
                destination(com.example.ecom.feature.admin.R.id.adminProductDetailFragment, false);
                runOnMainSync(() -> activity.findViewById(com.example.ecom.feature.admin.R.id.btnEditProduct).performClick());
                destination(com.example.ecom.feature.admin.R.id.adminProductFormFragment, false);
                back();
                destination(com.example.ecom.feature.admin.R.id.adminProductDetailFragment, false);
                back();
                destination(com.example.ecom.feature.admin.R.id.adminProductsFragment, true);
            }

            tab(com.example.ecom.feature.admin.R.id.adminOrdersFragment);
            if (order.get() != 0) {
                runOnMainSync(() -> nav.navigate(com.example.ecom.feature.admin.R.id.adminOrderDetailFragment,
                        id(AdminRouter.ARG_ADMIN_ORDER_ID, order.get())));
                destination(com.example.ecom.feature.admin.R.id.adminOrderDetailFragment, false);
                back();
                destination(com.example.ecom.feature.admin.R.id.adminOrdersFragment, true);
            }
            tab(com.example.ecom.feature.admin.R.id.adminRevenueFragment);
            tab(com.example.ecom.feature.admin.R.id.adminUsersFragment);
            if (customer.get() != 0) {
                runOnMainSync(() -> nav.navigate(com.example.ecom.feature.admin.R.id.adminUserDetailFragment,
                        id(AdminRouter.ARG_ADMIN_USER_ID, customer.get())));
                destination(com.example.ecom.feature.admin.R.id.adminUserDetailFragment, false);
                back();
                destination(com.example.ecom.feature.admin.R.id.adminUsersFragment, true);
            }
            runOnMainSync(() -> AdminRouter.toAdminSettings(nav));
            destination(com.example.ecom.feature.admin.R.id.adminSettingsFragment, false);
            back();
            destination(com.example.ecom.feature.admin.R.id.adminUsersFragment, true);
            tab(com.example.ecom.feature.admin.R.id.adminDashboardFragment);

            com.example.ecom.core.data.TokenManager tokens = new com.example.ecom.core.data.TokenManager(getTargetContext());
            check(java.util.Objects.equals(access, tokens.getAccessToken()), "Admin must preserve customer access token");
            check(java.util.Objects.equals(refresh, tokens.getRefreshToken()), "Admin must preserve customer refresh token");

            SystemClock.sleep(250);
            Bitmap screenshot = getUiAutomation().takeScreenshot();
            if (screenshot != null) {
                try (FileOutputStream output = new FileOutputStream(new File(
                        getTargetContext().getExternalFilesDir(null), "admin-integration.png"))) {
                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, output);
                }
                screenshot.recycle();
            }
            result.putString("stream", "PASS: admin five tabs, product validation/detail/edit/back, order/customer details, settings, footer, customer token preservation.\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            result.putString("stream", "FAIL: " + android.util.Log.getStackTraceString(failure));
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private Bundle id(String key, int value) {
        Bundle args = new Bundle();
        args.putInt(key, value);
        return args;
    }
}
