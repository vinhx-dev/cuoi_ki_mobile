package com.example.ecom.core.navigation;


import java.util.HashMap;
import java.util.Map;

/**
 * Khai báo layout khung cho từng trang. Thêm trang mới:
 *   1. Tạo Fragment + layout trong module feature/<tên> (module mới thì include trong
 *      settings.gradle.kts và thêm vào dependencies của :app)
 *   2. Thêm id trang vào core/navigation/src/main/res/values/ids.xml
 *   3. Khai báo <fragment> trong app/src/main/res/navigation/nav_graph.xml
 *   4. Đăng ký LayoutType ở đây
 *   5. Thêm hàm điều hướng trong AppRouter
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

        LAYOUTS.put(R.id.adminDashboardFragment, LayoutType.ADMIN);
        LAYOUTS.put(R.id.adminProductsFragment, LayoutType.ADMIN);
        LAYOUTS.put(R.id.adminOrdersFragment, LayoutType.ADMIN);
        LAYOUTS.put(R.id.adminRevenueFragment, LayoutType.ADMIN);
        LAYOUTS.put(R.id.adminUsersFragment, LayoutType.ADMIN);
        LAYOUTS.put(R.id.adminProductDetailFragment, LayoutType.ADMIN_DETAIL);
        LAYOUTS.put(R.id.adminProductFormFragment, LayoutType.ADMIN_DETAIL);
        LAYOUTS.put(R.id.adminOrderDetailFragment, LayoutType.ADMIN_DETAIL);
        LAYOUTS.put(R.id.adminUserDetailFragment, LayoutType.ADMIN_DETAIL);
        LAYOUTS.put(R.id.adminSettingsFragment, LayoutType.ADMIN_DETAIL);
    }

    public static LayoutType layoutOf(int destinationId) {
        LayoutType type = LAYOUTS.get(destinationId);
        return type != null ? type : LayoutType.DETAIL;
    }
}
