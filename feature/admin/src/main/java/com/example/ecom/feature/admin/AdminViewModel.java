package com.example.ecom.feature.admin;

import android.app.Application;

import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.feature.admin.data.AdminCustomer;
import com.example.ecom.feature.admin.data.AdminLocalStore;
import com.example.ecom.feature.admin.data.AdminOrder;
import com.example.ecom.feature.admin.data.AdminProduct;

import java.util.List;

/** Owns admin data access; the current admin preview uses the local data source. */
public final class AdminViewModel extends BaseViewModel {
    private final AdminLocalStore store;

    public AdminViewModel(Application application) {
        super(application);
        store = AdminLocalStore.getInstance(application);
    }

    public List<String> categories() { return store.categories(); }
    public List<AdminProduct> getProducts() { return store.getProducts(); }
    public List<AdminCustomer> getCustomers() { return store.getCustomers(); }
    public List<AdminOrder> getOrders() { return store.getOrders(); }
    public AdminProduct product(int id) { return store.product(id); }
    public AdminCustomer customer(int id) { return store.customer(id); }
    public AdminOrder order(int id) { return store.order(id); }
    public int customerOrderCount(int id) { return store.customerOrderCount(id); }
    public int pendingCount() { return store.pendingCount(); }
    public int productCount() { return store.productCount(); }
    public int customerCount() { return store.customerCount(); }
    public long dashboardRevenue() { return store.dashboardRevenue(); }
    public void saveProduct(AdminProduct product) { store.saveProduct(product); }
    public void deleteProduct(int id) { store.deleteProduct(id); }
    public boolean updateOrderStatus(int id, String next) { return store.updateOrderStatus(id, next); }
    public String setting(String key, String fallback) { return store.setting(key, fallback); }
    public boolean notifications() { return store.notifications(); }
    public void saveSettings(String name, String email, boolean notifications) {
        store.saveSettings(name, email, notifications);
    }
}
