package com.example.ecom.feature.admin.data;
import org.json.*;

public final class AdminCustomer {
    public long id;
    public String name, email, phone, address, role, status;
    public static AdminCustomer fromJson(JSONObject j) throws JSONException {
        AdminCustomer c = new AdminCustomer();
        c.id = j.getLong("id"); c.name = AdminJson.text(j, "fullName");
        c.email = AdminJson.text(j, "email"); c.phone = AdminJson.text(j, "phone");
        c.address = AdminJson.text(j, "address"); c.role = j.getString("role"); c.status = j.getString("status");
        return c;
    }
}
