package com.example.ecom.feature.admin.adapters;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import java.util.function.Consumer;
public abstract class AdminListAdapter<T> extends RecyclerView.Adapter<AdminListAdapter.Holder> {
    private final int layout;
    private final List<T> items=new ArrayList<>();
    private final Consumer<T> click;
    protected AdminListAdapter(int layout,Consumer<T> click) {
        this.layout=layout;
        this.click=click;
    }
    public void submit(List<T> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }
    protected abstract void bind(View view,T item);
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout,parent,false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder,int position) {
        T item=items.get(position);
        bind(holder.itemView,item);
        holder.itemView.setOnClickListener(v->click.accept(item));
    }
    @Override public int getItemCount() {
        return items.size();
    }
    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(View v) {
            super(v);
        }
    }
}
