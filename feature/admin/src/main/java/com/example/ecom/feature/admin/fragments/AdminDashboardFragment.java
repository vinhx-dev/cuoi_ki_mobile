package com.example.ecom.feature.admin.fragments;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.models.ChartPoint;
import com.example.ecom.core.navigation.AppRouter;
import java.util.*;
public class AdminDashboardFragment extends AdminFragment {
    @Override protected int layout() {
        return R.layout.fragment_admin_dashboard;
    }
    @Override protected void bind(View view) {
        view.findViewById(R.id.btnAdminProducts).setOnClickListener(v->AppRouter.toAdminProducts(this));
        view.findViewById(R.id.btnAdminOrders).setOnClickListener(v->AppRouter.toAdminOrders(this));
        view.findViewById(R.id.btnAdminUsers).setOnClickListener(v->AppRouter.toAdminUsers(this));
        view.findViewById(R.id.btnAdminRevenue).setOnClickListener(v->AppRouter.toAdminRevenue(this));
        view.findViewById(R.id.btnAdminSettings).setOnClickListener(v->AppRouter.toAdminSettings(this));
    }
    @Override protected void refresh() {
        AdminUi.text(root,R.id.metricOrders,AdminUi.number(repository.getOrders().size()));
        AdminUi.text(root,R.id.metricRevenue,AdminUi.money(repository.dashboardRevenue()));
        AdminUi.text(root,R.id.metricProducts,AdminUi.number(repository.productCount()));
        AdminUi.text(root,R.id.metricUsers,AdminUi.number(repository.customerCount()));
        List<ChartPoint> points=new ArrayList<>();
        long[] values= {
            5000000,10000000,8200000,12000000,10500000,13000000,18000000
        }
        ;
        for(int i=0;i<7;i++)points.add(new ChartPoint(String.format(Locale.US,"%02d/11",9+i),values[i]));
        ((RevenueChartView)root.findViewById(R.id.dashboardChart)).setPoints(points,false);
    }
}
