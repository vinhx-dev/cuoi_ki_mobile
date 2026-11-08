package com.example.myapplication.backend.entity;

import java.util.List;

public class Order {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SHIPPING = "SHIPPING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int id;
    private int userId;
    private List<OrderItem> items;
    private String address;
    private String status;
    private long createdAt;

    public Order(int id, int userId, List<OrderItem> items, String address, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }

    public long getTotal() {
        long total = 0;
        for (OrderItem item : items) total += item.getSubtotal();
        return total;
    }
}
