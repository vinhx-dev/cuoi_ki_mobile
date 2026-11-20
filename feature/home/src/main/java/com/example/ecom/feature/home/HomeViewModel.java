package com.example.ecom.feature.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.data.ShopCatalog;
import com.example.ecom.core.data.ShopProduct;
import com.example.ecom.core.ui.shop.CatalogRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    public final MutableLiveData<List<ShopProduct>> products = new MutableLiveData<>();
    public final MutableLiveData<String> message = new MutableLiveData<>("Bộ sưu tập sản phẩm");
    public final MutableLiveData<List<String>> categories = new MutableLiveData<>();
    public String query = "", category = "";
    private boolean loaded;
    private int request;
    private Map<String, Long> categoryIds = Collections.emptyMap();

    public HomeViewModel(@NonNull Application app) {
        super(app);
    }

    public void load() {
        if (loaded) return;
        loaded = true;
        refresh();
    }

    public void search(String value) {
        if (query.equals(value)) return;
        query = value;
        refresh();
    }

    public void refresh() {
        int token = ++request;
        message.setValue("Đang tải sản phẩm…");
        CatalogRepository repository = new CatalogRepository(getApplication());
        if (categoryIds.isEmpty()) {
            repository.categories(new ApiCallback<Map<String, Long>>() {
                @Override public void onSuccess(Map<String, Long> result) {
                    if (token != request) return;
                    categoryIds = result;
                    categories.setValue(new ArrayList<>(result.keySet()));
                    loadProducts(repository, token);
                }

                @Override public void onError(String error, int code) {
                    if (token != request) return;
                    if (category.isEmpty()) loadProducts(repository, token);
                    else showOffline(error);
                }
            });
        } else {
            loadProducts(repository, token);
        }
    }

    private void loadProducts(CatalogRepository repository, int token) {
        Long categoryId = categoryIds.get(category);
        if (!category.isEmpty() && categoryId == null) {
            products.setValue(Collections.emptyList());
            message.setValue("");
            return;
        }
        repository.list(query, categoryId, new ApiCallback<List<ShopProduct>>() {
            @Override public void onSuccess(List<ShopProduct> result) {
                if (token != request) return;
                message.setValue("");
                products.setValue(result);
            }

            @Override public void onError(String error, int code) {
                if (token == request) showOffline(error);
            }
        });
    }

    private void showOffline(String error) {
        if (categoryIds.isEmpty()) categories.setValue(ShopCatalog.CATEGORIES);
        message.setValue(error + " · Sản phẩm mẫu");
        List<ShopProduct> samples = ShopCatalog.filter(ShopCatalog.samples(), category, query);
        if (category.isEmpty() && query.isEmpty()) {
            samples = samples.subList(0, Math.min(6, samples.size()));
        }
        products.setValue(samples);
    }
}
