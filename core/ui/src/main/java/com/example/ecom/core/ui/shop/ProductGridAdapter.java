package com.example.ecom.core.ui.shop;
import android.view.*;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecom.core.data.ShopProduct;
import java.util.*;
import java.util.function.Consumer;
public final class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.Holder> {
    private final List<ShopProduct> items=new ArrayList<>();
    private final Consumer<ShopProduct> click;
    public ProductGridAdapter(Consumer<ShopProduct> click) {
        this.click=click;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }
    public void submit(List<ShopProduct> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }
    public static class Holder extends RecyclerView.ViewHolder {
        Holder(View v) {
            super(v);
        }
    }
    @NonNull public Holder onCreateViewHolder(@NonNull ViewGroup p,int type) {
        FrameLayout f=new FrameLayout(p.getContext());
        int gap=ShopViews.dp(p.getContext(),5);
        f.setPadding(gap,gap,gap,gap);
        f.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));
        return new Holder(f);
    }
    public void onBindViewHolder(@NonNull Holder h,int pos) {
        FrameLayout f=(FrameLayout)h.itemView;
        f.removeAllViews();
        f.addView(ShopViews.card(f.getContext(),items.get(pos),click));
    }
    public int getItemCount() {
        return items.size();
    }
}
