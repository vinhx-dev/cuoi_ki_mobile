package com.example.ecom.core.data.model.admin;
import org.json.*;
import java.util.*;
public class AdminOrder {
    public final int id,customerId;
    public final long createdAt;
    public String status;
    public final List<AdminOrderLine> items=new ArrayList<>();
    public AdminOrder(int id,int customerId,long createdAt,String status) {
        this.id=id;
        this.customerId=customerId;
        this.createdAt=createdAt;
        this.status=status;
    }
    public long total() {
        long n=0;
        for(AdminOrderLine line:items)n+=line.price*line.quantity;
        return n;
    }
    public boolean canCancel() {
        return "PENDING".equals(status)||"CONFIRMED".equals(status);
    }
    public String nextStatus() {
        switch(status) {
            case "PENDING":return "CONFIRMED";
            case "CONFIRMED":return "PROCESSING";
            case "PROCESSING":return "SHIPPING";
            case "SHIPPING":return "COMPLETED";
            default:return null;
        }
    }
    public JSONObject toJson()throws JSONException {
        JSONArray a=new JSONArray();
        for(AdminOrderLine line:items)a.put(line.toJson());
        return new JSONObject().put("id",id).put("customerId",customerId).put("createdAt",createdAt).put("status",status).put("items",a);
    }
    public static AdminOrder fromJson(JSONObject j)throws JSONException {
        AdminOrder o=new AdminOrder(j.getInt("id"),j.getInt("customerId"),j.getLong("createdAt"),j.getString("status"));
        JSONArray a=j.getJSONArray("items");
        for(int i=0;i<a.length();i++)o.items.add(AdminOrderLine.fromJson(a.getJSONObject(i)));
        return o;
    }
}
