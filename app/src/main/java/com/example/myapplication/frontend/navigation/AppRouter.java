package com.example.myapplication.frontend.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;

/**
 * ===== ROUTER DÙNG CHUNG =====
 * Mọi điều hướng trong app đi qua đây, Fragment không gọi NavController trực tiếp.
 * Đổi luồng / animation / tham số chỉ cần sửa 1 chỗ.
 */
public final class AppRouter {

    private AppRouter() {}

    // Key tham số truyền giữa các trang (khớp với <argument> trong nav_graph.xml)
    public static final String ARG_PRODUCT_ID = "productId";
    public static final String ARG_ORDER_ID = "orderId";

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
