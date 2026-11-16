package com.example.ecom.core.ui.layout;

import android.os.Bundle;

import com.example.ecom.core.navigation.LayoutType;
import com.example.ecom.core.navigation.RouteTable;
import com.example.ecom.core.ui.R;

/**
 * ===== CÁC LAYOUT DÙNG CHUNG (≈ thư mục layouts/ trong React) =====
 *
 * Mỗi layout là 1 thư mục trong core/ui/src/main/layouts/:
 *
 *   layouts/none/    layout_none.xml   : chỉ nội dung trang                   (Splash, Login, Register)
 *   layouts/main/    layout_main.xml   : header tìm kiếm + trang + bottom bar   (Home, Orders, Profile)
 *   layouts/detail/  layout_detail.xml : header có nút back + tìm kiếm + trang  (ProductDetail, OrderDetail)
 *   layouts/admin/   layout_admin.xml  : header quản trị (back + tiêu đề) + trang
 *
 * Phần dùng chung cho mọi layout (chỗ đặt trang + loading, thanh tìm kiếm) ở core/ui/src/main/res/.
 * Trang dùng layout nào: chọn ở AppPages.java (core:navigation).
 *
 * THÊM LAYOUT MỚI:
 *   1. Tạo thư mục layouts/<tên>/res/layout/layout_<tên>.xml (bắt buộc include part_content)
 *   2. Thêm tên vào LayoutType (core:navigation)
 *   3. Thêm 1 dòng vào hàm xmlOf() bên dưới
 */
final class AppLayout {

    private AppLayout() {}

    /** File XML của từng layout. */
    static int xmlOf(LayoutType type) {
        switch (type) {
            case MAIN:   return R.layout.layout_main;
            case DETAIL: return R.layout.layout_detail;
            case ADMIN:  return R.layout.layout_admin;
            case NONE:
            default:     return R.layout.layout_none;
        }
    }

    /** Đọc layout mà AppPages đã chọn cho trang (được gắn sẵn trong arguments của Fragment). */
    static LayoutType of(Bundle pageArgs) {
        String name = pageArgs != null ? pageArgs.getString(RouteTable.ARG_LAYOUT) : null;
        return name != null ? LayoutType.valueOf(name) : LayoutType.NONE;
    }
}
