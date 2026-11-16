package com.example.ecom.feature.order.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.order.R;

/** Danh sách đơn hàng của tôi. ≈ OrdersPage.jsx */
public class OrdersFragment extends BaseFragment {

    public OrdersFragment() {
        super(R.layout.fragment_orders);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OrdersViewModel vm = viewModel(OrdersViewModel.class);

        TextView textEmpty = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerOrders);
        OrderAdapter adapter = new OrderAdapter(order -> AppRouter.toOrderDetail(this, order.id));
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // Render
        observe(vm.getOrders(), orders -> {
            adapter.submit(orders);
            textEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        vm.refresh();
    }
}
