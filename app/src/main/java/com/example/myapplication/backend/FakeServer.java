package com.example.myapplication.backend;

import android.util.Log;

import com.example.myapplication.backend.config.RouteConfig;
import com.example.myapplication.backend.core.ApiException;
import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.core.Router;

/**
 * ===== ĐIỂM VÀO DUY NHẤT CỦA DEMO BACKEND =====
 *
 * Luồng xử lý:  FakeServer -> Router -> Controller -> Service -> Repository -> FakeDatabase
 *
 * Frontend KHÔNG được import bất cứ class nào trong package backend ngoài FakeServer
 * (thông qua FakeBackendTransport). Dữ liệu qua lại chỉ là JSON string, giống HTTP thật.
 */
public final class FakeServer {

    private static final String TAG = "FakeServer";
    private static volatile FakeServer instance;

    private final Router router;

    private FakeServer() {
        router = RouteConfig.createRouter();
    }

    public static FakeServer getInstance() {
        if (instance == null) {
            synchronized (FakeServer.class) {
                if (instance == null) instance = new FakeServer();
            }
        }
        return instance;
    }

    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        try {
            response = router.dispatch(request);
        } catch (ApiException e) {
            response = ApiResponse.error(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Internal error", e);
            response = ApiResponse.error(500, "Lỗi server: " + e.getMessage());
        }
        Log.d(TAG, request.getMethod() + " " + request.getPath() + " -> " + response.getStatusCode());
        return response;
    }
}
