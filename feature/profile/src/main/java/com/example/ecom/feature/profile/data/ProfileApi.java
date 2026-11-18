package com.example.ecom.feature.profile.data;
import android.content.Context;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.AuthRepository;
public class ProfileApi {
 private final AuthRepository api;
 public ProfileApi(Context context){api=new AuthRepository(context);}
 public void getMe(ApiCallback<User> cb){api.me(new ApiCallback<com.example.ecom.core.data.User>(){
  public void onSuccess(com.example.ecom.core.data.User u){User profile=new User((int)u.id,u.fullName,u.phone!=null?u.phone:"", "");profile.email=u.email!=null?u.email:"";cb.onSuccess(profile);}
  public void onError(String m,int s){cb.onError(m,s);}
 });}
 public void logout(ApiCallback<Object> cb){api.logout(cb);}
}
