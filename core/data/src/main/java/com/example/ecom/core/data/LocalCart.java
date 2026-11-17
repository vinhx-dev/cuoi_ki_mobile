package com.example.ecom.core.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Persistent cart scoped to the signed-in user; quantities are checked across all sizes. */
public final class LocalCart {

    public static class CartItem {
        public final int productId;
        public final String size;
        public final String key;
        public final ShopProduct product;
        public int quantity;
        public boolean checked;

        public CartItem(int productId, String size, String key, ShopProduct product, int quantity) {
            this.productId = productId;
            this.size = size;
            this.key = key;
            this.product = product;
            this.quantity = quantity;
            this.checked = false;
        }

        public long getTotalPrice() {
            return product != null ? product.price * quantity : 0;
        }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("customer_cart_" + new SessionManager(c).getUserName(), Context.MODE_PRIVATE);
    }

    public static synchronized int quantity(Context c, int id) {
        int count = 0;
        for (Map.Entry<String, ?> e : prefs(c).getAll().entrySet()) {
            if (e.getKey().startsWith(id + ":") && e.getValue() instanceof Integer) {
                count += (Integer) e.getValue();
            }
        }
        return count;
    }

    public static synchronized boolean add(Context c, ShopProduct p, String size, int qty) {
        if (qty < 1 || !Arrays.asList("S", "M", "L", "XL").contains(size) || quantity(c, p.id) + qty > p.stock) {
            return false;
        }
        SharedPreferences sp = prefs(c);
        String key = p.id + ":" + size;
        sp.edit().putInt(key, sp.getInt(key, 0) + qty).apply();
        return true;
    }

    public static synchronized List<CartItem> getItems(Context c) {
        List<CartItem> list = new ArrayList<>();
        Map<String, ?> all = prefs(c).getAll();

        if (all.isEmpty()) {
            populateSampleCart(c);
            all = prefs(c).getAll();
        }

        for (Map.Entry<String, ?> e : all.entrySet()) {
            if (e.getValue() instanceof Integer) {
                String[] keyParts = e.getKey().split(":");
                if (keyParts.length >= 2) {
                    try {
                        int pId = Integer.parseInt(keyParts[0]);
                        String size = keyParts[1];
                        int qty = (Integer) e.getValue();
                        ShopProduct p = ShopCatalog.get(pId);
                        if (p != null) {
                            list.add(new CartItem(pId, size, e.getKey(), p, qty));
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return list;
    }

    private static void populateSampleCart(Context c) {
        ShopProduct p1 = ShopCatalog.get(-3); // Áo Thun Cotton
        ShopProduct p2 = ShopCatalog.get(-5); // Áo Sơ Mi Caro
        ShopProduct p3 = ShopCatalog.get(-7); // Áo Khoác Nhẹ
        ShopProduct p4 = ShopCatalog.get(-8); // Áo Polo Nam

        if (p1 != null) add(c, p1, "L", 1);
        if (p2 != null) add(c, p2, "M", 1);
        if (p3 != null) add(c, p3, "L", 1);
        if (p4 != null) add(c, p4, "XL", 1);
    }

    public static synchronized void updateQuantity(Context c, String key, int newQty) {
        if (newQty <= 0) {
            remove(c, key);
        } else {
            prefs(c).edit().putInt(key, newQty).apply();
        }
    }

    public static synchronized void remove(Context c, String key) {
        prefs(c).edit().remove(key).apply();
    }

    public static synchronized void clear(Context c) {
        prefs(c).edit().clear().apply();
    }

    public static List<String> lines(Context c) {
        List<String> r = new ArrayList<>();
        for (Map.Entry<String, ?> e : prefs(c).getAll().entrySet()) {
            String[] key = e.getKey().split(":");
            ShopProduct p = ShopCatalog.get(Integer.parseInt(key[0]));
            r.add((p == null ? "Sản phẩm #" + key[0] : p.name) + " · Size " + key[1] + " · SL " + e.getValue());
        }
        return r;
    }
}
