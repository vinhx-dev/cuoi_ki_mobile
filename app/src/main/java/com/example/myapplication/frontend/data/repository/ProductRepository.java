package com.example.myapplication.frontend.data.repository;

import android.content.Context;

import com.example.myapplication.frontend.data.api.ApiCallback;
import com.example.myapplication.frontend.data.api.ApiClient;
import com.example.myapplication.frontend.data.api.ApiEndpoints;
import com.example.myapplication.frontend.data.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository {

    private final ApiClient api;

    public ProductRepository(Context context) {
        this.api = ApiClient.getInstance(context);
    }

    public void getProducts(String keyword, ApiCallback<List<Product>> cb) {
        Map<String, String> query = new HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) query.put("keyword", keyword.trim());
        api.get(ApiEndpoints.PRODUCTS, query, data -> Product.listFromJson((JSONArray) data), cb);
    }

    public void getProduct(int id, ApiCallback<Product> cb) {
        api.get(ApiEndpoints.product(id), null, data -> Product.fromJson((JSONObject) data), cb);
    }
}
