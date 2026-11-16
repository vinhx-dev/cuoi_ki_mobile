package com.example.ecom.core.api;

import java.io.IOException;
import java.io.OutputStream;

/** Phần thân (body) của request: JSON (JsonBody) hoặc form có file/ảnh (MultipartBody). */
public interface RequestBody {

    /** Giá trị header Content-Type. */
    String contentType();

    /** Ghi nội dung body vào kết nối. Chạy trên background thread. */
    void writeTo(OutputStream out) throws IOException;
}
