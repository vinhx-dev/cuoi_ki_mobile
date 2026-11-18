package com.example.ecom.feature.admin.adapters;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.core.data.model.admin.AdminCustomer;
import com.example.ecom.core.data.repository.AdminRepository;
import java.util.function.Consumer;
public class AdminCustomerAdapter extends AdminListAdapter<AdminCustomer> {
    private final AdminRepository repository;
    public AdminCustomerAdapter(AdminRepository repository,Consumer<AdminCustomer> click) {
        super(R.layout.item_admin_customer,click);
        this.repository=repository;
    }
    @Override protected void bind(View v,AdminCustomer c) {
        AdminUi.text(v,R.id.itemInitials,AdminUi.initials(c.name));
        AdminUi.text(v,R.id.itemName,c.name);
        AdminUi.text(v,R.id.itemEmail,c.email);
        AdminUi.text(v,R.id.itemPhone,c.phone);
        AdminUi.text(v,R.id.itemOrderCount,v.getContext().getString(R.string.admin_user_order_count,repository.customerOrderCount(c.id)));
    }
}
