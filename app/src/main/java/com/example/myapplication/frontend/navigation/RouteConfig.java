package com.example.myapplication.frontend.navigation;

import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Khai báo layout cho từng trang. Thêm trang mới:
 *   1. Tạo Fragment + layout xml
 *   2. Khai báo <fragment> trong res/navigation/nav_graph.xml
 *   3. Đăng ký LayoutType ở đây
 *   4. (tuỳ chọn) thêm hàm điều hướng trong AppRouter
 */
public final class RouteConfig {

    private RouteConfig() {}

    private static final Map<Integer, LayoutType> LAYOUTS = new HashMap<>();

    static {
        LAYOUTS.put(R.id.splashFragment, LayoutType.BLANK);

        LAYOUTS.put(R.id.loginFragment, LayoutType.AUTH);
        LAYOUTS.put(R.id.registerFragment, LayoutType.AUTH);

        LAYOUTS.put(R.id.homeFragment, LayoutType.MAIN);
        LAYOUTS.put(R.id.ordersFragment, LayoutType.MAIN);
        LAYOUTS.put(R.id.profileFragment, LayoutType.MAIN);

        LAYOUTS.put(R.id.productDetailFragment, LayoutType.DETAIL);
        LAYOUTS.put(R.id.orderDetailFragment, LayoutType.DETAIL);
    }

    public static LayoutType layoutOf(int destinationId) {
        LayoutType type = LAYOUTS.get(destinationId);
        return type != null ? type : LayoutType.DETAIL;
    }
}
