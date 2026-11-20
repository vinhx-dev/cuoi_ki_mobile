package com.example.ecom.feature.order.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Đơn hàng (dùng chung cho trang danh sách và trang chi tiết đơn). */
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
        if (arr == null) arr = j.optJSONArray("orderItems");
        if (arr == null) arr = j.optJSONArray("lines");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                items.add(OrderItem.fromJson(arr.getJSONObject(i)));
            }
        }
        if (arr == null && j.has("productId")) {
            items.add(new OrderItem(j.getInt("productId"), j.getString("name"),
                    j.getLong("unitPrice"), j.getInt("quantity")));
        }
        int id = j.optInt("id", j.optInt("orderId", 0));
        String status = j.optString("status", j.optString("orderStatus", STATUS_PENDING));
        String address = j.optString("address", j.optString("shippingAddress", ""));
        long total = j.optLong("total", j.optLong("totalPrice", j.optLong("totalAmount", 0)));
        long createdAt = parseCreatedAt(j);

        return new Order(id, status, address, total, createdAt, items);
    }

    private static long parseCreatedAt(JSONObject j) {
        Object value = j.opt("createdAt");
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            String date = (String) value;
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.ROOT);
            format.setLenient(false);
            try {
                return format.parse(date).getTime();
            } catch (java.text.ParseException ignored) {
                return 0;
            }
        }
        return 0;
    }

    public static List<Order> listFromJson(JSONArray arr) throws JSONException {
        List<Order> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(fromJson(arr.getJSONObject(i)));
        }
        return list;
    }
}
