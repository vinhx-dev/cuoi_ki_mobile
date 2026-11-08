package com.example.myapplication.frontend.navigation;

/**
 * Các kiểu layout khung (shell) của app. MainActivity sẽ bật/tắt header, nút back, bottom nav theo kiểu này.
 *
 *  BLANK  : không header, không bottom nav       (Splash)
 *  AUTH   : không header, không bottom nav       (Đăng nhập, Đăng ký)
 *  MAIN   : header + bottom nav, không nút back  (Trang chủ, Đơn hàng, Tài khoản)
 *  DETAIL : header có nút back, không bottom nav (Chi tiết SP, Chi tiết đơn...)
 */
public enum LayoutType {
    BLANK(false, false, false),
    AUTH(false, false, false),
    MAIN(true, false, true),
    DETAIL(true, true, false);

    public final boolean showHeader;
    public final boolean showBack;
    public final boolean showBottomNav;

    LayoutType(boolean showHeader, boolean showBack, boolean showBottomNav) {
        this.showHeader = showHeader;
        this.showBack = showBack;
        this.showBottomNav = showBottomNav;
    }
}
