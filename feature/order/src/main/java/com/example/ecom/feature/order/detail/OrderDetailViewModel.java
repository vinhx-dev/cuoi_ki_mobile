package com.example.ecom.feature.order.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.order.data.Order;
import com.example.ecom.feature.order.data.OrderApi;

/** ≈ useOrderDetail(orderId): chi tiết đơn + huỷ đơn. */
public class OrderDetailViewModel extends BaseViewModel {

    private final OrderApi orderApi;
    private final MutableLiveData<UiState<Order>> order = new MutableLiveData<>();
    private Integer loadedId = null;

    public OrderDetailViewModel(@NonNull Application app) {
        super(app);
        orderApi = new OrderApi(app);
    }

    public LiveData<UiState<Order>> getOrder() {
        return order;
    }

    /** Tải đơn. Đã tải rồi (vd. khi xoay màn hình) thì không gọi API lại. */
    public void load(int orderId) {
        if (loadedId != null && loadedId == orderId) return;
        loadedId = orderId;
        fetch(order, cb -> orderApi.getOrder(orderId, cb));
    }

    /** Huỷ đơn; thành công thì màn hình tự hiện trạng thái mới. */
    public void cancel(ApiCallback<Order> callback) {
        if (loadedId == null) return;
        orderApi.cancelOrder(loadedId, updateOnSuccess(order, callback));
    }
}
