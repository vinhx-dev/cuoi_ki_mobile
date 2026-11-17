package com.example.ecom.feature.auth.login;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.util.Patterns;
import androidx.annotation.*;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.AuthResponse;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.auth.R;
import java.text.Normalizer;
import java.util.Locale;
public class LoginFragment extends BaseFragment {
 private EditText email,password;
 private Button submit;
 private LoginViewModel vm;
 public LoginFragment(){super(R.layout.fragment_login);}
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle state){
  super.onViewCreated(v,state);vm=viewModel(LoginViewModel.class);
  email=v.findViewById(R.id.inputAccount);password=v.findViewById(R.id.inputPassword);submit=v.findViewById(R.id.btnLogin);
  submit.setOnClickListener(w->login());
  v.findViewById(R.id.textRegister).setOnClickListener(w->AppRouter.toRegister(this));
  v.findViewById(R.id.textForgot).setOnClickListener(w->toast(getString(R.string.msg_forgot_coming_soon)));
  v.findViewById(R.id.layoutSocialIcons).setOnClickListener(w->toast(getString(R.string.msg_social_coming_soon)));
 }
 private boolean pending(String message){
  String m=Normalizer.normalize(message,Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
  return m.contains("chua kich hoat")||m.contains("chua xac thuc")||m.contains("not activated")||m.contains("not verified")||m.contains("inactive")||m.contains("pending")||m.contains("unverified");
 }
 private void login(){
  String e=email.getText().toString().trim(),p=password.getText().toString();
  if(!Patterns.EMAIL_ADDRESS.matcher(e).matches()||p.isEmpty()){toast("Vui lòng nhập email hợp lệ và mật khẩu");return;}
  submit.setEnabled(false);
  vm.login(e,p,new ApiCallback<AuthResponse>(){
   public void onSuccess(AuthResponse a){if(!isUiActive())return;submit.setEnabled(true);AppRouter.toHome(LoginFragment.this);}
   public void onError(String m,int s){
    if(!isUiActive())return;
    if(s==403&&pending(m)) vm.resend(e,new ApiCallback<Object>(){
     public void onSuccess(Object o){openOtp(e,true);}
     public void onError(String message,int code){openOtp(e,false);}
    });else submit.setEnabled(true);
   }
  });
 }
 private void openOtp(String e,boolean sent){if(isUiActive()){submit.setEnabled(true);AppRouter.toOtp(this,e,sent);}}
}
