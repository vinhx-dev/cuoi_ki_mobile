package com.example.ecom.feature.auth.otp;
import android.os.*;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.auth.R;
public class OtpFragment extends BaseFragment {
 private OtpViewModel vm;
 private String email;
 private Button verify,resend;
 private EditText code;
 private CountDownTimer timer;
 public OtpFragment(){super(R.layout.fragment_otp);}
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle state){
  super.onViewCreated(v,state);vm=viewModel(OtpViewModel.class);email=requireArguments().getString("email","");
  if(email.isEmpty()){AppRouter.toLogin(this);return;}
  verify=v.findViewById(R.id.btnVerifyOtp);resend=v.findViewById(R.id.btnResendOtp);code=v.findViewById(R.id.inputOtp);
  ((TextView)v.findViewById(R.id.textOtpEmail)).setText("Nhập mã OTP 6 chữ số đã gửi đến\n"+email);
  v.findViewById(R.id.btnOtpBack).setOnClickListener(w->AppRouter.back(this));
  if(state!=null)vm.resendUntil=state.getLong("resendUntil",vm.resendUntil);
  else if(vm.resendUntil==0&&"true".equals(requireArguments().getString("sent")))vm.resendUntil=SystemClock.elapsedRealtime()+60000;
  verify.setOnClickListener(w->{String otp=code.getText().toString();if(!otp.matches("[0-9]{6}")){toast("Vui lòng nhập đúng 6 chữ số OTP");return;}if(vm.busy)return;vm.setBusy(true);verify.setEnabled(false);vm.verify(email,otp,new ApiCallback<Object>(){
   public void onSuccess(Object o){vm.setBusy(false);if(isUiActive())AppRouter.toLogin(OtpFragment.this);}
   public void onError(String m,int s){vm.setBusy(false);if(isUiActive()){verify.setEnabled(true);if(s==400){resend.setTypeface(null,android.graphics.Typeface.BOLD);resend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFD6B8));}}}
  });});
  resend.setOnClickListener(w->{if(SystemClock.elapsedRealtime()<vm.resendUntil||vm.busy)return;vm.resend(email,new ApiCallback<Object>(){public void onSuccess(Object o){} public void onError(String m,int s){}});countdown();});
  vm.loading.observe(getViewLifecycleOwner(),busy->verify.setEnabled(!Boolean.TRUE.equals(busy)));
  countdown();
 }
 private void countdown(){
  if(timer!=null)timer.cancel();long left=vm.resendUntil-SystemClock.elapsedRealtime();
  verify.setEnabled(!vm.busy);
  if(left<=0){resend.setEnabled(true);resend.setText("Gửi lại mã OTP");return;}
  resend.setEnabled(false);
  timer=new CountDownTimer(left,1000){public void onTick(long ms){resend.setText("Gửi lại mã OTP ("+((ms+999)/1000)+"s)");}public void onFinish(){if(isUiActive()){resend.setEnabled(true);resend.setText("Gửi lại mã OTP");}}}.start();
 }
 @Override public void onSaveInstanceState(@NonNull Bundle out){super.onSaveInstanceState(out);out.putLong("resendUntil",vm.resendUntil);}
 @Override public void onDestroyView(){if(timer!=null)timer.cancel();super.onDestroyView();}
}
