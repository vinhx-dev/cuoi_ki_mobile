package com.example.ecom.feature.auth.login;
import android.app.Application;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.*;
public class LoginViewModel extends BaseViewModel {
 private final AuthRepository api;
 public LoginViewModel(Application app) {super(app);api=new AuthRepository(app);}
 public void login(String email,String password,ApiCallback<AuthResponse> cb) {api.login(email,password,cb);}
 public void resend(String email,ApiCallback<Object> cb) {api.resend(email,cb);}
}
