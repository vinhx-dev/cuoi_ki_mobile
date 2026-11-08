package com.example.myapplication.backend.mapper;

import com.example.myapplication.backend.entity.Order;
import com.example.myapplication.backend.entity.OrderItem;
import com.example.myapplication.backend.entity.Product;
import com.example.myapplication.backend.entity.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Chuyển Entity -> JSON trả về client. Không bao giờ trả field nhạy cảm (password).
 */
public final class JsonMapper {

    private JsonMapper() {}

    public static JSONObject toJson(User u) throws JSONException {
        return new JSONObject()
                .put("id", u.getId())
                .put("name", u.getName())
                .put("phone", u.getPhone())
                .put("address", u.getAddress());
    }

    public static JSONObject toJson(Product p) throws JSONException {
        return new JSONObject()
                .put("id", p.getId())
                .put("name", p.getName())
                .put("description", p.getDescription())
                .put("price", p.getPrice())
                .put("stock", p.getStock())
                .put("imageUrl", p.getImageUrl());
    }

    public static JSONObject toJson(OrderItem i) throws JSONException {
        return new JSONObject()
                .put("productId", i.getProductId())
                .put("productName", i.getProductName())
                .put("price", i.getPrice())
                .put("quantity", i.getQuantity());
    }

    public static JSONObject toJson(Order o) throws JSONException {
        JSONArray items = new JSONArray();
        for (OrderItem i : o.getItems()) items.put(toJson(i));
        return new JSONObject()
                .put("id", o.getId())
                .put("status", o.getStatus())
                .put("address", o.getAddress())
                .put("total", o.getTotal())
                .put("createdAt", o.getCreatedAt())
                .put("items", items);
    }

    public static JSONArray productsToJson(List<Product> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Product p : list) arr.put(toJson(p));
        return arr;
    }

    public static JSONArray ordersToJson(List<Order> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Order o : list) arr.put(toJson(o));
        return arr;
    }
}
