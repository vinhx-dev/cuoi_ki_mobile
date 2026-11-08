package com.example.myapplication.backend.controller;

import com.example.myapplication.backend.core.ApiRequest;
import com.example.myapplication.backend.core.ApiResponse;
import com.example.myapplication.backend.mapper.JsonMapper;
import com.example.myapplication.backend.service.ProductService;

import org.json.JSONException;

public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /products?keyword=...
    public ApiResponse list(ApiRequest req) throws JSONException {
        return ApiResponse.ok(JsonMapper.productsToJson(productService.getProducts(req.getQuery("keyword"))));
    }

    // GET /products/{id}
    public ApiResponse detail(ApiRequest req) throws JSONException {
        return ApiResponse.ok(JsonMapper.toJson(productService.getProduct(req.getPathParamInt("id"))));
    }
}
