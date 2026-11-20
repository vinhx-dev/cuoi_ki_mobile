package com.example.ecom.feature.profile.data;

import android.content.Context;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;
import com.example.ecom.core.api.auth.AuthRepository;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileApi {

    public static class ShoppingStats {
        public final int totalOrdersCount;
        public final long totalPaidAmount;
        public final int activeOrdersCount;

        public ShoppingStats(int totalOrdersCount, long totalPaidAmount, int activeOrdersCount) {
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
        apiClient.get("/orders", null, data -> {
            int totalCount = 0;
            long totalPaid = 0;
            int activeCount = 0;

            if (data instanceof JSONArray) {
                JSONArray arr = (JSONArray) data;
                totalCount = arr.length();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String status = obj.optString("status", "PENDING");
                    long total = obj.optLong("total", obj.optLong("totalPrice", 0));

                    if ("DELIVERED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        totalPaid += total;
                    }
                    if ("PENDING".equalsIgnoreCase(status) || "SHIPPING".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
                        activeCount++;
                    }
                }
            }
            return new ShoppingStats(totalCount, totalPaid, activeCount);
        }, cb);
    }

    public void logout(ApiCallback<Object> cb) {
        api.logout(cb);
    }
}
