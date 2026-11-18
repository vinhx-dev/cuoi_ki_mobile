package com.example.ecom.feature.auth.otp;
import android.app.Application;
import android.os.SystemClock;
import com.example.ecom.core.ui.base.BaseViewModel;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.AuthRepository;
public class OtpViewModel extends BaseViewModel {
 private final AuthRepository api;
 public long resendUntil;
 public boolean busy;
 public final androidx.lifecycle.MutableLiveData<Boolean> loading=new androidx.lifecycle.MutableLiveData<>(false);
 public void setBusy(boolean value){busy=value;loading.setValue(value);}
 public OtpViewModel(Application app){super(app);api=new AuthRepository(app);}
 public void verify(String email,String otp,ApiCallback<Object> cb){api.verify(email,otp,cb);}
 public void resend(String email,ApiCallback<Object> cb){resendUntil=SystemClock.elapsedRealtime()+60000;api.resend(email,cb);}
}
