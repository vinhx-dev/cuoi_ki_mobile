package com.example.ecom.feature.auth.register;
import android.app.Application;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.*;
public class RegisterViewModel extends BaseViewModel {
 private final AuthRepository api;
 public RegisterViewModel(Application app) {super(app);api=new AuthRepository(app);}
 public void register(String name,String email,String phone,String password,ApiCallback<Object> cb) {api.register(name,email,phone,password,cb);}
}
