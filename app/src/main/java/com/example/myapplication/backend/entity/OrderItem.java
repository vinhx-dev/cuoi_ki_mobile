package com.example.myapplication.backend.entity;

public class OrderItem {
    private int productId;
    private String productName;
    private long price;
    private int quantity;

    public OrderItem(int productId, String productName, long price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public long getSubtotal() { return price * quantity; }
}
