package com.example.myapplication.backend.repository;

import com.example.myapplication.backend.database.FakeDatabase;
import com.example.myapplication.backend.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductRepository {

    private final FakeDatabase db = FakeDatabase.getInstance();

    public List<Product> findAll(String keyword) {
        List<Product> result = new ArrayList<>();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        synchronized (db.products) {
            for (Product p : db.products) {
                if (kw.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(kw)) result.add(p);
            }
        }
        return result;
    }

    public Product findById(int id) {
        synchronized (db.products) {
            for (Product p : db.products) if (p.getId() == id) return p;
        }
        return null;
    }
}
