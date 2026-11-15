package com.example.ecom.core.data.api;

import org.json.JSONObject;

import java.util.Map;

/**
 * Lớp "vận chuyển" request. Chạy trên background thread.
 * Cài đặt mặc định: HttpTransport (kết nối HTTP tới backend server).
 */
public interface ApiTransport {
    RawResponse execute(String method, String path, Map<String, String> query,
                        JSONObject body, String token) throws Exception;
}
