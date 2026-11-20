package com.example.ecom.feature.order.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/** Các API của feature đơn hàng. ≈ api/orders.js */
public class OrderApi {

    private final ApiClient api;

    public OrderApi(Context context) {
        this.api = ApiClient.getInstance(context);
    }

    /** GET /orders: đơn hàng của tôi */
    public void getMyOrders(ApiCallback<List<Order>> cb) {
        api.requestStrict("GET", "/orders", null, null, data -> Order.listFromJson((JSONArray) data), cb);
    }

    /** GET /orders/{id} */
    public void getOrder(int id, ApiCallback<Order> cb) {
        api.requestStrict("GET", "/orders/" + id, null, null, data -> Order.fromJson((JSONObject) data), cb);
    }

    public void cancelOrder(int id, ApiCallback<Order> cb) {
        api.requestStrict("PATCH", "/orders/" + id + "/cancel", null, null,
                data -> Order.fromJson((JSONObject) data), cb);
    }
}
