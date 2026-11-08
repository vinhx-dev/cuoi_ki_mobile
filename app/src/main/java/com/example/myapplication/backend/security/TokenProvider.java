package com.example.myapplication.backend.security;

import com.example.myapplication.backend.core.ApiException;

/**
 * Token giả lập (KHÔNG phải JWT thật): "fake-token.<userId>".
 * Đủ để demo luồng: login -> lưu token -> gửi kèm request -> backend xác thực.
 */
public final class TokenProvider {

    private static final String PREFIX = "fake-token.";

    private TokenProvider() {}

    public static String generate(int userId) {
        return PREFIX + userId;
    }

    /** Trả về userId nếu token hợp lệ, ngược lại ném 401. */
    public static int verify(String token) {
        if (token == null || !token.startsWith(PREFIX)) {
            throw ApiException.unauthorized("Bạn chưa đăng nhập");
        }
        try {
            return Integer.parseInt(token.substring(PREFIX.length()));
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Token không hợp lệ");
        }
    }
}
