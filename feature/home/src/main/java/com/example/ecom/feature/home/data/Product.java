package com.example.ecom.feature.home.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Sản phẩm trong danh sách trang chủ (chỉ các trường trang này cần). */
public class Product {
    public final int id;
    public final String name;
    public final long price;
    public final int stock;

    public Product(int id, String name, long price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public static Product fromJson(JSONObject j) {
        return new Product(j.optInt("id"), j.optString("name"), j.optLong("price"), j.optInt("stock"));
    }

    public static List<Product> listFromJson(JSONArray arr) throws JSONException {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        return list;
    }
}
