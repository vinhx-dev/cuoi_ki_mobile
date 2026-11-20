package com.example.ecom.feature.home.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Các API trang chủ dùng. ≈ api/products.js */
public class ProductApi {

    private final ApiClient api;

    public ProductApi(Context context) {
        this.api = ApiClient.getInstance(context);
    }

    /** GET /products/search?keyword= */
    public void getProducts(String keyword, ApiCallback<List<Product>> cb) {
        Map<String, String> query = new HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) query.put("keyword", keyword.trim());
        api.requestStrict("GET", query.isEmpty() ? "/products/home" : "/products/search", query, null, data -> Product.listFromJson((JSONArray) data), cb);
    }
}
