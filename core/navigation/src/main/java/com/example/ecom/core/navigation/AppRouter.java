package com.example.ecom.core.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;


/**
 * ===== ROUTER DÙNG CHUNG =====
 * Mọi điều hướng trong app đi qua đây, Fragment không gọi NavController trực tiếp.
 * Đổi luồng / animation / tham số chỉ cần sửa 1 chỗ.
 */
public final class AppRouter {

    private AppRouter() {}

    // Key tham số truyền giữa các trang (khớp với <argument> trong app/src/main/res/navigation/nav_graph.xml)
    public static final String ARG_PRODUCT_ID = "productId";
    public static final String ARG_ORDER_ID = "orderId";
    public static final String ARG_ADMIN_PRODUCT_ID = "adminProductId";
    public static final String ARG_ADMIN_ORDER_ID = "adminOrderId";
    public static final String ARG_ADMIN_USER_ID = "adminUserId";

    // ---------- Auth ----------

    public static void toLogin(Fragment from) {
        toLogin(nav(from));
    }

    public static void toLogin(NavController nav) {
        navigate(nav, R.id.loginFragment, null, true);
    }

    public static void toRegister(Fragment from) {
        navigate(nav(from), R.id.registerFragment, null, false);
    }

    // ---------- Main ----------

    /** Vào trang chủ và xoá toàn bộ back stack (sau khi đăng nhập). */
    public static void toHome(Fragment from) {
        navigate(nav(from), R.id.homeFragment, null, true);
    }

    public static void toProductDetail(Fragment from, int productId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        navigate(nav(from), R.id.productDetailFragment, args, false);
    }

    public static void toOrderDetail(Fragment from, int orderId) {
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        navigate(nav(from), R.id.orderDetailFragment, args, false);
    }

    // ---------- Admin ----------

    public static void toAdminDashboard(Fragment from) {
        navigate(nav(from), R.id.adminDashboardFragment, null, false);
    }

    public static void toAdminDashboard(NavController nav) {
        navigate(nav, R.id.adminDashboardFragment, null, false);
    }

    public static void toAdminProducts(Fragment from) {
        toAdminTab(nav(from), R.id.adminProductsFragment);
    }

    public static void toAdminOrders(Fragment from) {
        toAdminTab(nav(from), R.id.adminOrdersFragment);
    }

    public static void toAdminUsers(Fragment from) {
        toAdminTab(nav(from), R.id.adminUsersFragment);
    }

    public static void toAdminRevenue(Fragment from) {
        toAdminTab(nav(from), R.id.adminRevenueFragment);
    }

    public static void toAdminSettings(Fragment from) { toAdminSettings(nav(from)); }

    public static void toAdminSettings(NavController nav) {
        navigate(nav, R.id.adminSettingsFragment, null, false);
    }

    public static void toAdminProductDetail(Fragment from, int id) {
        adminDetail(from, R.id.adminProductDetailFragment, ARG_ADMIN_PRODUCT_ID, id);
    }

    public static void toAdminProductForm(Fragment from, int id) {
        adminDetail(from, R.id.adminProductFormFragment, ARG_ADMIN_PRODUCT_ID, id);
    }

    public static void toAdminOrderDetail(Fragment from, int id) {
        adminDetail(from, R.id.adminOrderDetailFragment, ARG_ADMIN_ORDER_ID, id);
    }

    public static void toAdminUserDetail(Fragment from, int id) {
        adminDetail(from, R.id.adminUserDetailFragment, ARG_ADMIN_USER_ID, id);
    }

    private static void adminDetail(Fragment from, int destination, String key, int id) {
        Bundle args = new Bundle();
        args.putInt(key, id);
        navigate(nav(from), destination, args, false);
    }

    /** Các tab admin quay về cùng dashboard, tránh tích lũy back stack khi đổi tab. */
    public static void toAdminTab(NavController nav, int destination) {
        if (destination != R.id.adminDashboardFragment && destination != R.id.adminProductsFragment
                && destination != R.id.adminOrdersFragment && destination != R.id.adminRevenueFragment
                && destination != R.id.adminUsersFragment) return;
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == destination) return;
        NavOptions options = new NavOptions.Builder().setLaunchSingleTop(true)
                .setPopUpTo(R.id.adminDashboardFragment, false).build();
        nav.navigate(destination, null, options);
    }

    public static void back(Fragment from) {
        nav(from).navigateUp();
    }

    // ---------- Core ----------

    private static NavController nav(Fragment fragment) {
        return NavHostFragment.findNavController(fragment);
    }

    private static void navigate(NavController nav, int destId, Bundle args, boolean clearBackStack) {
        // Tránh crash khi bấm 2 lần liên tiếp vào cùng 1 trang
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == destId && args == null) {
            return;
        }
        NavOptions.Builder options = new NavOptions.Builder();
        if (clearBackStack) {
            // Đổi hẳn "khu vực" (auth <-> main): xoá back stack, không animation để layout khung không bị chớp
            options.setPopUpTo(nav.getGraph().getId(), true);
        } else {
            options.setEnterAnim(android.R.anim.fade_in)
                    .setExitAnim(android.R.anim.fade_out)
                    .setPopEnterAnim(android.R.anim.fade_in)
                    .setPopExitAnim(android.R.anim.fade_out);
        }
        nav.navigate(destId, args, options.build());
    }
}
