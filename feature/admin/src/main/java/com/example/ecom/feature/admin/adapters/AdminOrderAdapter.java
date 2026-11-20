package com.example.ecom.feature.admin.adapters;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.feature.admin.data.AdminOrder;
import com.example.ecom.feature.admin.data.AdminCustomer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
public class AdminOrderAdapter extends AdminListAdapter<AdminOrder> {
    public AdminOrderAdapter(Consumer<AdminOrder> click) {
        super(R.layout.item_admin_order,click);
    }
    @Override protected void bind(View v,AdminOrder o) {
        AdminUi.text(v,R.id.itemCode,v.getContext().getString(R.string.admin_order_code,o.id));
        AdminUi.orderBadge(v.findViewById(R.id.itemStatus),o.status);
        AdminUi.text(v,R.id.itemCustomer,o.userName);
        AdminUi.text(v,R.id.itemDate,AdminUi.dateTime(o.createdAt));
        AdminUi.text(v,R.id.itemTotal,AdminUi.money(o.total()));
    }
}
