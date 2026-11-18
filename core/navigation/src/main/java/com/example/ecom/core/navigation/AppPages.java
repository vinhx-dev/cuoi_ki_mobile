package com.example.ecom.core.navigation;

import static com.example.ecom.core.navigation.Access.LOGIN_REQUIRED;
import static com.example.ecom.core.navigation.Access.PUBLIC;

/**
 * ===== KHAI BÁO TẤT CẢ CÁC TRANG CỦA APP (≈ <Routes> trong React Router) =====
 *
 *   React:
 *     <Route path="/product/:id"
 *            element={<ProtectedRoute><DetailLayout><ProductDetail/></DetailLayout></ProtectedRoute>} />
 *   Android:
 *     r.page(PRODUCT_DETAIL, "...ProductDetailFragment", LayoutType.DETAIL, LOGIN_REQUIRED).intArg(...)
 *
 * THÊM TRANG MỚI:
 *   1. Tạo Fragment trong module feature/<tên>:  public CartFragment() { super(R.layout.fragment_cart); }
 *   2. Thêm hằng số đường dẫn bên dưới (vd. CART = "cart")
 *   3. Thêm 1 dòng r.page(...) trong declare(): chọn LAYOUT và có CẦN ĐĂNG NHẬP không
 *   4. Thêm hàm toCart(...) trong AppRouter
 *
 * Trang được khai báo bằng TÊN CLASS (chuỗi) vì module core không được phụ thuộc vào module feature.
 * Gõ sai tên class thì app báo lỗi ngay khi mở, chỉ rõ dòng sai.
 */
public final class AppPages {

    private AppPages() {}

    // ===== ĐƯỜNG DẪN CÁC TRANG (≈ path="/product/:id") =====
    // {xxx} là tham số của đường dẫn, giống :xxx trong React.

    public static final String SPLASH = "splash";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String OTP = "otp?email={email}&sent={sent}";
    public static final String HOME = "home?query={query}";
    public static final String PRODUCT_LIST = "products?category={category}";
    public static final String ORDERS = "orders";
    public static final String PROFILE = "profile";
    public static final String PRODUCT_DETAIL = "product/{productId}";
    public static final String ORDER_DETAIL = "order/{orderId}";
    public static final String CART = "cart";
    public static final String VERIFY_OTP = "verify_otp?email={email}";

    // Tên tham số (đọc bằng getArguments() trong Fragment)
    public static final String ARG_QUERY = "query";
    public static final String ARG_PRODUCT_ID = "productId";
    public static final String ARG_ORDER_ID = "orderId";
    public static final String ARG_EMAIL = "email";

    private static final String FEATURE = "com.example.ecom.feature.";

    static void declare(RouteTable r) {
        //     đường dẫn        trang (class Fragment)                          layout             cần đăng nhập?

        // ----- Chưa đăng nhập -----
        r.page(SPLASH,          FEATURE + "splash.SplashFragment",              LayoutType.NONE,   PUBLIC);
        r.page(LOGIN,           FEATURE + "auth.login.LoginFragment",           LayoutType.NONE,   PUBLIC);
        r.page(OTP, FEATURE + "auth.otp.OtpFragment", LayoutType.NONE, PUBLIC).stringArg("email", "").stringArg("sent", "false");
        r.page(REGISTER,        FEATURE + "auth.register.RegisterFragment",     LayoutType.NONE,   PUBLIC);
        r.page(VERIFY_OTP,      FEATURE + "auth.otp.OtpVerifyFragment",         LayoutType.NONE,   PUBLIC)
                .stringArg(ARG_EMAIL, "");

        // ----- Các tab chính -----
        r.page(HOME,            FEATURE + "home.HomeFragment",                  LayoutType.MAIN,   LOGIN_REQUIRED)
                .stringArg(ARG_QUERY, "");
        r.page(PRODUCT_LIST, FEATURE + "home.HomeFragment", LayoutType.MAIN, LOGIN_REQUIRED).stringArg("category", "");
        r.page(ORDERS,          FEATURE + "order.list.OrdersFragment",          LayoutType.NONE,   LOGIN_REQUIRED);
        r.page(PROFILE,         FEATURE + "profile.ProfileFragment",            LayoutType.NONE,   LOGIN_REQUIRED);

        // ----- Trang chi tiết -----
        r.page(PRODUCT_DETAIL,  FEATURE + "product.ProductDetailFragment",      LayoutType.DETAIL, LOGIN_REQUIRED)
                .intArg(ARG_PRODUCT_ID);
        r.page(ORDER_DETAIL,    FEATURE + "order.detail.OrderDetailFragment",   LayoutType.DETAIL, LOGIN_REQUIRED)
                .intArg(ARG_ORDER_ID);
        r.page(CART,            FEATURE + "order.cart.CartFragment",            LayoutType.NONE,   LOGIN_REQUIRED);

        // Trang mở đầu tiên
        r.start(SPLASH);
        // Trang chuyển tới khi vào trang LOGIN_REQUIRED mà chưa đăng nhập
        r.loginPage(LOGIN);
    }
}
