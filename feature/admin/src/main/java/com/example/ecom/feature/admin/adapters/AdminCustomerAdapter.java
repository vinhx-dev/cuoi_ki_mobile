package com.example.ecom.feature.admin.adapters;
import android.view.View;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.feature.admin.data.AdminCustomer;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
public class AdminCustomerAdapter extends AdminListAdapter<AdminCustomer> {
    public AdminCustomerAdapter(Consumer<AdminCustomer> click) {
        super(R.layout.item_admin_customer,click);
    }
    @Override protected void bind(View v,AdminCustomer c) {
        AdminUi.text(v,R.id.itemInitials,AdminUi.initials(c.name));
        AdminUi.text(v,R.id.itemName,c.name);
        AdminUi.text(v,R.id.itemEmail,c.email);
        AdminUi.text(v,R.id.itemPhone,c.phone.isEmpty()?v.getContext().getString(R.string.admin_phone_unset):c.phone);
        AdminUi.text(v,R.id.itemOrderCount,v.getContext().getString("ROLE_ADMIN".equals(c.role)?R.string.admin_role_admin:R.string.admin_role_customer));
        AdminUi.badge(v.findViewById(R.id.itemUserStatus),AdminUi.userStatus(v.getContext(),c.status),
            "ACTIVE".equals(c.status)?R.color.admin_success:"BLOCKED".equals(c.status)?R.color.admin_danger:R.color.admin_warning,
            "ACTIVE".equals(c.status)?R.color.admin_success_light:"BLOCKED".equals(c.status)?R.color.admin_danger_light:R.color.admin_warning_light);
    }
}
