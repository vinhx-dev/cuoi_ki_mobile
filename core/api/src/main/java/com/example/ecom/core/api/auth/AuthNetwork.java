package com.example.ecom.core.api.auth;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.ecom.core.api.ApiConfig;
import com.example.ecom.core.data.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
public final class AuthNetwork {
 private static AuthNetwork instance;
 public final AuthService service;
 public final OkHttpClient client;
 public final TokenManager tokens;
 private volatile Runnable listener;
 public static synchronized AuthNetwork get(Context context) { if(instance==null) instance=new AuthNetwork(context.getApplicationContext());return instance; }
 public void setExpiredListener(Runnable listener) { this.listener=listener; }
 private Retrofit retrofit(OkHttpClient c) { return new Retrofit.Builder().baseUrl(ApiConfig.BASE_URL.replaceAll("/+$", "")+"/").client(c).addConverterFactory(GsonConverterFactory.create()).build(); }
 private AuthNetwork(Context context) {
  tokens=new TokenManager(context);
  // Refresh uses a separate client without an authenticator to avoid recursion/deadlock.
  OkHttpClient plain=new OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor(chain -> {
   okhttp3.Response response=chain.proceed(chain.request());
   if(chain.request().url().encodedPath().endsWith("/auth/refresh")) {
    try {
     com.google.gson.JsonObject json=com.google.gson.JsonParser.parseString(response.peekBody(65536).string()).getAsJsonObject();
     if(json.has("message")&&!json.get("message").isJsonNull()) {
      String message=json.get("message").getAsString();
      new Handler(Looper.getMainLooper()).post(()->android.widget.Toast.makeText(context,message,android.widget.Toast.LENGTH_SHORT).show());
     }
    } catch(Exception ignored) { }
   }
   return response;
  }).connectTimeout(15,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(30,TimeUnit.SECONDS).build();
  AuthService refresh=retrofit(plain).create(AuthService.class);
  HttpLoggingInterceptor logging=new HttpLoggingInterceptor();logging.setLevel(HttpLoggingInterceptor.Level.BASIC);logging.redactHeader("Authorization");
  client=plain.newBuilder().addInterceptor(new AuthInterceptor(tokens)).addInterceptor(logging).authenticator(new TokenAuthenticator(tokens,refresh,()->new Handler(Looper.getMainLooper()).post(()->{Runnable l=listener;if(l!=null)l.run();}))).build();
  service=retrofit(client).create(AuthService.class);
 }
}
