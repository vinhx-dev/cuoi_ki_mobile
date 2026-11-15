package com.example.ecom.feature.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.data.model.Order;
import com.example.ecom.core.data.model.OrderItem;
import com.example.ecom.core.data.repository.OrderRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.format.Formatter;

public class OrderDetailFragment extends BaseFragment {

    private OrderRepository orderRepository;
    private TextView textStatus, textDate, textAddress, textItems, textTotal;
    private Button btnCancel;
    private int orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderRepository = new OrderRepository(requireContext());

        textStatus = view.findViewById(R.id.textStatus);
        textDate = view.findViewById(R.id.textDate);
        textAddress = view.findViewById(R.id.textAddress);
        textItems = view.findViewById(R.id.textItems);
        textTotal = view.findViewById(R.id.textTotal);
        btnCancel = view.findViewById(R.id.btnCancel);

        orderId = requireArguments().getInt(AppRouter.ARG_ORDER_ID);
        setTitle(getString(R.string.order_code, orderId));

        btnCancel.setOnClickListener(v ->
                orderRepository.cancelOrder(orderId, apiCallback(order -> {
                    toast(getString(R.string.msg_order_cancelled));
                    bind(order);
                })));

        orderRepository.getOrder(orderId, apiCallback(this::bind));
    }

    private void bind(Order o) {
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
    }
}
