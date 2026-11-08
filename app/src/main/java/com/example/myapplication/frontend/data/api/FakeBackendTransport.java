package com.example.myapplication.frontend.data.api;

import com.example.myapplication.backend.FakeServer;
import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.core.HttpMethod;

import org.json.JSONObject;

import java.util.Map;

/**
 * Cầu nối DUY NHẤT giữa frontend và demo backend.
 * Hoạt động như 1 "API ảo": nhận request, giả lập độ trễ mạng, đưa vào FakeServer.
 */
public class FakeBackendTransport implements ApiTransport {

    @Override
    public RawResponse execute(String method, String path, Map<String, String> query,
                               JSONObject body, String token) throws Exception {
        Thread.sleep(ApiConfig.FAKE_DELAY_MS);

        // Copy body để backend không giữ tham chiếu tới object của frontend
        JSONObject bodyCopy = body != null ? new JSONObject(body.toString()) : null;

        ApiRequest request = new ApiRequest(HttpMethod.valueOf(method), path, query, bodyCopy, token);
        ApiResponse response = FakeServer.getInstance().handle(request);
        return new RawResponse(response.getStatusCode(), response.getBody());
    }
}
