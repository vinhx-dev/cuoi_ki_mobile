package com.example.ecom.feature.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.product.data.Product;
import com.example.ecom.feature.product.data.ProductApi;

/** ≈ useProductDetail(productId): thông tin sản phẩm + số lượng muốn mua + đặt hàng. */
public class ProductDetailViewModel extends BaseViewModel {

    private final ProductApi productApi;

    private final MutableLiveData<UiState<Product>> product = new MutableLiveData<>();
    private final MutableLiveData<Integer> quantity = new MutableLiveData<>(1); // ≈ useState(1)
    private Integer loadedId = null;

    public ProductDetailViewModel(@NonNull Application app) {
        super(app);
        productApi = new ProductApi(app);
    }

    public LiveData<UiState<Product>> getProduct() {
        return product;
    }

    public LiveData<Integer> getQuantity() {
        return quantity;
    }

    /** Tải sản phẩm. Đã tải rồi (vd. khi xoay màn hình) thì không gọi API lại. */
    public void load(int productId) {
        if (loadedId != null && loadedId == productId) return;
        loadedId = productId;
        fetch(product, cb -> productApi.getProduct(productId, cb));
    }

    public void changeQuantity(int delta) {
        Product p = currentProduct();
        if (p == null) return;
        quantity.setValue(Math.max(1, Math.min(p.stock, quantity.getValue() + delta)));
    }

    /** Đặt hàng; thành công trả về id đơn mới. */
    public void placeOrder(ApiCallback<Integer> callback) {
        Product p = currentProduct();
        if (p == null) return;
        productApi.createOrder(p.id, quantity.getValue(), callback);
    }

    private Product currentProduct() {
        UiState<Product> s = product.getValue();
        return s != null ? s.data : null;
    }
}
