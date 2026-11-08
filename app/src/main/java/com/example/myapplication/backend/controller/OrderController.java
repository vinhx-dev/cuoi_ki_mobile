package com.example.myapplication.backend.controller;

import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.entity.User;
import com.example.myapplication.backend.mapper.JsonMapper;
import com.example.myapplication.backend.service.AuthService;
import com.example.myapplication.backend.service.OrderService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    // GET /orders
    public ApiResponse list(ApiRequest req) throws JSONException {
        User user = authService.authenticate(req.getToken());
        return ApiResponse.ok(JsonMapper.ordersToJson(orderService.getMyOrders(user)));
    }

    // GET /orders/{id}
    public ApiResponse detail(ApiRequest req) throws JSONException {
        User user = authService.authenticate(req.getToken());
        return ApiResponse.ok(JsonMapper.toJson(orderService.getMyOrder(user, req.getPathParamInt("id"))));
    }

    // POST /orders  body: { address, items: [{ productId, quantity }] }
    public ApiResponse create(ApiRequest req) throws JSONException {
        User user = authService.authenticate(req.getToken());
        JSONObject body = req.getBody();
        JSONArray arr = body.optJSONArray("items");
        List<OrderService.ItemInput> items = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject it = arr.getJSONObject(i);
                items.add(new OrderService.ItemInput(it.optInt("productId"), it.optInt("quantity")));
            }
        }
        return ApiResponse.created(JsonMapper.toJson(orderService.createOrder(user, items, body.optString("address"))));
    }

    // POST /orders/{id}/cancel
    public ApiResponse cancel(ApiRequest req) throws JSONException {
        User user = authService.authenticate(req.getToken());
        return ApiResponse.ok("Đã hủy đơn hàng",
                JsonMapper.toJson(orderService.cancelOrder(user, req.getPathParamInt("id"))));
    }
}
