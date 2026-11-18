package com.example.ecom.feature.product.data;

import org.json.JSONObject;

/** Thông tin đầy đủ của 1 sản phẩm (trang chi tiết). */
public class Product {
    public final int id;
    public final String name;
    public final String description;
    public final long price;
    public final int stock;
    public final String imageUrl;

    public Product(int id, String name, String description, long price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public static Product fromJson(JSONObject j) {
        return new Product(j.optInt("id"), j.optString("name"), j.optString("description"),
                j.optLong("price"), j.optInt("stock"), j.optString("imageUrl"));
    }
}
