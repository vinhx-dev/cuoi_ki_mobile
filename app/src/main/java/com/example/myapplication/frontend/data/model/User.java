package com.example.myapplication.frontend.data.model;

import org.json.JSONObject;

public class User {
    public final int id;
    public final String name;
    public final String phone;
    public final String address;

    public User(int id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public static User fromJson(JSONObject j) {
        return new User(j.optInt("id"), j.optString("name"), j.optString("phone"), j.optString("address"));
    }
}
