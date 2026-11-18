package com.example.ecom.core.api.auth;
import com.example.ecom.core.data.TokenManager;
import com.example.ecom.core.data.TokenStore;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.*;
public final class TokenAuthenticator implements Authenticator {
 private final TokenStore tokens;
 private final AuthService refreshService;
 private final Runnable expired;
 public TokenAuthenticator(TokenStore tokens, AuthService service, Runnable expired) { this.tokens=tokens;refreshService=service;this.expired=expired; }
 @Override public Request authenticate(Route route, Response response) throws IOException {
  if(!AuthInterceptor.requiresAuth(response.request())) return null;
  if(response.priorResponse()!=null) {
   synchronized(TokenManager.LOCK) {
    if(("Bearer "+tokens.getAccessToken()).equals(response.request().header("Authorization"))) {tokens.clear();expired.run();}
   }
   return null;
  }
  synchronized(TokenManager.LOCK) {
   String access=tokens.getAccessToken(), refresh=tokens.getRefreshToken();
   if(access==null) return null;
   // Another failed request already rotated the token while this request waited.
   if(!("Bearer "+access).equals(response.request().header("Authorization"))) return response.request().newBuilder().header("Authorization","Bearer "+access).build();
   if(refresh==null) { tokens.clear();expired.run();return null; }
   Map<String,String> body=new HashMap<>();body.put("accessToken",access);body.put("refreshToken",refresh);
   retrofit2.Response<ApiResponse<AuthResponse>> result;
   try { result=refreshService.refresh(body).execute(); }
   catch(IOException e) { // The server may have consumed this single-use token before the connection failed.
    tokens.clear();expired.run();throw e;
   }
   ApiResponse<AuthResponse> envelope=result.body();
   if(result.code()==200 && envelope!=null && envelope.success && envelope.data!=null && envelope.data.accessToken!=null && envelope.data.refreshToken!=null) {
    AuthResponse a=envelope.data;tokens.save(a.accessToken,a.refreshToken,a.user!=null?a.user:tokens.getUser());
    return response.request().newBuilder().header("Authorization","Bearer "+a.accessToken).build();
   }
   // Never reuse a single-use refresh token after an ambiguous or invalid response.
   tokens.clear();expired.run();
   return null;
  }
 }
}
