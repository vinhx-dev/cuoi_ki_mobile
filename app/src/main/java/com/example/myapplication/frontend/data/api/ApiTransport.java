package com.example.myapplication.frontend.data.api;

import org.json.JSONObject;

import java.util.Map;

/**
 * Lớp "vận chuyển" request. Chạy trên background thread.
 * Có 2 cài đặt: FakeBackendTransport (demo) và HttpTransport (server thật).
 */
public interface ApiTransport {
    RawResponse execute(String method, String path, Map<String, String> query,
                        JSONObject body, String token) throws Exception;
}
