package com.example.ecom.feature.admin.fragments;
import android.view.View;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.adapters.AdminOrderAdapter;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.core.data.model.admin.*;
import com.example.ecom.core.navigation.AppRouter;
import java.util.*;
public class AdminUserDetailFragment extends AdminFragment {
    private int id;
    private AdminOrderAdapter adapter;
    @Override protected int layout() {
        return R.layout.fragment_admin_user_detail;
    }
    @Override protected void bind(View view) {
        id=requireArguments().getInt(AppRouter.ARG_ADMIN_USER_ID);
        adapter=new AdminOrderAdapter(repository,o->AppRouter.toAdminOrderDetail(this,o.id));
        RecyclerView list=view.findViewById(R.id.customerOrders);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
    }
    @Override protected void refresh() {
        AdminCustomer c=repository.customer(id);
        if(c==null) {
            toast(getString(R.string.admin_not_found));
            AppRouter.back(this);
            return;
        }
        AdminUi.text(root,R.id.textCustomerName,c.name);
        AdminUi.text(root,R.id.textCustomerEmail,c.email);
        AdminUi.text(root,R.id.textCustomerPhone,c.phone);
        AdminUi.text(root,R.id.textCustomerAddress,c.address);
        List<AdminOrder> orders=new ArrayList<>();
        for(AdminOrder o:repository.getOrders())if(o.customerId==id)orders.add(o);
        AdminUi.text(root,R.id.textCustomerOrders,getString(R.string.admin_user_order_count,orders.size()));
        adapter.submit(orders);
        root.findViewById(R.id.emptyState).setVisibility(orders.isEmpty()?View.VISIBLE:View.GONE);
    }
    @Override public void onDestroyView() {
        adapter=null;
        super.onDestroyView();
    }
}
