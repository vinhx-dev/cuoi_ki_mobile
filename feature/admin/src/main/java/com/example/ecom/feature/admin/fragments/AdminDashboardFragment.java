package com.example.ecom.feature.admin.fragments;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.models.ChartPoint;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import java.util.*;
public class AdminDashboardFragment extends AdminFragment {
    @Override protected int layout() {
        return R.layout.fragment_admin_dashboard;
    }
    @Override protected void bind(View view) {
        view.findViewById(R.id.btnAdminProducts).setOnClickListener(v->AdminRouter.toAdminProducts(this));
        view.findViewById(R.id.btnAdminOrders).setOnClickListener(v->AdminRouter.toAdminOrders(this));
        view.findViewById(R.id.btnAdminUsers).setOnClickListener(v->AdminRouter.toAdminUsers(this));
        view.findViewById(R.id.btnAdminRevenue).setOnClickListener(v->AdminRouter.toAdminRevenue(this));
        view.findViewById(R.id.btnAdminSettings).setOnClickListener(v->AdminRouter.toAdminSettings(this));
    }
    @Override protected void refresh() {
        AdminUi.text(root,R.id.metricOrders,AdminUi.number(adminViewModel.getOrders().size()));
        AdminUi.text(root,R.id.metricRevenue,AdminUi.money(adminViewModel.dashboardRevenue()));
        AdminUi.text(root,R.id.metricProducts,AdminUi.number(adminViewModel.productCount()));
        AdminUi.text(root,R.id.metricUsers,AdminUi.number(adminViewModel.customerCount()));
        List<ChartPoint> points=new ArrayList<>();
        long[] values= {
            5000000,10000000,8200000,12000000,10500000,13000000,18000000
        }
        ;
        for(int i=0;i<7;i++)points.add(new ChartPoint(String.format(Locale.US,"%02d/11",9+i),values[i]));
        ((RevenueChartView)root.findViewById(R.id.dashboardChart)).setPoints(points,false);
    }
}
