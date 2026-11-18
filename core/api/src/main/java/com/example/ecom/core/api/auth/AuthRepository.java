package com.example.ecom.core.api.auth;
import android.content.Context;
import android.widget.Toast;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.data.User;

import java.util.*;
import retrofit2.*;
public final class AuthRepository {
 private final Context context;
 private final AuthNetwork network;
 public AuthRepository(Context c) {context=c.getApplicationContext();network=AuthNetwork.get(c);}
 private Map<String,String> body(String... values) {Map<String,String> m=new HashMap<>();for(int i=0;i<values.length;i+=2)m.put(values[i],values[i+1]);return m;}
 private <T> void run(Call<ApiResponse<T>> call,int expected,ApiCallback<T> cb) {
  call.enqueue(new Callback<ApiResponse<T>>() {
   public void onResponse(Call<ApiResponse<T>> c,Response<ApiResponse<T>> r) {
    ApiResponse<T> e=r.body();
    String message=e!=null&&e.message!=null?e.message:"Phản hồi máy chủ không hợp lệ (HTTP "+r.code()+")";
    if(e==null && r.errorBody()!=null) try {
     com.google.gson.JsonObject error=com.google.gson.JsonParser.parseString(r.errorBody().string()).getAsJsonObject();
     if(error.has("message")&&!error.get("message").isJsonNull())message=error.get("message").getAsString();
    } catch(Exception ignored) {}
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    if(r.code()==expected && e!=null && e.success) cb.onSuccess(e.data);else cb.onError(message,r.code());
   }
   public void onFailure(Call<ApiResponse<T>> c,Throwable t) {String m="Không thể kết nối máy chủ. Vui lòng thử lại.";Toast.makeText(context,m,Toast.LENGTH_SHORT).show();cb.onError(m,-1);}
  });
 }
 public void register(String name,String email,String phone,String password,ApiCallback<Object> cb) {run(network.service.register(body("email",email,"password",password,"fullName",name,"phone",phone)),201,cb);}
 public void login(String email,String password,ApiCallback<AuthResponse> cb) {run(network.service.login(body("email",email,"password",password)),200,new ApiCallback<AuthResponse>() {
  public void onSuccess(AuthResponse a) {if(a==null||a.accessToken==null||a.refreshToken==null||a.user==null){cb.onError("Thiếu dữ liệu phiên đăng nhập",-1);return;}network.tokens.save(a.accessToken,a.refreshToken,a.user);cb.onSuccess(a);}
  public void onError(String m,int s) {cb.onError(m,s);}
 });}
 public void verify(String email,String otp,ApiCallback<Object> cb) {run(network.service.verify(body("email",email,"otp",otp)),200,cb);}
 public void resend(String email,ApiCallback<Object> cb) {run(network.service.resend(body("email",email)),200,cb);}
 public void me(ApiCallback<User> cb) {run(network.service.me(),200,new ApiCallback<User>() {
  public void onSuccess(User u) {if(u==null){cb.onError("Thiếu thông tin tài khoản",-1);return;}network.tokens.saveUser(u);cb.onSuccess(u);}
  public void onError(String m,int s) {if(s==401||s==404)network.tokens.clear();cb.onError(m,s);}
 });}
 public void logout(ApiCallback<Object> cb) {
  String refresh=network.tokens.getRefreshToken();
  run(network.service.logout(body("refreshToken",refresh)),200,new ApiCallback<Object>() {
   public void onSuccess(Object o) {network.tokens.clear();cb.onSuccess(o);}
   public void onError(String m,int s) {network.tokens.clear();cb.onError(m,s);}
  });
 }
}
