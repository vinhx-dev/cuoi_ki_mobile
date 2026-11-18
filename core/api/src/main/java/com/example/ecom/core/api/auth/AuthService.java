package com.example.ecom.core.api.auth;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;
import com.example.ecom.core.data.User;
public interface AuthService {
 @POST("auth/register") Call<ApiResponse<Object>> register(@Body Map<String,String> body);
 @POST("auth/login") Call<ApiResponse<AuthResponse>> login(@Body Map<String,String> body);
 @POST("auth/verify-otp") Call<ApiResponse<Object>> verify(@Body Map<String,String> body);
 @POST("auth/resend-otp") Call<ApiResponse<Object>> resend(@Body Map<String,String> body);
 @POST("auth/refresh") Call<ApiResponse<AuthResponse>> refresh(@Body Map<String,String> body);
 @POST("auth/logout") Call<ApiResponse<Object>> logout(@Body Map<String,String> body);
 @GET("auth/me") Call<ApiResponse<User>> me();
}
