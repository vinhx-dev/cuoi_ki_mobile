package com.example.ecom.feature.admin.data;
import org.json.*;
public class AdminOrderLine {
    public final String name,category,size,color,imageKey;
    public final long price;
    public final int quantity;
    public AdminOrderLine(String name,String category,long price,int quantity,String size,String color,String imageKey) {
        this.name=name;
        this.category=category;
        this.price=price;
        this.quantity=quantity;
        this.size=size;
        this.color=color;
        this.imageKey=imageKey;
    }
    public JSONObject toJson()throws JSONException {
        return new JSONObject().put("name",name).put("category",category).put("price",price).put("quantity",quantity).put("size",size).put("color",color).put("imageKey",imageKey);
    }
    public static AdminOrderLine fromJson(JSONObject j)throws JSONException {
        return new AdminOrderLine(j.getString("name"),j.getString("category"),j.getLong("price"),j.getInt("quantity"),j.getString("size"),j.getString("color"),j.getString("imageKey"));
    }
}
