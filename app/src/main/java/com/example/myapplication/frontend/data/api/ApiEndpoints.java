package com.example.myapplication.frontend.data.api;

/** Khai báo tập trung toàn bộ endpoint. */
public final class ApiEndpoints {

    private ApiEndpoints() {}

    public static final String LOGIN = "/auth/login";
    public static final String REGISTER = "/auth/register";

    public static final String ME = "/users/me";

    public static final String PRODUCTS = "/products";
    public static String product(int id) { return "/products/" + id; }

    public static final String ORDERS = "/orders";
    public static String order(int id) { return "/orders/" + id; }
    public static String cancelOrder(int id) { return "/orders/" + id + "/cancel"; }
}
