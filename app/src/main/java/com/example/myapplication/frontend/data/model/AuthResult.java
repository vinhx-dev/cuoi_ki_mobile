package com.example.myapplication.frontend.data.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthResult {
    public final String token;
    public final User user;

    public AuthResult(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public static AuthResult fromJson(JSONObject j) throws JSONException {
        return new AuthResult(j.getString("token"), User.fromJson(j.getJSONObject("user")));
    }
}
