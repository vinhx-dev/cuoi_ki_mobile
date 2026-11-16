package com.example.ecom.feature.order.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.ui.format.Formatter;
import com.example.ecom.feature.order.R;
import com.example.ecom.feature.order.data.Order;

import java.util.ArrayList;
import java.util.List;

/** Danh sách đơn hàng. ≈ <OrderList items onItemClick/> */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnItemClick {
        void onClick(Order order);
    }

    public interface OnCancelClick {
        void onCancel(Order order);
    }

    private final List<Order> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnCancelClick onCancelClick;

    public OrderAdapter(OnItemClick onItemClick, OnCancelClick onCancelClick) {
        this.onItemClick = onItemClick;
        this.onCancelClick = onCancelClick;
    }

    public void submit(List<Order> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_status, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = items.get(position);

        // Tên sản phẩm
        if (o.items != null && !o.items.isEmpty()) {
            String name = o.items.get(0).productName;
            if (o.items.size() > 1) {
                name += " (+ " + (o.items.size() - 1) + " sản phẩm khác)";
            }
            h.productName.setText(name);
        } else {
            h.productName.setText("Đơn hàng #" + o.id);
        }

        // Giá sản phẩm
        h.productPrice.setText(Formatter.money(o.total));

        // Trạng thái đơn hàng + Màu chấm tròn
        String statusText;
        int dotColor;

        if (Order.STATUS_PENDING.equals(o.status)) {
            statusText = "Đang xử lý";
            dotColor = 0xFFFFA000;
        } else if ("SHIPPING".equals(o.status)) {
            statusText = "Đang giao";
            dotColor = 0xFF1E88E5;
        } else if ("DELIVERED".equals(o.status)) {
            statusText = "Đã giao";
            dotColor = 0xFF4CAF50;
        } else if ("CANCELLED".equals(o.status)) {
            statusText = "Đã hủy";
            dotColor = 0xFFE53935;
        } else {
            statusText = Formatter.orderStatus(o.status);
            dotColor = 0xFF9E9E9E;
        }

        h.statusName.setText(statusText);
        if (h.statusDot != null && h.statusDot.getBackground() != null) {
            h.statusDot.getBackground().mutate().setTint(dotColor);
        }

        // Nút Hủy đơn
        if (o.canCancel() && onCancelClick != null) {
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> onCancelClick.onCancel(o));
        } else {
            h.btnCancel.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> onItemClick.onClick(o));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView productName, productPrice, statusName;
        final View statusDot, btnCancel;

        VH(View v) {
            super(v);
            productName = v.findViewById(R.id.textProductName);
            productPrice = v.findViewById(R.id.textProductPrice);
            statusName = v.findViewById(R.id.textStatusName);
            statusDot = v.findViewById(R.id.viewStatusDot);
            btnCancel = v.findViewById(R.id.btnCancelOrder);
        }
    }
}
