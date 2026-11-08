package com.example.myapplication.frontend.data.api;

/**
 * Cấu hình gọi API.
 *  - USE_FAKE_BACKEND = true  -> gọi demo backend chạy ngay trong app (package backend)
 *  - USE_FAKE_BACKEND = false -> gọi server thật qua HTTP tại BASE_URL
 * Toàn bộ code UI/Repository không cần sửa khi đổi cờ này.
 */
public final class ApiConfig {

    private ApiConfig() {}

    public static final boolean USE_FAKE_BACKEND = true;

    // 10.0.2.2 = localhost của máy tính khi chạy trên emulator
    public static final String BASE_URL = "http://10.0.2.2:8080/api";

    public static final int TIMEOUT_MS = 15_000;

    // Độ trễ giả lập mạng cho fake backend, để thấy loading
    public static final long FAKE_DELAY_MS = 600;
}
