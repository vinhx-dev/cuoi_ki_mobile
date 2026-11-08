package com.example.myapplication.frontend.data.api;

/** Kết quả thô từ transport: status code + body JSON string. */
public class RawResponse {
    public final int statusCode;
    public final String body;

    public RawResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}
