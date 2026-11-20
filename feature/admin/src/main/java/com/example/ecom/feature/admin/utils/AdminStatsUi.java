package com.example.ecom.feature.admin.utils;
import android.content.Context;
import com.example.ecom.feature.admin.R;
import org.json.*;
import java.math.BigDecimal;

/** Missing metrics stay unavailable; no sample values or inferred time series. */
public final class AdminStatsUi {
    private AdminStatsUi() {}
    public static String number(JSONObject j,String key) { return j.isNull(key)?"—":AdminUi.number(j.optLong(key)); }
    public static String money(JSONObject j,String key) {
        if(j.isNull(key)) return "—";
        try { return AdminUi.money(new BigDecimal(j.get(key).toString())); } catch(Exception e) { return "—"; }
    }
    public static String dashboard(Context c,JSONObject j) {
        StringBuilder s=new StringBuilder();
        append(s,c.getString(R.string.admin_revenue_today),money(j,"revenueToday"));
        append(s,c.getString(R.string.admin_revenue_month),money(j,"revenueThisMonth"));
        JSONObject statuses=j.optJSONObject("ordersByStatus");
        if(statuses!=null) for(String status:new String[]{"PENDING","SHIPPING","DELIVERED","CANCELLED"})
            append(s,AdminUi.status(c,status),number(statuses,status));
        String[] keys={"activeUsers","blockedUsers","newUsersThisMonth","outOfStockProducts","totalCategories"};
        int[] labels={R.string.admin_active_users,R.string.admin_blocked_users,R.string.admin_new_users,
            R.string.admin_out_of_stock,R.string.admin_total_categories};
        for(int i=0;i<keys.length;i++) if(j.has(keys[i])) append(s,c.getString(labels[i]),number(j,keys[i]));
        return s.toString();
    }
    public static String userStats(Context c,JSONObject j) {
        StringBuilder s=new StringBuilder();
        append(s,c.getString(R.string.admin_total_spent),money(j,"totalSpent"));
        String[] keys={"purchasedOrders","pendingOrders","shippingOrders","cartItems"};
        int[] labels={R.string.admin_orders_total,R.string.admin_status_pending,R.string.admin_status_shipping,R.string.admin_cart_items};
        for(int i=0;i<keys.length;i++) append(s,c.getString(labels[i]),number(j,keys[i]));
        return s.toString();
    }
    public static String topProducts(Context c,JSONArray data) {
        if(data.length()==0) return c.getString(R.string.admin_no_results);
        StringBuilder s=new StringBuilder();
        for(int i=0;i<data.length();i++) {
            JSONObject p=data.optJSONObject(i); if(p==null) continue;
            append(s,p.optString("name"),c.getString(R.string.admin_sold_count,p.optInt("soldCount")));
        }
        return s.toString();
    }
    public static String categories(Context c,JSONArray data) {
        if(data.length()==0) return c.getString(R.string.admin_no_results);
        StringBuilder s=new StringBuilder();
        for(int i=0;i<data.length();i++) {
            JSONObject category=data.optJSONObject(i); if(category==null) continue;
            append(s,category.optString("name"),c.getString(R.string.admin_category_product_count,category.optLong("productCount")));
        }
        return s.toString();
    }
    private static void append(StringBuilder s,String label,String value) {
        if(s.length()>0) s.append('\n');
        s.append(label).append(": ").append(value);
    }
}
