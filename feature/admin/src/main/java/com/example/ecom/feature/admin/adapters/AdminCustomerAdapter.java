package com.example.ecom.feature.admin.adapters;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.feature.admin.data.AdminCustomer;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
public class AdminCustomerAdapter extends AdminListAdapter<AdminCustomer> {
    private final IntUnaryOperator orderCount;
    public AdminCustomerAdapter(IntUnaryOperator orderCount,Consumer<AdminCustomer> click) {
        super(R.layout.item_admin_customer,click);
        this.orderCount=orderCount;
    }
    @Override protected void bind(View v,AdminCustomer c) {
        AdminUi.text(v,R.id.itemInitials,AdminUi.initials(c.name));
        AdminUi.text(v,R.id.itemName,c.name);
        AdminUi.text(v,R.id.itemEmail,c.email);
        AdminUi.text(v,R.id.itemPhone,c.phone);
        AdminUi.text(v,R.id.itemOrderCount,v.getContext().getString(R.string.admin_user_order_count,orderCount.applyAsInt(c.id)));
    }
}
