package com.example.ecom.feature.admin.adapters;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.core.data.model.admin.AdminOrder;
import com.example.ecom.core.data.repository.AdminRepository;
import java.util.function.Consumer;
public class AdminOrderAdapter extends AdminListAdapter<AdminOrder> {
    private final AdminRepository repository;
    public AdminOrderAdapter(AdminRepository repository,Consumer<AdminOrder> click) {
        super(R.layout.item_admin_order,click);
        this.repository=repository;
    }
    @Override protected void bind(View v,AdminOrder o) {
        AdminUi.text(v,R.id.itemCode,v.getContext().getString(R.string.admin_order_code,o.id));
        AdminUi.text(v,R.id.itemStatus,AdminUi.status(v.getContext(),o.status));
        AdminUi.text(v,R.id.itemCustomer,AdminUi.customerName(repository,o));
        AdminUi.text(v,R.id.itemDate,AdminUi.dateTime(o.createdAt));
        AdminUi.text(v,R.id.itemTotal,AdminUi.money(o.total()));
    }
}
