package com.example.ecom.feature.admin.fragments;
import android.view.*;
import android.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;

public class AdminOrderDetailFragment extends AdminFragment {
    private long id;
    private AdminOrder order;
    @Override protected int layout() { return R.layout.fragment_admin_order_detail; }
    @Override protected void bind(View view) {
        id=requireArguments().getLong(AdminRouter.ARG_ADMIN_ORDER_ID);
        view.findViewById(R.id.btnAdvanceOrder).setOnClickListener(v -> {
            if(order!=null && order.nextStatus()!=null) update(order.nextStatus());
        });
        view.findViewById(R.id.btnCancelOrder).setOnClickListener(v -> {
            if(order==null || !order.canCancel()) return;
            new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_cancel_order_title)
                .setMessage(getString(R.string.admin_cancel_order_message,getString(R.string.admin_order_code,id)))
                .setNegativeButton(R.string.admin_close,null)
                .setPositiveButton(R.string.admin_order_cancel,(d,which) -> update("CANCELLED")).show();
        });
    }
    private void update(String status) {
        this.<AdminOrder>action("order:"+id, cb -> adminViewModel.orderStatus(id,status,cb), data -> {
            render(data); toast(getString(R.string.admin_order_updated));
        }, R.id.btnAdvanceOrder, R.id.btnCancelOrder);
    }
    @Override protected void refresh() { this.<AdminOrder>load(cb -> adminViewModel.order(id,cb),this::render); }
    private void render(AdminOrder o) {
        order=o;
        AdminUi.text(root,R.id.textOrderCode,getString(R.string.admin_order_code,o.id));
        AdminUi.text(root,R.id.textOrderDate,getString(R.string.admin_order_date,AdminUi.dateTime(o.createdAt)));
        AdminUi.orderBadge(root.findViewById(R.id.textOrderStatus),o.status);
        AdminUi.text(root,R.id.textCustomerName,o.userName+"\n"+o.userEmail);
        AdminUi.text(root,R.id.textCustomerPhone,o.userPhone);
        AdminUi.text(root,R.id.textCustomerAddress,o.userAddress);
        AdminUi.text(root,R.id.textOrderTotal,AdminUi.money(o.total()));
        LinearLayout items=root.findViewById(R.id.orderItems); items.removeAllViews();
        for(AdminOrderLine line:o.items) {
            View row=LayoutInflater.from(themedContext()).inflate(R.layout.item_admin_order_line,items,false);
            AdminImageLoader.load((ImageView)row.findViewById(R.id.itemImage),line.image,R.drawable.admin_image_placeholder);
            AdminUi.text(row,R.id.itemName,line.name);
            AdminUi.text(row,R.id.itemInfo,getString(R.string.admin_order_line_server,line.size,line.quantity));
            AdminUi.text(row,R.id.itemPrice,AdminUi.money(line.price.multiply(java.math.BigDecimal.valueOf(line.quantity))));
            items.addView(row);
        }
        Button advance=root.findViewById(R.id.btnAdvanceOrder);
        advance.setVisibility(o.nextStatus()==null?View.GONE:View.VISIBLE);
        advance.setText("PENDING".equals(o.status)?R.string.admin_order_ship:R.string.admin_order_complete);
        root.findViewById(R.id.btnCancelOrder).setVisibility(o.canCancel()?View.VISIBLE:View.GONE);
    }
}
