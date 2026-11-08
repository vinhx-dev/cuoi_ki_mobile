package com.example.myapplication.frontend.data.repository;

import android.content.Context;

import com.example.myapplication.frontend.data.api.ApiCallback;
import com.example.myapplication.frontend.data.api.ApiClient;
import com.example.myapplication.frontend.data.api.ApiEndpoints;
import com.example.myapplication.frontend.data.model.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OrderRepository {

    private final ApiClient api;

    public OrderRepository(Context context) {
        this.api = ApiClient.getInstance(context);
    }

    public void getMyOrders(ApiCallback<List<Order>> cb) {
        api.get(ApiEndpoints.ORDERS, null, data -> Order.listFromJson((JSONArray) data), cb);
    }

    public void getOrder(int id, ApiCallback<Order> cb) {
        api.get(ApiEndpoints.order(id), null, data -> Order.fromJson((JSONObject) data), cb);
    }

    /** Đặt nhanh 1 sản phẩm. address rỗng -> backend dùng địa chỉ mặc định của user. */
    public void createOrder(int productId, int quantity, String address, ApiCallback<Order> cb) {
        JSONObject body = new JSONObject();
        try {
            JSONArray items = new JSONArray();
            items.put(new JSONObject().put("productId", productId).put("quantity", quantity));
            body.put("items", items).put("address", address != null ? address : "");
        } catch (JSONException ignored) { }
        api.post(ApiEndpoints.ORDERS, body, data -> Order.fromJson((JSONObject) data), cb);
    }

    public void cancelOrder(int id, ApiCallback<Order> cb) {
        api.post(ApiEndpoints.cancelOrder(id), null, data -> Order.fromJson((JSONObject) data), cb);
    }
}
