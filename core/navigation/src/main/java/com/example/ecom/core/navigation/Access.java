package com.example.ecom.core.navigation;

/** Ai được vào trang. Chọn khi khai báo trang trong AppPages. */
public enum Access {
    /** Ai cũng vào được. */
    PUBLIC,
    /** Phải đăng nhập; chưa đăng nhập thì tự chuyển sang trang Login. ≈ <ProtectedRoute> */
    LOGIN_REQUIRED
}
