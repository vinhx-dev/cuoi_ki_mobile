package com.example.ecom.core.api;

import java.util.Map;

/**
 * Lớp "vận chuyển" request. Chạy trên background thread.
 * Cài đặt mặc định: HttpTransport (HttpURLConnection). Có thể thay bằng OkHttp mà không ảnh hưởng tầng trên.
 */
public interface ApiTransport {
    /**
     * @param body  JsonBody, MultipartBody hoặc null (không có body)
     * @param token token đăng nhập hoặc null
     */
    RawResponse execute(String method, String path, Map<String, String> query,
                        RequestBody body, String token) throws Exception;
}
