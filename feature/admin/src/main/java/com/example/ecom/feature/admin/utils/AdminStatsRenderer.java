package com.example.ecom.feature.admin.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.example.ecom.feature.admin.R;
import org.json.*;

/** Structured presentation of the backend metrics; no locally invented values. */
public final class AdminStatsRenderer {
    private AdminStatsRenderer() {}
    private static int dp(Context c,int value) { return AdminUi.dp(c,value); }
    private static TextView text(Context c,String value,int size,int color,boolean bold) {
        TextView view=new TextView(c); view.setText(value); view.setTextSize(size);
        view.setTextColor(ContextCompat.getColor(c,color)); view.setIncludeFontPadding(false);
        if(bold) view.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return view;
    }
    private static void divider(LinearLayout parent) {
        if(parent.getChildCount()==0) return;
        View divider=new View(parent.getContext());
        divider.setBackgroundColor(ContextCompat.getColor(parent.getContext(),R.color.admin_border));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(parent.getContext(),1));
        params.setMargins(0,dp(parent.getContext(),13),0,dp(parent.getContext(),13)); parent.addView(divider,params);
    }
    private static LinearLayout row(LinearLayout parent,String label,String value) {
        Context c=parent.getContext(); divider(parent);
        LinearLayout row=new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=text(c,label,13,R.color.admin_text_secondary,false);
        name.setPadding(0,0,dp(c,12),0);
        row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        TextView metric=text(c,value,15,R.color.admin_text,true);
        metric.setGravity(Gravity.END);
        row.addView(metric,new LinearLayout.LayoutParams(-2,-2)); parent.addView(row);
        return row;
    }
    private static void empty(LinearLayout parent,int message) {
        TextView text=text(parent.getContext(),parent.getContext().getString(message),13,R.color.admin_text_secondary,false);
        text.setPadding(0,dp(parent.getContext(),14),0,dp(parent.getContext(),14)); text.setGravity(Gravity.CENTER);
        parent.addView(text);
    }
    public static void orders(LinearLayout parent,JSONObject dashboard) {
        parent.removeAllViews();
        JSONObject statuses=dashboard.optJSONObject("ordersByStatus");
        if(statuses==null) { empty(parent,R.string.admin_no_results); return; }
        for(String status:new String[]{"PENDING","SHIPPING","DELIVERED","CANCELLED"}) {
            if(!statuses.has(status)) continue;
            Context c=parent.getContext(); divider(parent);
            LinearLayout row=new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView label=text(c,AdminUi.status(c,status),12,R.color.admin_text,false);
            AdminUi.orderBadge(label,status); row.addView(label,new LinearLayout.LayoutParams(-2,-2));
            row.addView(new View(c),new LinearLayout.LayoutParams(0,1,1));
            TextView value=text(c,AdminStatsUi.number(statuses,status),16,R.color.admin_text,true);
            row.addView(value,new LinearLayout.LayoutParams(-2,-2)); parent.addView(row);
        }
    }
    public static void health(LinearLayout parent,JSONObject data) {
        parent.removeAllViews();
        String[] keys={"activeUsers","blockedUsers","newUsersThisMonth","outOfStockProducts","totalCategories"};
        int[] labels={R.string.admin_active_users,R.string.admin_blocked_users,R.string.admin_new_users,R.string.admin_out_of_stock,R.string.admin_total_categories};
        for(int i=0;i<keys.length;i++) if(data.has(keys[i])) row(parent,parent.getContext().getString(labels[i]),AdminStatsUi.number(data,keys[i]));
    }
    public static void user(LinearLayout parent,JSONObject data) {
        parent.removeAllViews(); Context c=parent.getContext();
        row(parent,c.getString(R.string.admin_total_spent),AdminStatsUi.money(data,"totalSpent"));
        String[] keys={"purchasedOrders","pendingOrders","shippingOrders","cartItems"};
        int[] labels={R.string.admin_orders_total,R.string.admin_status_pending,R.string.admin_status_shipping,R.string.admin_cart_items};
        for(int i=0;i<keys.length;i++) if(data.has(keys[i])) row(parent,c.getString(labels[i]),AdminStatsUi.number(data,keys[i]));
    }
    public static void topProducts(LinearLayout parent,JSONArray data) {
        parent.removeAllViews(); Context c=parent.getContext();
        if(data.length()==0) { empty(parent,R.string.admin_top_empty); return; }
        for(int i=0;i<data.length();i++) {
            JSONObject product=data.optJSONObject(i); if(product==null) continue;
            divider(parent);
            LinearLayout row=new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView rank=text(c,String.valueOf(i+1),12,R.color.admin_primary,true);
            AdminUi.badge(rank,String.valueOf(i+1),R.color.admin_primary,R.color.admin_primary_light);
            row.addView(rank);
            TextView name=text(c,product.optString("name"),14,R.color.admin_text,true);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);
            params.setMargins(dp(c,12),0,dp(c,12),0); row.addView(name,params);
            TextView sold=text(c,c.getString(R.string.admin_sales_units,AdminStatsUi.number(product,"soldCount")),11,R.color.admin_text_secondary,false);
            row.addView(sold); parent.addView(row);
        }
    }
    public static void categories(LinearLayout parent,JSONArray data) {
        parent.removeAllViews();
        if(data.length()==0) { empty(parent,R.string.admin_category_empty); return; }
        Context c=parent.getContext(); long max=0;
        for(int i=0;i<data.length();i++) { JSONObject item=data.optJSONObject(i); if(item!=null) max=Math.max(max,item.optLong("productCount")); }
        for(int i=0;i<data.length();i++) {
            JSONObject category=data.optJSONObject(i); if(category==null) continue;
            row(parent,category.optString("name"),AdminStatsUi.number(category,"productCount"));
            if(max>0) {
                ProgressBar bar=new ProgressBar(c,null,android.R.attr.progressBarStyleHorizontal);
                bar.setMax(1000); bar.setProgress((int)(category.optLong("productCount")*1000/max));
                bar.setProgressTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(c,R.color.admin_primary)));
                bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(c,R.color.admin_primary_light)));
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(c,4));
                params.topMargin=dp(c,10); parent.addView(bar,params);
            }
        }
    }
}
