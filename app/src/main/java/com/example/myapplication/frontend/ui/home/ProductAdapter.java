package com.example.myapplication.frontend.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.frontend.data.model.Product;
import com.example.myapplication.frontend.utils.Formatter;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnItemClick {
        void onClick(Product product);
    }

    private final List<Product> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    public ProductAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submit(List<Product> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.name.setText(p.name);
        h.price.setText(Formatter.money(p.price));
        h.stock.setText("Còn " + p.stock + " sản phẩm");
        h.itemView.setOnClickListener(v -> onItemClick.onClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, price, stock;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.textProductName);
            price = v.findViewById(R.id.textProductPrice);
            stock = v.findViewById(R.id.textProductStock);
        }
    }
}
