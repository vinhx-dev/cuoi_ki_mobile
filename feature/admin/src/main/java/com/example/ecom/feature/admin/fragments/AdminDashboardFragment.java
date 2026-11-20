package com.example.ecom.feature.admin.fragments;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import org.json.JSONObject;

public class AdminDashboardFragment extends AdminFragment {
    @Override protected int layout() { return R.layout.fragment_admin_dashboard; }
    @Override protected void bind(View view) {
        String date=new java.text.SimpleDateFormat("EEEE, dd 'tháng' MM",AdminUi.VI).format(new java.util.Date());
        AdminUi.text(view,R.id.dashboardDate,date.substring(0,1).toUpperCase(AdminUi.VI)+date.substring(1));
        view.findViewById(R.id.btnAdminProducts).setOnClickListener(v -> AdminRouter.toAdminProducts(this));
        view.findViewById(R.id.btnAdminOrders).setOnClickListener(v -> AdminRouter.toAdminOrders(this));
        view.findViewById(R.id.btnAdminUsers).setOnClickListener(v -> AdminRouter.toAdminUsers(this));
        view.findViewById(R.id.btnAdminRevenue).setOnClickListener(v -> AdminRouter.toAdminRevenue(this));
        view.findViewById(R.id.btnAdminSettings).setOnClickListener(v -> AdminRouter.toAdminSettings(this));
    }
    @Override protected void refresh() {
        this.<JSONObject>load(adminViewModel::dashboard, data -> {
            AdminUi.text(root,R.id.metricOrders,AdminStatsUi.number(data,"totalOrders"));
            AdminUi.text(root,R.id.metricRevenue,AdminStatsUi.money(data,"totalRevenue"));
            AdminUi.text(root,R.id.metricProducts,AdminStatsUi.number(data,"totalProducts"));
            AdminUi.text(root,R.id.metricUsers,AdminStatsUi.number(data,"totalUsers"));
            AdminUi.text(root,R.id.dashboardToday,AdminStatsUi.money(data,"revenueToday"));
            AdminUi.text(root,R.id.dashboardMonth,AdminStatsUi.money(data,"revenueThisMonth"));
            AdminStatsRenderer.orders(root.findViewById(R.id.dashboardOrderStates),data);
            AdminStatsRenderer.health(root.findViewById(R.id.dashboardStats),data);
        });
    }
}
