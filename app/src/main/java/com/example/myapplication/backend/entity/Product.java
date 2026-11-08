package com.example.myapplication.backend.entity;

public class Product {
    private int id;
    private String name;
    private String description;
    private long price;
    private int stock;
    private String imageUrl;

    public Product(int id, String name, String description, long price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
}
