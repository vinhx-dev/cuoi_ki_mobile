package com.example.ecom.feature.admin.data;

import android.content.Context;
import com.example.ecom.core.api.*;
import org.json.*;
import java.util.*;

/** Shared bearer/refresh OkHttp stack, with mock fallback explicitly disabled. */
public final class AdminApi {
    private final ApiClient api;
    public AdminApi(Context context) { api = ApiClient.getInstance(context); }
    private interface ItemParser<T> { T parse(JSONObject j) throws JSONException; }
    private static <T> List<T> list(Object data, ItemParser<T> parser) throws JSONException {
        if (!(data instanceof JSONArray)) throw new JSONException("Expected array");
        JSONArray array = (JSONArray) data;
        List<T> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) result.add(parser.parse(array.getJSONObject(i)));
        return result;
    }
    private static JSONObject object(Object data) throws JSONException {
        if (!(data instanceof JSONObject)) throw new JSONException("Expected object");
        return (JSONObject) data;
    }
    public void products(ApiCallback<List<AdminProduct>> cb) {
        api.requestStrict("GET", "/admin/products", null, null, d -> list(d, AdminProduct::fromJson), cb);
    }
    public void product(long id, ApiCallback<AdminProduct> cb) {
        api.requestStrict("GET", "/admin/products/" + id, null, null, d -> AdminProduct.fromJson(object(d)), cb);
    }
    public void categories(ApiCallback<List<AdminCategory>> cb) {
        api.requestStrict("GET", "/categories", null, null, d -> list(d, AdminCategory::fromJson), cb);
    }
    public void saveProduct(AdminProduct p, ApiCallback<AdminProduct> cb) {
        try {
            api.requestStrict(p.id == 0 ? "POST" : "PUT", p.id == 0 ? "/products" : "/products/" + p.id,
                    null, p.body(), d -> AdminProduct.fromJson(object(d)), cb);
        } catch (JSONException e) { cb.onError("Dữ liệu sản phẩm không hợp lệ", -1); }
    }
    public void deleteProduct(long id, ApiCallback<Void> cb) {
        // A successful delete intentionally has data:null.
        api.requestStrict("DELETE", "/products/" + id, null, null, null, cb);
    }
    public void orders(String status, Long userId, ApiCallback<List<AdminOrder>> cb) {
        Map<String, String> query = new HashMap<>();
        query.put("status", status); query.put("userId", userId == null ? null : userId.toString());
        api.requestStrict("GET", "/admin/orders", query, null, d -> list(d, AdminOrder::fromJson), cb);
    }
    public void order(long id, ApiCallback<AdminOrder> cb) {
        api.requestStrict("GET", "/admin/orders/" + id, null, null, d -> AdminOrder.fromJson(object(d)), cb);
    }
    public void orderStatus(long id, String status, ApiCallback<AdminOrder> cb) {
        api.requestStrict("PATCH", "/admin/orders/" + id + "/status", null, ApiClient.json("status", status),
                d -> AdminOrder.fromJson(object(d)), cb);
    }
    public void users(String keyword, String status, ApiCallback<List<AdminCustomer>> cb) {
        Map<String, String> query = new HashMap<>(); query.put("keyword", keyword); query.put("status", status);
        api.requestStrict("GET", "/admin/users", query, null, d -> list(d, AdminCustomer::fromJson), cb);
    }
    public void user(long id, ApiCallback<JSONObject> cb) { getObject("/admin/users/" + id, cb); }
    public void userStatus(long id, String status, ApiCallback<AdminCustomer> cb) {
        api.requestStrict("PATCH", "/admin/users/" + id + "/status", null, ApiClient.json("status", status),
                d -> AdminCustomer.fromJson(object(d)), cb);
    }
    public void dashboard(ApiCallback<JSONObject> cb) { getObject("/admin/stats/dashboard", cb); }
    public void topProducts(ApiCallback<JSONArray> cb) { getArray("/admin/stats/top-products", cb); }
    public void categoryStats(ApiCallback<JSONArray> cb) { getArray("/admin/stats/categories", cb); }
    private void getObject(String path, ApiCallback<JSONObject> cb) {
        api.requestStrict("GET", path, null, null, AdminApi::object, cb);
    }
    private void getArray(String path, ApiCallback<JSONArray> cb) {
        api.requestStrict("GET", path, null, null, d -> {
            if (!(d instanceof JSONArray)) throw new JSONException("Expected array");
            return (JSONArray) d;
        }, cb);
    }
}
