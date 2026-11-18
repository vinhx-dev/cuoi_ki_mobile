package com.example.ecom.core.api;

import android.content.Context;
import com.example.ecom.core.api.auth.AuthNetwork;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Compatibility facade: both API interfaces share the same session and refresh lock. */
public final class RetrofitClient {
    private static volatile RetrofitClient instance;
    private final AuthApiService authApiService;
    private final AuthApiService rawAuthApiService;

    private RetrofitClient(Context context) {
        OkHttpClient client = AuthNetwork.get(context).client;
        authApiService = create(client);
        rawAuthApiService = create(client.newBuilder().authenticator(okhttp3.Authenticator.NONE).build());
    }

    private AuthApiService create(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL.replaceAll("/+$", "") + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(AuthApiService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) instance = new RetrofitClient(context.getApplicationContext());
        return instance;
    }

    public static AuthApiService getAuthApiService(Context context) { return getInstance(context).authApiService; }
    public static AuthApiService getRawAuthApiService(Context context) { return getInstance(context).rawAuthApiService; }
}
