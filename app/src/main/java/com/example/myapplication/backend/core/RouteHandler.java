package com.example.myapplication.backend.core;

import org.json.JSONException;

@FunctionalInterface
public interface RouteHandler {
    ApiResponse handle(ApiRequest request) throws JSONException;
}
