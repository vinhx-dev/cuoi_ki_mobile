package com.example.ecom.feature.admin.data;
import org.json.*;
import java.math.BigDecimal;
import java.util.*;

public final class AdminOrder {
    public long id, customerId, createdAt;
    public String status, userName, userEmail, userPhone, userAddress;
    private BigDecimal total;
    public final List<AdminOrderLine> items = new ArrayList<>();
    public BigDecimal total() { return total; }
    public boolean canCancel() { return "PENDING".equals(status) || "SHIPPING".equals(status); }
    public String nextStatus() {
        if ("PENDING".equals(status)) return "SHIPPING";
        if ("SHIPPING".equals(status)) return "DELIVERED";
        return null;
    }
    public static AdminOrder fromJson(JSONObject j) throws JSONException {
        AdminOrder o = new AdminOrder();
        o.id = j.getLong("id"); o.customerId = j.getLong("userId");
        o.createdAt = AdminJson.date(j, "createdAt"); o.status = j.getString("status");
        o.userName = AdminJson.text(j, "userFullName"); o.userEmail = AdminJson.text(j, "userEmail");
        o.userPhone = AdminJson.text(j, "userPhone"); o.userAddress = AdminJson.text(j, "userAddress");
        o.total = new BigDecimal(j.get("totalPrice").toString());
        AdminOrderLine line = new AdminOrderLine();
        line.name = j.getString("name"); line.image = AdminJson.text(j, "image");
        line.size = AdminJson.text(j, "size"); line.price = new BigDecimal(j.get("unitPrice").toString());
        line.quantity = j.getInt("quantity"); o.items.add(line);
        return o;
    }
}
