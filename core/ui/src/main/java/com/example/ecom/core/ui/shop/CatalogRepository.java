package com.example.ecom.core.ui.shop;
import android.content.Context;
import com.example.ecom.core.api.*;
import com.example.ecom.core.data.*;
import org.json.*;
import java.util.*;
/** Real API remains the primary source. Samples are explicitly offered when it is unavailable. */
public final class CatalogRepository {
    private final ApiClient api;
    public CatalogRepository(Context c) {
        api=ApiClient.getInstance(c);
    }
    private ShopProduct parse(JSONObject j) {
        String category=j.optString("categoryName","");
        Object raw=j.opt("category");
        if(raw instanceof JSONObject)category=((JSONObject)raw).optString("name",category);
        else if(raw instanceof String)category=(String)raw;
        String image=j.isNull("image")?j.optString("imageUrl",""):j.optString("image","");
        ShopProduct p=new ShopProduct(j.optInt("id"),j.optString("name"),j.optLong("price"),j.optInt("quantity", j.optInt("stock")),category,j.optString("description"), image, sizes(j.optJSONArray("size")));
        ShopCatalog.cache(p);
        return p;
    }
    private List<String> sizes(JSONArray values) {
        List<String> result = new ArrayList<>();
        if (values != null) {
            for (int i = 0; i < values.length(); i++) result.add(values.optString(i));
        }
        return result;
    }
    public void list(String query,ApiCallback<List<ShopProduct>> cb) {
        list(query, null, cb);
    }

    public void categories(ApiCallback<Map<String, Long>> cb) {
        api.requestStrict("GET", "/categories", null, null, data -> {
            Map<String, Long> categories = new LinkedHashMap<>();
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);
                categories.put(category.getString("name"), category.getLong("id"));
            }
            return categories;
        }, cb);
    }

    public void list(String query, Long categoryId, ApiCallback<List<ShopProduct>> cb) {
        Map<String, String> params = new HashMap<>();
        if (!query.trim().isEmpty()) params.put("keyword", query.trim());
        if (categoryId != null) params.put("categoryId", categoryId.toString());
        api.requestStrict("GET", params.isEmpty() ? "/products/home" : "/products/search", params, null,data-> {
            JSONArray a=(JSONArray)data;
            List<ShopProduct> r=new ArrayList<>();
            for(int i=0;i<a.length();i++)r.add(parse(a.getJSONObject(i)));
            return r;
        }
        ,cb);
    }
    public void detail(int id,ApiCallback<ShopProduct> cb) {
        if(id<0) {
            ShopProduct p=ShopCatalog.get(id);
            if(p!=null)cb.onSuccess(p);
            else cb.onError("Không tìm thấy sản phẩm",404);
            return;
        }
        api.requestStrict("GET", "/products/"+id, null, null,data->parse((JSONObject)data),cb);
    }
}
