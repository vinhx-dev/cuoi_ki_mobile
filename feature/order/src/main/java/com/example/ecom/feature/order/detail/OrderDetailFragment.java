package com.example.ecom.feature.order.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppPages;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.format.Formatter;
import com.example.ecom.feature.order.R;
import com.example.ecom.feature.order.data.OrderItem;

/** Chi tiết đơn hàng + huỷ đơn. ≈ OrderDetailPage.jsx */
public class OrderDetailFragment extends BaseFragment {

    public OrderDetailFragment() {
        super(R.layout.fragment_order_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OrderDetailViewModel vm = viewModel(OrderDetailViewModel.class);

        TextView textStatus = view.findViewById(R.id.textStatus);
        TextView textDate = view.findViewById(R.id.textDate);
        TextView textAddress = view.findViewById(R.id.textAddress);
        TextView textItems = view.findViewById(R.id.textItems);
        TextView textTotal = view.findViewById(R.id.textTotal);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Sự kiện
        btnCancel.setOnClickListener(v -> vm.cancel(apiCallback(order ->
                toast(getString(R.string.msg_order_cancelled)))));

        // Render
        observe(vm.getOrder(), o -> {
            textStatus.setText(getString(R.string.order_status, Formatter.orderStatus(o.status)));
            textDate.setText(getString(R.string.order_date, Formatter.dateTime(o.createdAt)));
            textAddress.setText(getString(R.string.order_address, o.address));

            StringBuilder sb = new StringBuilder();
            for (OrderItem i : o.items) {
                sb.append(getString(R.string.order_item_line, i.productName, i.quantity,
                        Formatter.money(i.price * i.quantity))).append('\n');
            }
            textItems.setText(sb.toString().trim());
            textTotal.setText(getString(R.string.order_total, Formatter.money(o.total)));
            btnCancel.setVisibility(o.canCancel() ? View.VISIBLE : View.GONE);
        });

        vm.load(requireArguments().getInt(AppPages.ARG_ORDER_ID));
    }
}
