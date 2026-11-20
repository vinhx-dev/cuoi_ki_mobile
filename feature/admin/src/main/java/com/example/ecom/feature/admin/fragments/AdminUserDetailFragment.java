package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.adapters.AdminOrderAdapter;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;

public class AdminUserDetailFragment extends AdminFragment {
    private long id;
    private AdminOrderAdapter adapter;
    @Override protected int layout() { return R.layout.fragment_admin_user_detail; }
    @Override protected void bind(View view) {
        id=requireArguments().getLong(AdminRouter.ARG_ADMIN_USER_ID);
        adapter=new AdminOrderAdapter(o -> AdminRouter.toAdminOrderDetail(this,o.id));
        RecyclerView list=view.findViewById(R.id.customerOrders);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
    }
    @Override protected void refresh() {
        this.<AdminViewModel.UserDetail>load(cb -> adminViewModel.userDetail(id,cb), data -> {
            AdminCustomer c=data.user;
            AdminUi.text(root,R.id.textCustomerName,c.name);
            AdminUi.text(root,R.id.textCustomerEmail,c.email+"\n"+getString("ROLE_ADMIN".equals(c.role)?R.string.admin_role_admin:R.string.admin_role_customer)+" · "+AdminUi.userStatus(requireContext(),c.status));
            AdminUi.text(root,R.id.textCustomerPhone,c.phone.isEmpty()?getString(R.string.admin_phone_unset):c.phone);
            AdminUi.text(root,R.id.textCustomerAddress,c.address.isEmpty()?getString(R.string.admin_address_unset):c.address);
            AdminStatsRenderer.user(root.findViewById(R.id.textCustomerOrders),data.stats);
            adapter.submit(data.orders);
            root.findViewById(R.id.emptyState).setVisibility(data.orders.isEmpty()?View.VISIBLE:View.GONE);
        });
    }
}
