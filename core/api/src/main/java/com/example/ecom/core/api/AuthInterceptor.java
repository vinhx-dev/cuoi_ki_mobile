package com.example.ecom.core.api;

import android.content.Context;

import com.example.ecom.core.data.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        builder.header("Content-Type", "application/json");
        builder.header("Accept", "application/json");

        String path = original.url().encodedPath();

        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getAccessToken();

        if (token != null && !token.isEmpty() && !isPublicEndpoint(path)) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }

    private boolean isPublicEndpoint(String path) {
        return path.endsWith("auth/login") ||
               path.endsWith("auth/register") ||
               path.endsWith("auth/verify-otp") ||
               path.endsWith("auth/resend-otp");
    }
}
