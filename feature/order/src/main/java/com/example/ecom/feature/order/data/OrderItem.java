package com.example.ecom.feature.order.data;

import org.json.JSONObject;

public class OrderItem {
    public final int productId;
    public final String productName;
    public final long price;
    public final int quantity;

    public OrderItem(int productId, String productName, long price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public static OrderItem fromJson(JSONObject j) {
        return new OrderItem(j.optInt("productId"), j.optString("productName"),
                j.optLong("price"), j.optInt("quantity"));
    }
}
