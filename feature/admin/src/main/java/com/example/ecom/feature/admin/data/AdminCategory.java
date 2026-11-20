package com.example.ecom.feature.admin.data;
import org.json.*;

public final class AdminCategory {
    public long id;
    public String name;
    public static AdminCategory fromJson(JSONObject j) throws JSONException {
        AdminCategory c = new AdminCategory();
        c.id = j.getLong("id"); c.name = j.getString("name");
        return c;
    }
    @Override public String toString() { return name; }
}
