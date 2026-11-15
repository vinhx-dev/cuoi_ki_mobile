package com.example.ecom.core.ui.base;

/**
 * Những gì "khung app" (Activity chứa các trang) cung cấp cho các Fragment.
 * MainActivity (module :app) implement interface này, nhờ vậy module feature
 * không cần phụ thuộc ngược vào :app.
 */
public interface AppShell {

    /** Bật/tắt loading dùng chung (hỗ trợ nhiều request song song). */
    void setLoading(boolean loading);

    /** Đổi tiêu đề header (vd: tên sản phẩm). */
    void setHeaderTitle(CharSequence title);
}
