package com.example.ecom.core.data;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("tokenType")
    public String tokenType;

    @SerializedName("accessTokenExpiresIn")
    public long accessTokenExpiresIn;

    @SerializedName("refreshTokenExpiresIn")
    public long refreshTokenExpiresIn;

    @SerializedName("user")
    public User user;
}
