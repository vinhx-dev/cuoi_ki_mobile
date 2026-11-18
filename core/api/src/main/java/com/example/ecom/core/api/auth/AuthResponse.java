package com.example.ecom.core.api.auth;
import com.example.ecom.core.data.User;
public class AuthResponse {
 public String accessToken, refreshToken, tokenType;
 public long accessTokenExpiresIn, refreshTokenExpiresIn;
 public User user;
}
