package com.example.myapplication.frontend.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Product> listFromJson(JSONArray arr) throws JSONException {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        return list;
    }
}
