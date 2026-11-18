package com.example.ecom.core.api;

import com.example.ecom.core.api.BuildConfig;

/**
 * Cấu hình gọi API tới backend HTTP server.
 *  - BASE_URL: Lấy từ BuildConfig (mặc định http://10.0.2.2:8080/api cho emulator,
 *    hoặc ghi đè bằng thuộc tính 'api.baseUrl' trong local.properties khi chạy máy thật).
 *  - TIMEOUT_MS: Thời gian timeout kết nối (15 giây).
 */
public final class ApiConfig {

    private ApiConfig() {}

    public static final String BASE_URL = BuildConfig.BASE_URL;

    public static final int TIMEOUT_MS = 15_000;
}
