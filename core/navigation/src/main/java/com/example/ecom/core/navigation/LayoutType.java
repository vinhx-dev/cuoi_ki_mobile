package com.example.ecom.core.navigation;

/**
 * Tên các layout có thể chọn khi khai báo trang trong AppPages.
 * Giao diện của từng layout nằm ở core/ui/src/main/layouts/<tên>/.
 */
public enum LayoutType {
    /** Không header, không bottom bar (Splash, Login...). */
    NONE,
    /** Header tìm kiếm + bottom bar (các tab chính). */
    MAIN,
    /** Header có nút back + tìm kiếm (trang chi tiết). */
    DETAIL,
    /** Header quản trị: nút back + tiêu đề (trang admin). */
    ADMIN
}
