package com.example.myapplication.backend.core;

/**
 * Lỗi nghiệp vụ — ném ra ở bất kỳ tầng nào, FakeServer sẽ bắt và trả về đúng status code.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }

    public static ApiException badRequest(String msg) { return new ApiException(400, msg); }
    public static ApiException unauthorized(String msg) { return new ApiException(401, msg); }
    public static ApiException forbidden(String msg) { return new ApiException(403, msg); }
    public static ApiException notFound(String msg) { return new ApiException(404, msg); }
}
