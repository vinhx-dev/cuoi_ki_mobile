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
        ShopProduct p=new ShopProduct(j.optInt("id"),j.optString("name"),j.optLong("price"),j.optInt("stock"),category,j.optString("description"),j.optString("imageUrl"));
        ShopCatalog.cache(p);
        return p;
    }
    public void list(String query,ApiCallback<List<ShopProduct>> cb) {
        Map<String,String> params=new HashMap<>();
        if(!query.isEmpty())params.put("keyword",query);
        api.getRemote("/products",params,data-> {
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
        api.getRemote("/products/"+id,null,data->parse((JSONObject)data),cb);
    }
}
