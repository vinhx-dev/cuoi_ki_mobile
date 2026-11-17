package com.example.ecom.core.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static volatile RetrofitClient instance;
    private final AuthApiService authApiService;
    private final AuthApiService rawAuthApiService;

    private RetrofitClient(Context context) {
        Context app = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(app))
                .authenticator(new TokenAuthenticator(app))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApiService = retrofit.create(AuthApiService.class);

        OkHttpClient rawClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Retrofit rawRetrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(rawClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        rawAuthApiService = rawRetrofit.create(AuthApiService.class);
    }

    public static RetrofitClient getInstance(Context context) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient(context);
                }
            }
        }
        return instance;
    }

    public static AuthApiService getAuthApiService(Context context) {
        return getInstance(context).authApiService;
    }

    public static AuthApiService getRawAuthApiService(Context context) {
        return getInstance(context).rawAuthApiService;
    }
}
