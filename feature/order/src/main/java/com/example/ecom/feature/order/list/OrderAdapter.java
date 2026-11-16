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

    private final List<Order> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    public OrderAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submit(List<Order> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = items.get(position);
        h.code.setText(h.itemView.getContext().getString(R.string.order_code, o.id));
        h.status.setText(Formatter.orderStatus(o.status));
        h.total.setText(Formatter.money(o.total));
        h.date.setText(Formatter.dateTime(o.createdAt));
        h.itemView.setOnClickListener(v -> onItemClick.onClick(o));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView code, status, total, date;

        VH(View v) {
            super(v);
            code = v.findViewById(R.id.textOrderCode);
            status = v.findViewById(R.id.textOrderStatus);
            total = v.findViewById(R.id.textOrderTotal);
            date = v.findViewById(R.id.textOrderDate);
        }
    }
}
