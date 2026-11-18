package com.example.ecom.core.data.model.admin;
import org.json.*;
import java.util.*;
public class AdminProduct {
    public int id, stock;
    public String name, category, description, sizes, colors, imageKey;
    public long price;
    public final List<String> images = new ArrayList<>();
    public AdminProduct(int id,String name,String category,long price,int stock,String description,String sizes,String colors,String imageKey) {
        this.id=id;
        this.name=name;
        this.category=category;
        this.price=price;
        this.stock=stock;
        this.description=description;
        this.sizes=sizes;
        this.colors=colors;
        this.imageKey=imageKey;
    }
    public JSONObject toJson() throws JSONException {
        return new JSONObject().put("id",id).put("name",name).put("category",category).put("price",price).put("stock",stock).put("description",description).put("sizes",sizes).put("colors",colors).put("imageKey",imageKey).put("images",new JSONArray(images));
    }
    public static AdminProduct fromJson(JSONObject j) throws JSONException {
        AdminProduct p=new AdminProduct(j.getInt("id"),j.getString("name"),j.getString("category"),j.getLong("price"),j.getInt("stock"),j.optString("description"),j.optString("sizes"),j.optString("colors"),j.optString("imageKey","tee"));
        JSONArray a=j.optJSONArray("images");
        if(a!=null)for(int i=0;i<a.length();i++)p.images.add(a.getString(i));
        return p;
    }
}
