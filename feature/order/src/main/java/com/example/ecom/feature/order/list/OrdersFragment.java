package com.example.ecom.feature.order.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.order.R;
import com.example.ecom.feature.order.data.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Trang Trạng thái đơn hàng. ≈ OrdersPage.jsx */
public class OrdersFragment extends BaseFragment {

    public OrdersFragment() {
        super(R.layout.fragment_order_status);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OrdersViewModel vm = viewModel(OrdersViewModel.class);

        // Thiết lập Bottom Navigation Bar
        BottomNavigationView bottomBar = view.findViewById(com.example.ecom.core.ui.R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setSelectedItemId(com.example.ecom.core.ui.R.id.tabOrders);
            bottomBar.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == com.example.ecom.core.ui.R.id.tabHome) {
                    AppRouter.toTab(this, "home");
                } else if (itemId == com.example.ecom.core.ui.R.id.tabOrders) {
                    AppRouter.toTab(this, "orders");
                } else if (itemId == com.example.ecom.core.ui.R.id.tabProfile) {
                    AppRouter.toTab(this, "profile");
                }
                return false;
            });
        }

        TextView textEmpty = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerOrderStatus);
        if (recycler == null) {
            recycler = view.findViewById(R.id.recyclerOrders);
        }

        OrderAdapter adapter = new OrderAdapter(
                order -> AppRouter.toOrderDetail(this, order.id),
                this::confirmCancelOrder
        );
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // Render
        observe(vm.getOrders(), orders -> {
            adapter.submit(orders);
            if (textEmpty != null) {
                textEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        vm.refresh();
    }

    private void confirmCancelOrder(Order order) {
        OrdersViewModel vm = viewModel(OrdersViewModel.class);
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + order.id + " này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    vm.cancelOrder(order.id, apiCallback(res -> {
                        toast("Đã hủy đơn hàng #" + order.id + " thành công");
                        vm.refresh();
                    }));
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }
}
