package com.example.myapplication.backend.core;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request gửi vào FakeServer — mô phỏng 1 HTTP request.
 */
public class ApiRequest {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> query;
    private final JSONObject body;
    private final String token;

    // Router sẽ điền các tham số dạng /products/{id}
    private final Map<String, String> pathParams = new HashMap<>();

    public ApiRequest(HttpMethod method, String path, Map<String, String> query,
                      JSONObject body, String token) {
        this.method = method;
        this.path = path;
        this.query = query != null ? query : new HashMap<>();
        this.body = body != null ? body : new JSONObject();
        this.token = token;
    }

    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public JSONObject getBody() { return body; }
    public String getToken() { return token; }

    public String getQuery(String key) { return query.get(key); }

    public String getPathParam(String key) { return pathParams.get(key); }

    public int getPathParamInt(String key) {
        try {
            return Integer.parseInt(pathParams.get(key));
        } catch (Exception e) {
            throw new ApiException(400, "Tham số '" + key + "' không hợp lệ");
        }
    }

    void putPathParams(Map<String, String> params) { pathParams.putAll(params); }
}
