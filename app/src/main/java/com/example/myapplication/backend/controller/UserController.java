package com.example.myapplication.backend.controller;

import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.entity.User;
import com.example.myapplication.backend.mapper.JsonMapper;
import com.example.myapplication.backend.service.AuthService;

import org.json.JSONException;

public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    // GET /users/me  (cần token)
    public ApiResponse me(ApiRequest req) throws JSONException {
        User user = authService.authenticate(req.getToken());
        return ApiResponse.ok(JsonMapper.toJson(user));
    }
}
