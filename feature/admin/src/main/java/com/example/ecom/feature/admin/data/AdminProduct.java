package com.example.ecom.feature.admin.data;

import org.json.*;
import java.math.BigDecimal;
import java.util.*;

/** Full admin DTO, preserving arbitrary sizes and the JSON type of forAI. */
public final class AdminProduct {
    public long id, categoryId;
    public int stock, soldCount;
    public String name = "", category = "", description = "", image = "";
    public BigDecimal price = BigDecimal.ZERO;
    public Object forAI = JSONObject.NULL;
    public boolean includeImage = true;
    public final List<String> sizes = new ArrayList<>();

    public static AdminProduct fromJson(JSONObject j) throws JSONException {
        AdminProduct p = new AdminProduct();
        p.id = j.getLong("id"); p.name = j.getString("name");
        p.price = new BigDecimal(j.get("price").toString());
        p.stock = j.getInt("quantity"); p.soldCount = j.getInt("soldCount");
        p.categoryId = j.optLong("categoryId");
        p.category = AdminJson.text(j, "categoryName");
        p.description = AdminJson.text(j, "description"); p.image = AdminJson.text(j, "image");
        p.forAI = j.opt("forAI");
        JSONArray sizes = j.getJSONArray("size");
        for (int i = 0; i < sizes.length(); i++) p.sizes.add(sizes.getString(i));
        return p;
    }

    public JSONObject body() throws JSONException {
        JSONObject body = new JSONObject().put("name", name).put("price", price).put("quantity", stock)
                .put("description", description).put("size", new JSONArray(sizes))
                .put("categoryId", categoryId).put("soldCount", soldCount)
                .put("forAI", forAI == null ? JSONObject.NULL : forAI);
        if (includeImage) body.put("image", image);
        return body;
    }
}
