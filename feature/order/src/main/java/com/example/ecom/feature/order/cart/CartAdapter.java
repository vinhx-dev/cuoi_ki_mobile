package com.example.ecom.feature.order.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.data.LocalCart;
import com.example.ecom.core.ui.format.Formatter;
import com.example.ecom.feature.order.R;

import java.util.ArrayList;
import java.util.List;

/** Adapter hiển thị danh sách sản phẩm trong giỏ hàng. */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface Listener {
        void onItemCheckChanged();
        void onItemQuantityChanged(LocalCart.CartItem item, int delta);
        void onItemDeleted(LocalCart.CartItem item);
    }

    private final List<LocalCart.CartItem> items = new ArrayList<>();
    private final Listener listener;

    public CartAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<LocalCart.CartItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public List<LocalCart.CartItem> getItems() {
        return items;
    }

    public void setAllChecked(boolean checked) {
        for (LocalCart.CartItem item : items) {
            item.checked = checked;
        }
        notifyDataSetChanged();
        if (listener != null) listener.onItemCheckChanged();
    }

    public boolean isAllChecked() {
        if (items.isEmpty()) return false;
        for (LocalCart.CartItem item : items) {
            if (!item.checked) return false;
        }
        return true;
    }

    public int getCheckedCount() {
        int count = 0;
        for (LocalCart.CartItem item : items) {
            if (item.checked) count++;
        }
        return count;
    }

    public long calculateCheckedTotal() {
        long total = 0;
        for (LocalCart.CartItem item : items) {
            if (item.checked) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LocalCart.CartItem item = items.get(position);

        h.productName.setText(item.product != null ? item.product.name : "Sản phẩm");
        h.productSize.setText("Size: " + item.size);
        h.productPrice.setText(Formatter.money(item.product != null ? item.product.price : 0));
        h.quantity.setText(String.valueOf(item.quantity));

        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(item.checked);
        h.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            item.checked = isChecked;
            if (listener != null) listener.onItemCheckChanged();
        });

        h.btnMinus.setOnClickListener(v -> {
            if (listener != null) listener.onItemQuantityChanged(item, -1);
        });

        h.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onItemQuantityChanged(item, +1);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onItemDeleted(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView productName, productSize, productPrice, quantity, btnMinus, btnPlus;
        final CheckBox checkBox;
        final ImageView btnDelete;

        VH(View v) {
            super(v);
            productName = v.findViewById(R.id.textProductName);
            productSize = v.findViewById(R.id.textProductSize);
            productPrice = v.findViewById(R.id.textProductPrice);
            quantity = v.findViewById(R.id.textQuantity);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            checkBox = v.findViewById(R.id.cbItem);
            btnDelete = v.findViewById(R.id.btnDeleteItem);
        }
    }
}
