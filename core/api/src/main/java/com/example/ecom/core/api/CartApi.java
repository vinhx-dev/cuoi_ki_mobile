package com.example.ecom.core.api;

import android.content.Context;
import org.json.JSONObject;

public final class CartApi {
    private final ApiClient api;

    public CartApi(Context context) {
        api = ApiClient.getInstance(context);
    }

    public void add(int productId, int quantity, String size, ApiCallback<JSONObject> cb) {
        api.requestStrict("POST", "/cart/items", null,
                ApiClient.json("productId", productId, "quantity", quantity, "size", size),
                data -> (JSONObject) data, cb);
    }
}
