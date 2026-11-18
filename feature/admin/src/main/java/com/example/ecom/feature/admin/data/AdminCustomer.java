package com.example.ecom.feature.admin.data;
import org.json.*;
public class AdminCustomer {
    public final int id;
    public final String name,email,phone,address;
    public AdminCustomer(int id,String name,String email,String phone,String address) {
        this.id=id;
        this.name=name;
        this.email=email;
        this.phone=phone;
        this.address=address;
    }
    public JSONObject toJson() throws JSONException {
        return new JSONObject().put("id",id).put("name",name).put("email",email).put("phone",phone).put("address",address);
    }
    public static AdminCustomer fromJson(JSONObject j)throws JSONException {
        return new AdminCustomer(j.getInt("id"),j.getString("name"),j.getString("email"),j.getString("phone"),j.getString("address"));
    }
}
