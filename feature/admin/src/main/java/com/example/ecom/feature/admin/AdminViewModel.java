package com.example.ecom.feature.admin;

import android.app.Application;
import android.content.*;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.feature.admin.data.*;
import org.json.*;
import java.util.*;
import java.util.function.Consumer;

/** Only server data enters the live admin flow. Settings remain device-local. */
public final class AdminViewModel extends BaseViewModel {
    private final AdminApi api;
    private final SharedPreferences preferences;
    private final Set<String> mutations = new HashSet<>();
    public AdminViewModel(Application app) {
        super(app); api = new AdminApi(app);
        preferences = app.getSharedPreferences("admin_device_settings", Context.MODE_PRIVATE);
    }
    public boolean busy(String key) { return mutations.contains(key); }
    public void categories(ApiCallback<List<AdminCategory>> cb) { api.categories(cb); }
    public void product(long id, ApiCallback<AdminProduct> cb) { api.product(id, cb); }
    public void saveProduct(AdminProduct p, ApiCallback<AdminProduct> cb) { api.saveProduct(p, cb); }
    public void deleteProduct(long id, ApiCallback<Void> cb) { api.deleteProduct(id, cb); }
    public void orders(String status, Long userId, ApiCallback<List<AdminOrder>> cb) { api.orders(status, userId, cb); }
    public void order(long id, ApiCallback<AdminOrder> cb) { api.order(id, cb); }
    public void orderStatus(long id, String status, ApiCallback<AdminOrder> cb) { api.orderStatus(id, status, cb); }
    public void users(String keyword, String status, ApiCallback<List<AdminCustomer>> cb) { api.users(keyword, status, cb); }
    public void dashboard(ApiCallback<JSONObject> cb) { api.dashboard(cb); }
    public static final class Catalog {
        public List<AdminCategory> categories;
        public List<AdminProduct> products;
        public AdminProduct product;
    }
    public static final class UserDetail {
        public AdminCustomer user;
        public JSONObject stats;
        public List<AdminOrder> orders;
    }
    public static final class Revenue {
        public JSONObject dashboard;
        public JSONArray topProducts, categories;
    }
    private static <T> ApiCallback<T> then(ApiCallback<?> target, Consumer<T> next) {
        return new ApiCallback<T>() {
            public void onSuccess(T data) { next.accept(data); }
            public void onError(String message, int status) { target.onError(message, status); }
        };
    }
    public void catalog(ApiCallback<Catalog> cb) {
        Catalog result = new Catalog();
        api.categories(then(cb, categories -> {
            result.categories = categories;
            api.products(then(cb, products -> { result.products = products; cb.onSuccess(result); }));
        }));
    }
    public void form(long id, ApiCallback<Catalog> cb) {
        Catalog result = new Catalog();
        api.categories(then(cb, categories -> {
            result.categories = categories;
            if (id == 0) { result.product = new AdminProduct(); cb.onSuccess(result); }
            else api.product(id, then(cb, product -> { result.product = product; cb.onSuccess(result); }));
        }));
    }
    public void userDetail(long id, ApiCallback<UserDetail> cb) {
        UserDetail result = new UserDetail();
        api.user(id, then(cb, data -> {
            try { result.user = AdminCustomer.fromJson(data.getJSONObject("user")); result.stats = data.getJSONObject("stats"); }
            catch (JSONException e) { cb.onError("Dữ liệu người dùng không hợp lệ", -1); return; }
            api.orders(null, id, then(cb, orders -> { result.orders = orders; cb.onSuccess(result); }));
        }));
    }
    public void revenue(ApiCallback<Revenue> cb) {
        Revenue result = new Revenue();
        api.dashboard(then(cb, dashboard -> {
            result.dashboard = dashboard;
            api.topProducts(then(cb, top -> {
                result.topProducts = top;
                api.categoryStats(then(cb, categories -> { result.categories = categories; cb.onSuccess(result); }));
            }));
        }));
    }
    public <T> void mutate(String key, Consumer<ApiCallback<T>> request, ApiCallback<T> cb) {
        if (!mutations.add(key)) { cb.onError(getApplication().getString(R.string.admin_busy), -1); return; }
        request.accept(new ApiCallback<T>() {
            public void onSuccess(T data) { mutations.remove(key); cb.onSuccess(data); }
            public void onError(String message, int status) { mutations.remove(key); cb.onError(message, status); }
        });
    }
    public String setting(String key, String fallback) { return preferences.getString(key, fallback); }
    public boolean notifications() { return preferences.getBoolean("notifications", true); }
    public void saveSettings(String name, String email, boolean notifications) {
        preferences.edit().putString("name", name).putString("email", email).putBoolean("notifications", notifications).apply();
    }
}
