package com.example.ecom.core.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Giả lập Backend Server (Mock Data) cho phép chạy thử full tính năng app
 * khi chưa kết nối tới máy chủ thật.
 */
public class MockTransport implements ApiTransport {

    private static class MockProduct {
        int id;
        String name;
        String description;
        long price;
        int stock;
        String category;
        String imageUrl;

        MockProduct(int id, String name, String description, long price, int stock, String category, String imageUrl) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.category = category;
            this.imageUrl = imageUrl;
        }

        JSONObject toListJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", name);
            obj.put("price", price);
            obj.put("stock", stock);
            obj.put("category", category);
            obj.put("categoryName", category);
            obj.put("description", description);
            obj.put("imageUrl", imageUrl);
            return obj;
        }

        JSONObject toDetailJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", name);
            obj.put("description", description);
            obj.put("price", price);
            obj.put("stock", stock);
            obj.put("category", category);
            obj.put("categoryName", category);
            obj.put("imageUrl", imageUrl);
            return obj;
        }
    }

    private static class MockOrderItem {
        int productId;
        String productName;
        int quantity;
        long price;

        MockOrderItem(int productId, String productName, int quantity, long price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

        JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("productId", productId);
            obj.put("productName", productName);
            obj.put("quantity", quantity);
            obj.put("price", price);
            return obj;
        }
    }

    private static class MockOrder {
        int id;
        String status; // "PENDING", "DELIVERED", "CANCELLED"
        String address;
        long total;
        long createdAt;
        List<MockOrderItem> items;

        MockOrder(int id, String status, String address, long total, long createdAt, List<MockOrderItem> items) {
            this.id = id;
            this.status = status;
            this.address = address;
            this.total = total;
            this.createdAt = createdAt;
            this.items = items;
        }

        JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("status", status);
            obj.put("address", address);
            obj.put("total", total);
            obj.put("createdAt", createdAt);
            JSONArray arr = new JSONArray();
            for (MockOrderItem item : items) {
                arr.put(item.toJson());
            }
            obj.put("items", arr);
            return obj;
        }
    }

    private final Map<Integer, MockProduct> products = new HashMap<>();
    private final List<MockOrder> orders = new ArrayList<>();
    private final AtomicInteger nextOrderId = new AtomicInteger(103);
    private String currentUserPhone = "0987654321";
    private String currentUserName = "Nguyễn Văn A";

    public MockTransport() {
        initData();
    }

    private void initData() {
        addProduct(new MockProduct(-3, "Áo Thun Cotton",
                "Áo thun cotton sọc ngang phong cách thoáng mát, dễ mặc hằng ngày.",
                150000L, 15, "Áo", ""));
        addProduct(new MockProduct(-5, "Áo Sơ Mi Caro",
                "Áo sơ mi caro nam vải mềm mại, phong cách trẻ trung lịch lãm.",
                280000L, 20, "Áo", ""));
        addProduct(new MockProduct(-7, "Áo Khoác Nhẹ",
                "Áo khoác nhẹ thể thao phong cách hiện đại, giữ ấm tốt.",
                350000L, 8, "Áo", ""));
        addProduct(new MockProduct(-8, "Áo Polo Nam",
                "Áo polo nam lịch sự, chất liệu cao cấp thấm hút mồ hôi.",
                199000L, 25, "Áo", ""));
        addProduct(new MockProduct(-6, "Giày Thể Thao Nam",
                "Giày thể thao êm chân, đế cao su chống trượt tốt.",
                750000L, 12, "Giày", ""));
        addProduct(new MockProduct(-4, "Váy Nữ Thời Trang",
                "Váy nữ kiểu dáng trẻ trung thanh lịch.",
                320000L, 10, "Váy", ""));
        addProduct(new MockProduct(-9, "Áo Sơ Mi Trắng",
                "Áo sơ mi trắng nam nữ thanh lịch.",
                250000L, 18, "Áo", ""));
        addProduct(new MockProduct(-10, "Áo Hoodie Basic",
                "Áo hoodie phong cách trẻ trung.",
                450000L, 14, "Áo", ""));

        List<MockOrderItem> items1 = new ArrayList<>();
        items1.add(new MockOrderItem(-3, "Áo Thun Cotton", 1, 150000L));
        orders.add(new MockOrder(101, "PENDING", "123 Nguyễn Trãi, Thanh Xuân, Hà Nội", 150000L, System.currentTimeMillis() - 3600000L, items1));

        List<MockOrderItem> items2 = new ArrayList<>();
        items2.add(new MockOrderItem(-5, "Áo Sơ Mi Caro", 1, 280000L));
        orders.add(new MockOrder(100, "DELIVERED", "123 Nguyễn Trãi, Thanh Xuân, Hà Nội", 280000L, System.currentTimeMillis() - 86400000L, items2));
    }

    private void addProduct(MockProduct p) {
        products.put(p.id, p);
    }

    @Override
    public RawResponse execute(String method, String path, Map<String, String> query,
                               RequestBody body, String token) throws Exception {
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        // 1. POST /auth/login
        if ("POST".equals(method) && "/auth/login".equals(path)) {
            String requestStr = getBodyString(body);
            JSONObject req = requestStr.isEmpty() ? new JSONObject() : new JSONObject(requestStr);
            String phone = req.optString("phone", "0987654321");
            this.currentUserPhone = phone;
            this.currentUserName = "0987654321".equals(phone) ? "Nguyễn Văn A" : ("User " + phone);

            JSONObject userObj = new JSONObject();
            userObj.put("id", 1);
            userObj.put("name", currentUserName);
            userObj.put("phone", currentUserPhone);
            userObj.put("address", "123 Nguyễn Trãi, Thanh Xuân, Hà Nội");

            JSONObject dataObj = new JSONObject();
            dataObj.put("token", "mock_token_demo_12345");
            dataObj.put("user", userObj);

            return successResponse(dataObj, "Đăng nhập thành công");
        }

        // 2. POST /auth/register
        if ("POST".equals(method) && "/auth/register".equals(path)) {
            String requestStr = getBodyString(body);
            JSONObject req = requestStr.isEmpty() ? new JSONObject() : new JSONObject(requestStr);
            String name = req.optString("name", "Người dùng mới");
            String phone = req.optString("phone", "0987654321");
            this.currentUserName = name;
            this.currentUserPhone = phone;

            JSONObject userObj = new JSONObject();
            userObj.put("id", 1);
            userObj.put("name", currentUserName);
            userObj.put("phone", currentUserPhone);
            userObj.put("address", "");

            JSONObject dataObj = new JSONObject();
            dataObj.put("token", "mock_token_demo_12345");
            dataObj.put("user", userObj);

            return successResponse(dataObj, "Đăng ký thành công");
        }

        // 3. GET /users/me
        if ("GET".equals(method) && "/users/me".equals(path)) {
            JSONObject userObj = new JSONObject();
            userObj.put("id", 1);
            userObj.put("name", currentUserName);
            userObj.put("phone", currentUserPhone);
            userObj.put("address", "123 Nguyễn Trãi, Thanh Xuân, Hà Nội");

            return successResponse(userObj, "Thành công");
        }

        // 4. GET /products
        if ("GET".equals(method) && "/products".equals(path)) {
            String keyword = query != null ? query.get("keyword") : null;
            JSONArray arr = new JSONArray();
            for (MockProduct p : products.values()) {
                if (keyword == null || keyword.trim().isEmpty() ||
                        p.name.toLowerCase().contains(keyword.trim().toLowerCase())) {
                    arr.put(p.toListJson());
                }
            }
            return successResponse(arr, "Thành công");
        }

        // 5. GET /products/{id}
        if ("GET".equals(method) && path.startsWith("/products/")) {
            try {
                int id = Integer.parseInt(path.substring("/products/".length()));
                MockProduct p = products.get(id);
                if (p != null) {
                    return successResponse(p.toDetailJson(), "Thành công");
                }
            } catch (NumberFormatException ignored) {}
            return errorResponse(404, "Sản phẩm không tồn tại");
        }

        // 6. POST /orders
        if ("POST".equals(method) && "/orders".equals(path)) {
            String requestStr = getBodyString(body);
            JSONObject req = requestStr.isEmpty() ? new JSONObject() : new JSONObject(requestStr);
            JSONArray itemsArr = req.optJSONArray("items");
            String address = req.optString("address", "");
            if (address.isEmpty()) {
                address = "123 Nguyễn Trãi, Thanh Xuân, Hà Nội";
            }

            List<MockOrderItem> orderItems = new ArrayList<>();
            long total = 0;
            if (itemsArr != null) {
                for (int i = 0; i < itemsArr.length(); i++) {
                    JSONObject itemObj = itemsArr.getJSONObject(i);
                    int productId = itemObj.optInt("productId");
                    int quantity = itemObj.optInt("quantity", 1);
                    MockProduct p = products.get(productId);
                    if (p != null) {
                        orderItems.add(new MockOrderItem(p.id, p.name, quantity, p.price));
                        total += p.price * quantity;
                    }
                }
            }

            int newId = nextOrderId.getAndIncrement();
            MockOrder newOrder = new MockOrder(newId, "PENDING", address, total, System.currentTimeMillis(), orderItems);
            orders.add(0, newOrder);

            JSONObject resObj = new JSONObject();
            resObj.put("id", newId);
            return successResponse(resObj, "Đặt hàng thành công");
        }

        // 7. GET /orders
        if ("GET".equals(method) && "/orders".equals(path)) {
            JSONArray arr = new JSONArray();
            for (MockOrder o : orders) {
                arr.put(o.toJson());
            }
            return successResponse(arr, "Thành công");
        }

        // 8. GET /orders/{id}
        if ("GET".equals(method) && path.startsWith("/orders/") && !path.endsWith("/cancel")) {
            try {
                int id = Integer.parseInt(path.substring("/orders/".length()));
                for (MockOrder o : orders) {
                    if (o.id == id) {
                        return successResponse(o.toJson(), "Thành công");
                    }
                }
            } catch (NumberFormatException ignored) {}
            return errorResponse(404, "Không tìm thấy đơn hàng");
        }

        // 9. POST /orders/{id}/cancel
        if ("POST".equals(method) && path.startsWith("/orders/") && path.endsWith("/cancel")) {
            try {
                String idStr = path.substring("/orders/".length(), path.length() - "/cancel".length());
                int id = Integer.parseInt(idStr);
                for (MockOrder o : orders) {
                    if (o.id == id) {
                        o.status = "CANCELLED";
                        return successResponse(o.toJson(), "Huỷ đơn hàng thành công");
                    }
                }
            } catch (NumberFormatException ignored) {}
            return errorResponse(404, "Không tìm thấy đơn hàng");
        }

        return errorResponse(404, "Endpoint không hợp lệ: " + method + " " + path);
    }

    private String getBodyString(RequestBody body) {
        if (body == null) return "";
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            body.writeTo(os);
            return os.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private RawResponse successResponse(Object data, String message) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("message", message);
        res.put("data", data);
        return new RawResponse(200, res.toString());
    }

    private RawResponse errorResponse(int statusCode, String message) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("success", false);
        res.put("message", message);
        return new RawResponse(statusCode, res.toString());
    }
}
