package com.example.ecom.core.api;

import com.example.ecom.core.data.ApiResponse;
import com.example.ecom.core.data.AuthDtos;
import com.example.ecom.core.data.AuthResponse;
import com.example.ecom.core.data.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/register")
    Call<ApiResponse<Void>> register(@Body AuthDtos.RegisterRequest request);

    @POST("auth/verify-otp")
    Call<ApiResponse<Void>> verifyOtp(@Body AuthDtos.VerifyOtpRequest request);

    @POST("auth/resend-otp")
    Call<ApiResponse<Void>> resendOtp(@Body AuthDtos.ResendOtpRequest request);

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body AuthDtos.LoginRequest request);

    @POST("auth/refresh")
    Call<ApiResponse<AuthResponse>> refreshToken(@Body AuthDtos.RefreshRequest request);

    @GET("auth/me")
    Call<ApiResponse<User>> getMe();

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Body AuthDtos.LogoutRequest request);
}
