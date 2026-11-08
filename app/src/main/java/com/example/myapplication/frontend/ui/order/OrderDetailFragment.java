package com.example.myapplication.frontend.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.frontend.navigation.AppRouter;
import com.example.myapplication.frontend.ui.base.BaseFragment;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.frontend.data.model.Order;
import com.example.myapplication.frontend.data.model.OrderItem;
import com.example.myapplication.frontend.data.repository.OrderRepository;
import com.example.myapplication.frontend.utils.Formatter;

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
        setTitle("Đơn hàng #" + orderId);

        btnCancel.setOnClickListener(v ->
                orderRepository.cancelOrder(orderId, apiCallback(order -> {
                    toast("Đã hủy đơn hàng");
                    bind(order);
                })));

        orderRepository.getOrder(orderId, apiCallback(this::bind));
    }

    private void bind(Order o) {
        textStatus.setText("Trạng thái: " + Formatter.orderStatus(o.status));
        textDate.setText("Ngày đặt: " + Formatter.dateTime(o.createdAt));
        textAddress.setText("Giao đến: " + o.address);

        StringBuilder sb = new StringBuilder();
        for (OrderItem i : o.items) {
            sb.append("• ").append(i.productName)
              .append("  x").append(i.quantity)
              .append("  —  ").append(Formatter.money(i.price * i.quantity))
              .append('\n');
        }
        textItems.setText(sb.toString().trim());
        textTotal.setText("Tổng: " + Formatter.money(o.total));
        btnCancel.setVisibility(o.canCancel() ? View.VISIBLE : View.GONE);
    }
}
