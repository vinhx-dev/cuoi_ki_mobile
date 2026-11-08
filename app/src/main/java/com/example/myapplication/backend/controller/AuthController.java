package com.example.myapplication.backend.controller;

import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.entity.User;
import com.example.myapplication.backend.mapper.JsonMapper;
import com.example.myapplication.backend.service.AuthService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tầng Controller: đọc request -> gọi Service -> map kết quả ra JSON response.
 * Không chứa logic nghiệp vụ.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/login  body: { phone, password }
    public ApiResponse login(ApiRequest req) throws JSONException {
        JSONObject body = req.getBody();
        User user = authService.login(body.optString("phone"), body.optString("password"));
        return ApiResponse.ok("Đăng nhập thành công", authPayload(user));
    }

    // POST /auth/register  body: { name, phone, password }
    public ApiResponse register(ApiRequest req) throws JSONException {
        JSONObject body = req.getBody();
        User user = authService.register(body.optString("name"), body.optString("phone"), body.optString("password"));
        return ApiResponse.created(authPayload(user));
    }

    private JSONObject authPayload(User user) throws JSONException {
        return new JSONObject()
                .put("token", authService.issueToken(user))
                .put("user", JsonMapper.toJson(user));
    }
}
