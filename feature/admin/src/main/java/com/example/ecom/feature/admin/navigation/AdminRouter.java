package com.example.ecom.feature.admin.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecom.feature.admin.R;

/** Navigation confined to the admin host; customer routes stay owned by Minh's AppRouter. */
public final class AdminRouter {
    public static final String ARG_ADMIN_PRODUCT_ID = "adminProductId";
    public static final String ARG_ADMIN_ORDER_ID = "adminOrderId";
    public static final String ARG_ADMIN_USER_ID = "adminUserId";

    private AdminRouter() {}

    public static void toAdminDashboard(Fragment from) { toAdminTab(nav(from), R.id.adminDashboardFragment); }
    public static void toAdminProducts(Fragment from) { toAdminTab(nav(from), R.id.adminProductsFragment); }
    public static void toAdminOrders(Fragment from) { toAdminTab(nav(from), R.id.adminOrdersFragment); }
    public static void toAdminUsers(Fragment from) { toAdminTab(nav(from), R.id.adminUsersFragment); }
    public static void toAdminRevenue(Fragment from) { toAdminTab(nav(from), R.id.adminRevenueFragment); }
    public static void toAdminSettings(Fragment from) { toAdminSettings(nav(from)); }
    public static void toAdminSettings(NavController nav) { nav.navigate(R.id.adminSettingsFragment); }

    public static void toAdminProductDetail(Fragment from, int id) {
        detail(from, R.id.adminProductDetailFragment, ARG_ADMIN_PRODUCT_ID, id);
    }

    public static void toAdminProductForm(Fragment from, int id) {
        detail(from, R.id.adminProductFormFragment, ARG_ADMIN_PRODUCT_ID, id);
    }

    public static void toAdminOrderDetail(Fragment from, int id) {
        detail(from, R.id.adminOrderDetailFragment, ARG_ADMIN_ORDER_ID, id);
    }

    public static void toAdminUserDetail(Fragment from, int id) {
        detail(from, R.id.adminUserDetailFragment, ARG_ADMIN_USER_ID, id);
    }

    public static boolean isTab(int destination) {
        return destination == R.id.adminDashboardFragment || destination == R.id.adminProductsFragment
                || destination == R.id.adminOrdersFragment || destination == R.id.adminRevenueFragment
                || destination == R.id.adminUsersFragment;
    }

    public static void toAdminTab(NavController nav, int destination) {
        if (!isTab(destination)) return;
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == destination) return;
        NavOptions options = new NavOptions.Builder().setLaunchSingleTop(true)
                .setPopUpTo(R.id.adminDashboardFragment, false).build();
        nav.navigate(destination, null, options);
    }

    public static void back(Fragment from) { nav(from).navigateUp(); }

    private static void detail(Fragment from, int destination, String key, int id) {
        Bundle args = new Bundle();
        args.putInt(key, id);
        nav(from).navigate(destination, args);
    }

    private static NavController nav(Fragment from) { return NavHostFragment.findNavController(from); }
}
