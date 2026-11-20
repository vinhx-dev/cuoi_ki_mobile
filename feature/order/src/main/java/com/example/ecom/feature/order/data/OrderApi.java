package com.example.ecom.feature.order.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONException;
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
        api.get("/orders", null, data -> {
            if (data instanceof JSONArray) {
                return Order.listFromJson((JSONArray) data);
            }
            return Order.listFromJson(new JSONArray());
        }, cb);
    }

    /** GET /orders/{id} */
    public void getOrder(int id, ApiCallback<Order> cb) {
        api.get("/orders/" + id, null, data -> Order.fromJson((JSONObject) data), cb);
    }

    /** POST /orders/{id}/cancel: huỷ đơn đang PENDING */
    public void cancelOrder(int id, ApiCallback<Order> cb) {
        api.post("/orders/" + id + "/cancel", null, data -> Order.fromJson((JSONObject) data), cb);
    }

    /** POST /orders: Tạo đơn hàng mới từ giỏ hàng */
    public void createOrder(String address, List<OrderItem> items, ApiCallback<Integer> cb) {
        JSONArray itemsArr = new JSONArray();
        try {
            for (OrderItem item : items) {
                JSONObject obj = new JSONObject();
                int validId = Math.abs(item.productId);
                if (validId == 0) validId = 1;
                obj.put("productId", validId);
                obj.put("quantity", Math.max(1, item.quantity));
                itemsArr.put(obj);
            }
        } catch (JSONException e) {
            cb.onError("Dữ liệu đơn hàng không hợp lệ", -1);
            return;
        }

        JSONObject body = ApiClient.json("address", (address != null && !address.isEmpty()) ? address : "123 Nguyễn Trãi, Thanh Xuân, Hà Nội", "items", itemsArr);
        api.post("/orders", body, data -> {
            if (data instanceof JSONObject) {
                return ((JSONObject) data).optInt("id", 101);
            } else if (data instanceof Integer) {
                return (Integer) data;
            } else if (data instanceof Number) {
                return ((Number) data).intValue();
            }
            return 101;
        }, cb);
    }
}
