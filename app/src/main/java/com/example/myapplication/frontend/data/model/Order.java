package com.example.myapplication.frontend.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public static final String STATUS_PENDING = "PENDING";

    public final int id;
    public final String status;
    public final String address;
    public final long total;
    public final long createdAt;
    public final List<OrderItem> items;

    public Order(int id, String status, String address, long total, long createdAt, List<OrderItem> items) {
        this.id = id;
        this.status = status;
        this.address = address;
        this.total = total;
        this.createdAt = createdAt;
        this.items = items;
    }

    public boolean canCancel() {
        return STATUS_PENDING.equals(status);
    }

    public static Order fromJson(JSONObject j) throws JSONException {
        List<OrderItem> items = new ArrayList<>();
        JSONArray arr = j.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) items.add(OrderItem.fromJson(arr.getJSONObject(i)));
        }
        return new Order(j.optInt("id"), j.optString("status"), j.optString("address"),
                j.optLong("total"), j.optLong("createdAt"), items);
    }

    public static List<Order> listFromJson(JSONArray arr) throws JSONException {
        List<Order> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        return list;
    }
}
