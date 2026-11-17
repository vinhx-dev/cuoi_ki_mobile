package com.example.ecom.core.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ecom.core.data.ApiResponse;
import com.example.ecom.core.data.AuthDtos;
import com.example.ecom.core.data.AuthResponse;
import com.example.ecom.core.data.TokenManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private static final Object LOCK = new Object();
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TokenAuthenticator(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (responseCount(response) >= 3) {
            return null;
        }

        TokenManager tokenManager = TokenManager.getInstance(context);
        String currentAccessToken = tokenManager.getAccessToken();
        String currentRefreshToken = tokenManager.getRefreshToken();

        if (currentRefreshToken == null || currentRefreshToken.isEmpty()) {
            handleSessionExpired(tokenManager);
            return null;
        }

        synchronized (LOCK) {
            String latestAccessToken = tokenManager.getAccessToken();

            if (latestAccessToken != null && !latestAccessToken.equals(currentAccessToken)) {
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + latestAccessToken)
                        .build();
            }

            AuthApiService refreshApi = RetrofitClient.getRawAuthApiService(context);
            Call<ApiResponse<AuthResponse>> call = refreshApi.refreshToken(
                    new AuthDtos.RefreshRequest(currentRefreshToken, currentAccessToken));

            try {
                retrofit2.Response<ApiResponse<AuthResponse>> refreshResponse = call.execute();
                if (refreshResponse.isSuccessful() && refreshResponse.body() != null && refreshResponse.body().success) {
                    AuthResponse data = refreshResponse.body().data;
                    if (data != null && data.accessToken != null) {
                        tokenManager.saveTokens(data.accessToken, data.refreshToken != null ? data.refreshToken : currentRefreshToken);
                        if (data.user != null) {
                            tokenManager.saveUser(data.user);
                        }

                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + data.accessToken)
                                .build();
                    }
                }
            } catch (Exception ignored) {}

            handleSessionExpired(tokenManager);
            return null;
        }
    }

    private void handleSessionExpired(TokenManager tokenManager) {
        tokenManager.clear();
        mainHandler.post(() -> {
            ApiClient.UnauthorizedListener listener = ApiClient.getInstance(context).getUnauthorizedListener();
            if (listener != null) {
                listener.onUnauthorized();
            }
        });
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
