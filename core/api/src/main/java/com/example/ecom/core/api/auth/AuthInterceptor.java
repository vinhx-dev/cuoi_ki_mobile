package com.example.ecom.core.api.auth;
import com.example.ecom.core.data.TokenManager;
import com.example.ecom.core.data.TokenStore;
import java.io.IOException;
import okhttp3.*;
public final class AuthInterceptor implements Interceptor {
 private final TokenStore tokens;
 public AuthInterceptor(TokenStore tokens) { this.tokens=tokens; }
 public static boolean requiresAuth(Request r) {
  String p=r.url().encodedPath();
  return !(p.endsWith("/auth/login")||p.endsWith("/auth/register")||p.endsWith("/auth/verify-otp")||p.endsWith("/auth/resend-otp")||p.endsWith("/auth/refresh"));
 }
 @Override public Response intercept(Chain chain) throws IOException {
  Request.Builder b=chain.request().newBuilder().header("Accept","application/json");
  if(chain.request().body()==null || chain.request().body().contentType()==null || chain.request().body().contentType().subtype().equals("json")) b.header("Content-Type","application/json");
  String token=tokens.getAccessToken();
  if(requiresAuth(chain.request())&&token!=null) b.header("Authorization","Bearer "+token);
  return chain.proceed(b.build());
 }
}
