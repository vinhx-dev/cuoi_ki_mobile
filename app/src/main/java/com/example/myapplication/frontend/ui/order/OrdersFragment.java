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
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.frontend.data.repository.OrderRepository;

/** Danh sách đơn hàng của tôi. */
public class OrdersFragment extends BaseFragment {

    private OrderRepository orderRepository;
    private OrderAdapter adapter;
    private TextView textEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderRepository = new OrderRepository(requireContext());

        textEmpty = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerOrders);
        adapter = new OrderAdapter(order -> AppRouter.toOrderDetail(this, order.id));
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        orderRepository.getMyOrders(apiCallback(orders -> {
            adapter.submit(orders);
            textEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        }));
    }
}
