package com.example.ecom.core.navigation;

import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import java.util.function.BooleanSupplier;

/**
 * ===== ĐIỀU HƯỚNG DÙNG CHUNG (≈ const navigate = useNavigate() trong React) =====
 *
 *   React:   navigate(`/product/${id}`)
 *   Android: AppRouter.toProductDetail(this, id)    →  mở đường dẫn "product/5"
 *
 * Mọi lần chuyển trang đều được kiểm tra: trang LOGIN_REQUIRED mà chưa đăng nhập
 * thì tự chuyển sang trang Login (≈ <ProtectedRoute>).
 * Fragment không gọi NavController trực tiếp. Đổi luồng / animation chỉ sửa ở đây.
 */
public final class AppRouter {

    private AppRouter() {}

    /** Đường dẫn gốc chứa mọi trang. */
    static final String ROOT = "root";

    private static RouteTable table;
    private static BooleanSupplier isLoggedIn = () -> false;

    /**
     * Gọi 1 lần trong MainActivity: nạp các trang từ AppPages vào NavController.
     * @param isLoggedIn cách kiểm tra đã đăng nhập chưa (app tự cung cấp, vd. đọc token)
     */
    public static void install(NavController nav, BooleanSupplier isLoggedIn) {
        RouteTable t = new RouteTable();
        AppPages.declare(t);
        t.install(nav);
        table = t;
        AppRouter.isLoggedIn = isLoggedIn;
    }

    // ===== CHUYỂN TRANG =====

    public static void toLogin(Fragment from) {
        toLogin(nav(from));
    }

    public static void toLogin(NavController nav) {
        navigate(nav, table.loginRoute(), replaceAll());
    }

    public static void toOtp(Fragment from, String email, boolean sent) {
        navigate(nav(from), "otp?email=" + Uri.encode(email) + "&sent=" + sent, fade());
    }

    public static void toRegister(Fragment from) {
        navigate(nav(from), "register", fade());
    }

    public static void toVerifyOtp(Fragment from, String email) {
        navigate(nav(from), "verify_otp?email=" + Uri.encode(email), fade());
    }

    /** Vào trang chủ và xoá toàn bộ lịch sử (sau khi đăng nhập). ≈ navigate("/", { replace: true }) */
    public static void toHome(Fragment from) {
        navigate(nav(from), "home", replaceAll());
    }

    /** Role comes from login/me response; core navigation has no feature dependency. */
    public static void afterLogin(Fragment from, boolean admin) {
        if (!admin) { toHome(from); return; }
        android.content.Intent intent = new android.content.Intent().setClassName(from.requireContext(),
                "com.example.ecom.feature.admin.activities.AdminActivity");
        from.startActivity(intent);
        from.requireActivity().finish();
    }

    /** Mở trang chủ kèm từ khoá tìm kiếm. ≈ navigate(`/?query=${query}`) */
    public static void toHomeSearch(Fragment from, String query) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(AppPages.HOME, true) // thay trang chủ cũ (nếu có) bằng trang chủ mới
                .build();
        navigate(nav(from), "home?query=" + Uri.encode(query), options);
    }

    public static void toProductList(Fragment from, String category) {
        navigate(nav(from), "products?category=" + Uri.encode(category), fade());
    }

    public static void toProductDetail(Fragment from, int productId) {
        navigate(nav(from), "product/" + productId, fade());
    }

    public static void toOrderDetail(Fragment from, int orderId) {
        navigate(nav(from), "order/" + orderId, fade());
    }

    public static void toCart(Fragment from) {
        navigate(nav(from), "cart", fade());
    }

    /** Chuyển tab ở bottom bar: luôn giữ trang chủ ở dưới cùng, không chồng nhiều tab lên nhau. */
    public static void toTab(Fragment from, String path) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(AppPages.HOME, false)
                .setLaunchSingleTop(true)
                .build();
        navigate(nav(from), path, options);
    }

    /** ≈ navigate(-1) */
    public static void back(Fragment from) {
        nav(from).navigateUp();
    }

    // ===== Nội bộ =====

    private static NavController nav(Fragment fragment) {
        return NavHostFragment.findNavController(fragment);
    }

    private static void navigate(NavController nav, String path, NavOptions options) {
        RouteTable.Page page = table.match(path);
        if (page == null) {
            throw new IllegalArgumentException("Chưa khai báo trang cho đường dẫn '" + path + "' trong AppPages");
        }
        // Trang cần đăng nhập mà chưa đăng nhập -> sang trang Login
        if (page.access == Access.LOGIN_REQUIRED && !isLoggedIn.getAsBoolean()) {
            path = table.loginRoute();
            options = replaceAll();
        }
        // Đang ở đúng trang này rồi (vd. bấm 2 lần liên tiếp) thì bỏ qua
        NavDestination current = nav.getCurrentDestination();
        if (current != null && current.hasDeepLink(Uri.parse("android-app://androidx.navigation/" + path))) {
            return;
        }
        nav.navigate(path, options);
    }

    /** Đổi hẳn khu vực (auth <-> main): xoá lịch sử, không animation để không bị chớp. */
    private static NavOptions replaceAll() {
        return new NavOptions.Builder().setPopUpTo(ROOT, true).build();
    }

    private static NavOptions fade() {
        return new NavOptions.Builder()
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(android.R.anim.fade_out)
                .setPopEnterAnim(android.R.anim.fade_in)
                .setPopExitAnim(android.R.anim.fade_out)
                .build();
    }
}
