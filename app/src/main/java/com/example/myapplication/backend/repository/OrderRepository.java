package com.example.myapplication.backend.repository;

import com.example.myapplication.backend.database.FakeDatabase;
import com.example.myapplication.backend.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final FakeDatabase db = FakeDatabase.getInstance();

    public List<Order> findByUserId(int userId) {
        List<Order> result = new ArrayList<>();
        synchronized (db.orders) {
            // Đơn mới nhất lên đầu
            for (int i = db.orders.size() - 1; i >= 0; i--) {
                Order o = db.orders.get(i);
                if (o.getUserId() == userId) result.add(o);
            }
        }
        return result;
    }

    public Order findById(int id) {
        synchronized (db.orders) {
            for (Order o : db.orders) if (o.getId() == id) return o;
        }
        return null;
    }

    public Order save(Order order) {
        order.setId(db.orderSeq.incrementAndGet());
        db.orders.add(order);
        return order;
    }
}
