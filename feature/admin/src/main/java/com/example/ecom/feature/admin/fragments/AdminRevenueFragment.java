package com.example.ecom.feature.admin.fragments;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.utils.*;

public class AdminRevenueFragment extends AdminFragment {
    @Override protected int layout() { return R.layout.fragment_admin_revenue; }
    @Override protected void bind(View view) {}
    @Override protected void refresh() {
        this.<AdminViewModel.Revenue>load(adminViewModel::revenue, data -> {
            AdminUi.text(root,R.id.revenueTotal,AdminStatsUi.money(data.dashboard,"totalRevenue"));
            AdminUi.text(root,R.id.revenueToday,AdminStatsUi.money(data.dashboard,"revenueToday"));
            AdminUi.text(root,R.id.revenueMonth,AdminStatsUi.money(data.dashboard,"revenueThisMonth"));
            AdminStatsRenderer.orders(root.findViewById(R.id.revenueStats),data.dashboard);
            AdminStatsRenderer.topProducts(root.findViewById(R.id.topProductsStats),data.topProducts);
            AdminStatsRenderer.categories(root.findViewById(R.id.categoryStats),data.categories);
        });
    }
}
