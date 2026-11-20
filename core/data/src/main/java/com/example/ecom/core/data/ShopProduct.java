package com.example.ecom.core.data;
/** A single product model shared by catalogue, detail and cart. Negative IDs are local samples. */
public final class ShopProduct {
    public final int id, stock;
    public final String name, description, category, imageUrl;
    public final long price;
    public final java.util.List<String> sizes;
    public ShopProduct(int id, String name, long price, int stock, String category, String description, String imageUrl) {
        this(id, name, price, stock, category, description, imageUrl,
                java.util.Arrays.asList("S", "M", "L", "XL"));
    }
    public ShopProduct(int id, String name, long price, int stock, String category,
                       String description, String imageUrl, java.util.List<String> sizes) {
        this.sizes = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(sizes));
        this.id=id;
        this.name=name;
        this.price=price;
        this.stock=stock;
        this.category=category;
        this.description=description;
        this.imageUrl=imageUrl;
    }
    public boolean isSample() {
        return id < 0;
    }
}
