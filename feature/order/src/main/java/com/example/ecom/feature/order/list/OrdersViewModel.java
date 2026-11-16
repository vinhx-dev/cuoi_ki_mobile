package com.example.ecom.feature.order.list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.order.data.Order;
import com.example.ecom.feature.order.data.OrderApi;

import java.util.List;

/** ≈ useMyOrders(): danh sách đơn hàng của tôi. */
public class OrdersViewModel extends BaseViewModel {

    private final OrderApi orderApi;
    private final MutableLiveData<UiState<List<Order>>> orders = new MutableLiveData<>();

    public OrdersViewModel(@NonNull Application app) {
        super(app);
        orderApi = new OrderApi(app);
    }

    public LiveData<UiState<List<Order>>> getOrders() {
        return orders;
    }

    /** Luôn tải lại (đơn có thể vừa được tạo / huỷ ở trang khác); trong lúc tải vẫn hiện danh sách cũ. */
    public void refresh() {
        fetch(orders, orderApi::getMyOrders);
    }

    public void cancelOrder(int orderId, ApiCallback<Order> callback) {
        orderApi.cancelOrder(orderId, callback);
    }
}
