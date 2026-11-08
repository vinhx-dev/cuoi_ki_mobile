package com.example.myapplication.backend.database;

import com.example.myapplication.backend.entity.Order;
import com.example.myapplication.backend.entity.OrderItem;
import com.example.myapplication.backend.entity.Product;
import com.example.myapplication.backend.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * "Database" in-memory. Dữ liệu sẽ reset mỗi lần mở lại app.
 * Chỉ tầng Repository được phép truy cập class này.
 */
public final class FakeDatabase {

    private static final FakeDatabase INSTANCE = new FakeDatabase();

    public final List<User> users = Collections.synchronizedList(new ArrayList<>());
    public final List<Product> products = Collections.synchronizedList(new ArrayList<>());
    public final List<Order> orders = Collections.synchronizedList(new ArrayList<>());

    public final AtomicInteger userSeq = new AtomicInteger(0);
    public final AtomicInteger productSeq = new AtomicInteger(0);
    public final AtomicInteger orderSeq = new AtomicInteger(0);

    private FakeDatabase() {
        seed();
    }

    public static FakeDatabase getInstance() {
        return INSTANCE;
    }

    private void seed() {
        // Tài khoản demo: 0123456789 / 123456
        users.add(new User(userSeq.incrementAndGet(), "Nguyễn Văn A", "0123456789", "123456", "123 Lê Lợi, Q1, TP.HCM"));
        users.add(new User(userSeq.incrementAndGet(), "Trần Thị B", "0987654321", "123456", "45 Trần Phú, Hà Nội"));

        products.add(new Product(productSeq.incrementAndGet(), "Áo thun basic", "Áo thun cotton 100%, form rộng", 150_000, 50, ""));
        products.add(new Product(productSeq.incrementAndGet(), "Quần jean slim", "Jean co giãn, màu xanh đậm", 420_000, 30, ""));
        products.add(new Product(productSeq.incrementAndGet(), "Giày sneaker", "Sneaker trắng đế cao su", 890_000, 20, ""));
        products.add(new Product(productSeq.incrementAndGet(), "Balo laptop", "Balo chống nước, ngăn laptop 15.6\"", 550_000, 15, ""));
        products.add(new Product(productSeq.incrementAndGet(), "Tai nghe bluetooth", "Pin 30h, chống ồn chủ động", 1_290_000, 10, ""));
        products.add(new Product(productSeq.incrementAndGet(), "Bình giữ nhiệt", "Inox 304, giữ nhiệt 12h", 199_000, 100, ""));

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(1, "Áo thun basic", 150_000, 2));
        items.add(new OrderItem(3, "Giày sneaker", 890_000, 1));
        orders.add(new Order(orderSeq.incrementAndGet(), 1, items, "123 Lê Lợi, Q1, TP.HCM",
                Order.STATUS_COMPLETED, System.currentTimeMillis() - 3 * 24 * 3600_000L));
    }
}
