package com.example.ecom.feature.admin.fragments;
import android.view.*;
import android.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.core.data.model.admin.*;
import com.example.ecom.core.navigation.AppRouter;
public class AdminOrderDetailFragment extends AdminFragment {
    private int id;
    @Override protected int layout() {
        return R.layout.fragment_admin_order_detail;
    }
    @Override protected void bind(View view) {
        id=requireArguments().getInt(AppRouter.ARG_ADMIN_ORDER_ID);
        view.findViewById(R.id.btnAdvanceOrder).setOnClickListener(v-> {
            AdminOrder o=repository.order(id);
            if(o!=null&&o.nextStatus()!=null)update(o.nextStatus());
        }
        );
        view.findViewById(R.id.btnCancelOrder).setOnClickListener(v->new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_cancel_order_title).setMessage(getString(R.string.admin_cancel_order_message,getString(R.string.admin_order_code,id))).setNegativeButton(R.string.admin_close,null).setPositiveButton(R.string.admin_order_cancel,(d,which)->update("CANCELLED")).show());
    }
    private void update(String status) {
        toast(getString(repository.updateOrderStatus(id,status)?R.string.admin_order_updated:R.string.admin_order_conflict));
        refresh();
    }
    @Override protected void refresh() {
        AdminOrder o=repository.order(id);
        if(o==null) {
            toast(getString(R.string.admin_not_found));
            AppRouter.back(this);
            return;
        }
        AdminCustomer c=repository.customer(o.customerId);
        AdminUi.text(root,R.id.textOrderCode,getString(R.string.admin_order_code,o.id));
        AdminUi.text(root,R.id.textOrderDate,getString(R.string.admin_order_date,AdminUi.dateTime(o.createdAt)));
        AdminUi.text(root,R.id.textOrderStatus,AdminUi.status(requireContext(),o.status));
        AdminUi.text(root,R.id.textCustomerName,c==null?"":c.name);
        AdminUi.text(root,R.id.textCustomerPhone,c==null?"":c.phone);
        AdminUi.text(root,R.id.textCustomerAddress,c==null?"":c.address);
        AdminUi.text(root,R.id.textOrderTotal,AdminUi.money(o.total()));
        LinearLayout items=root.findViewById(R.id.orderItems);
        items.removeAllViews();
        for(AdminOrderLine line:o.items) {
            View row=LayoutInflater.from(themedContext()).inflate(R.layout.item_admin_order_line,items,false);
            ((ImageView)row.findViewById(R.id.itemImage)).setImageResource(AdminUi.image(line.imageKey));
            AdminUi.text(row,R.id.itemName,line.name);
            AdminUi.text(row,R.id.itemInfo,getString(R.string.admin_order_line_info,line.size,line.color,line.quantity));
            AdminUi.text(row,R.id.itemPrice,AdminUi.money(line.price*line.quantity));
            items.addView(row);
        }
        Button advance=root.findViewById(R.id.btnAdvanceOrder);
        advance.setVisibility(o.nextStatus()==null?View.GONE:View.VISIBLE);
        int action=R.string.admin_order_confirm;
        switch(o.status) {
            case "CONFIRMED":action=R.string.admin_order_process;
            break;
            case "PROCESSING":action=R.string.admin_order_ship;
            break;
            case "SHIPPING":action=R.string.admin_order_complete;
            break;
        }
        advance.setText(action);
        root.findViewById(R.id.btnCancelOrder).setVisibility(o.canCancel()?View.VISIBLE:View.GONE);
    }
}
