package com.example.ecom.core.data;

import com.google.gson.annotations.SerializedName;

public final class AuthDtos {
    private AuthDtos() {}

    public static class LoginRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        @SerializedName("fullName")
        public String fullName;

        @SerializedName("phone")
        public String phone;

        public RegisterRequest(String email, String password, String fullName, String phone) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
        }
    }

    public static class VerifyOtpRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("otp")
        public String otp;

        public VerifyOtpRequest(String email, String otp) {
            this.email = email;
            this.otp = otp;
        }
    }

    public static class ResendOtpRequest {
        @SerializedName("email")
        public String email;

        public ResendOtpRequest(String email) {
            this.email = email;
        }
    }

    public static class RefreshRequest {
        @SerializedName("refreshToken")
        public String refreshToken;

        @SerializedName("accessToken")
        public String accessToken;

        public RefreshRequest(String refreshToken, String accessToken) {
            this.refreshToken = refreshToken;
            this.accessToken = accessToken;
        }
    }

    public static class LogoutRequest {
        @SerializedName("refreshToken")
        public String refreshToken;

        public LogoutRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
