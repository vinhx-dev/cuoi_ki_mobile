package com.example.ecom.feature.product.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;

import org.json.JSONObject;

/** Các API trang chi tiết sản phẩm dùng. */
public class ProductApi {

    private final ApiClient api;

    public ProductApi(Context context) {
        this.api = ApiClient.getInstance(context);
    }

    /** GET /products/{id} */
    public void getProduct(int id, ApiCallback<Product> cb) {
        api.requestStrict("GET", "/products/" + id, null, null, data -> Product.fromJson((JSONObject) data), cb);
    }

    /** POST /orders: đặt nhanh 1 sản phẩm, trả về id đơn mới. Địa chỉ rỗng -> backend dùng địa chỉ mặc định. */
    public void createOrder(int productId, int quantity, ApiCallback<Integer> cb) {
        createOrder(productId, quantity, "", cb);
    }

    public void createOrder(int productId, int quantity, String size, ApiCallback<Integer> cb) {
        api.requestStrict("POST", "/orders", null,
                ApiClient.json("productId", productId, "quantity", quantity, "size", size),
                data -> ((JSONObject) data).getInt("id"), cb);
    }
}
