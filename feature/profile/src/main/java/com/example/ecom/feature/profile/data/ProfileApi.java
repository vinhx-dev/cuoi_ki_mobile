package com.example.ecom.feature.profile.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;
import com.example.ecom.core.api.auth.AuthRepository;

import org.json.JSONObject;

public class ProfileApi {

    public static class ShoppingStats {
        public final long totalOrdersCount;
        public final long totalPaidAmount;
        public final long activeOrdersCount;

        public ShoppingStats(long totalOrdersCount, long totalPaidAmount, long activeOrdersCount) {
            this.totalOrdersCount = totalOrdersCount;
            this.totalPaidAmount = totalPaidAmount;
            this.activeOrdersCount = activeOrdersCount;
        }
    }

    private final AuthRepository api;
    private final ApiClient apiClient;

    public ProfileApi(Context context) {
        this.api = new AuthRepository(context);
        this.apiClient = ApiClient.getInstance(context);
    }

    public void getMe(ApiCallback<User> cb) {
        api.me(new ApiCallback<com.example.ecom.core.data.User>() {
            @Override
            public void onSuccess(com.example.ecom.core.data.User u) {
                User profile = new User((int) u.id, u.getDisplayName(), u.phone != null ? u.phone : "", u.address != null ? u.address : "");
                profile.email = u.email != null ? u.email : "";
                cb.onSuccess(profile);
            }

            @Override
            public void onError(String m, int s) {
                cb.onError(m, s);
            }
        });
    }

    public void getShoppingStats(ApiCallback<ShoppingStats> cb) {
        apiClient.requestStrict("GET", "/users/me/stats", null, null, data -> {
            JSONObject stats = (JSONObject) data;
            return new ShoppingStats(stats.getLong("purchasedOrders"), stats.getLong("totalSpent"),
                    stats.getLong("pendingOrders") + stats.getLong("shippingOrders"));
        }, cb);
    }

    public void logout(ApiCallback<Object> cb) {
        api.logout(cb);
    }
}
