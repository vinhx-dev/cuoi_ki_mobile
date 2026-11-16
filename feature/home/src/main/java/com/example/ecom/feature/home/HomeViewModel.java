package com.example.ecom.feature.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.data.SessionManager;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.ui.base.UiState;
import com.example.ecom.feature.home.data.Product;
import com.example.ecom.feature.home.data.ProductApi;

import java.util.List;

/** ≈ useHome(): danh sách sản phẩm + từ khoá tìm kiếm. */
public class HomeViewModel extends BaseViewModel {

    private final ProductApi productApi;
    private final SessionManager session;

    private final MutableLiveData<UiState<List<Product>>> products = new MutableLiveData<>();
    private String query = null;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        productApi = new ProductApi(app);
        session = new SessionManager(app);
    }

    public LiveData<UiState<List<Product>>> getProducts() {
        return products;
    }

    /** Từ khoá đang tìm; null nếu chưa tải lần nào. */
    public String getQuery() {
        return query;
    }

    public String getUserName() {
        return session.getUserName();
    }

    /** Tìm sản phẩm. Gọi lại với cùng từ khoá (vd. khi xoay màn hình) thì không gọi API lại. */
    public void search(String keyword) {
        if (keyword.equals(query)) return;
        query = keyword;
        fetch(products, cb -> productApi.getProducts(keyword, cb));
    }
}
