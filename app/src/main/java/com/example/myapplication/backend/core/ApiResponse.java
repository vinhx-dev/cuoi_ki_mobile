package com.example.myapplication.backend.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Response trả về từ FakeServer.
 * Body luôn có dạng: { "success": bool, "message": string, "data": any }
 */
public class ApiResponse {

    private final int statusCode;
    private final String body;

    private ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }

    public static ApiResponse ok(Object data) {
        return of(200, true, "OK", data);
    }

    public static ApiResponse ok(String message, Object data) {
        return of(200, true, message, data);
    }

    public static ApiResponse created(Object data) {
        return of(201, true, "Created", data);
    }

    public static ApiResponse error(int statusCode, String message) {
        return of(statusCode, false, message, null);
    }

    private static ApiResponse of(int status, boolean success, String message, Object data) {
        try {
            JSONObject json = new JSONObject();
            json.put("success", success);
            json.put("message", message);
            json.put("data", data != null ? data : JSONObject.NULL);
            return new ApiResponse(status, json.toString());
        } catch (JSONException e) {
            return new ApiResponse(500, "{\"success\":false,\"message\":\"Serialize error\",\"data\":null}");
        }
    }
}
