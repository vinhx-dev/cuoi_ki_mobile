package com.example.ecom.feature.splash;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.*;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.AuthRepository;
import com.example.ecom.core.data.*;
public class SplashFragment extends BaseFragment {
 public SplashFragment(){super(R.layout.fragment_splash);}
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle state){super.onViewCreated(v,state);
  if(new TokenManager(requireContext()).getAccessToken()==null){v.post(()->{if(isUiActive())AppRouter.toLogin(this);});return;}
  new AuthRepository(requireContext()).me(new ApiCallback<User>(){
   public void onSuccess(User u){if(isUiActive())AppRouter.afterLogin(SplashFragment.this,u.isAdmin());}
   public void onError(String m,int s){if(isUiActive())AppRouter.toLogin(SplashFragment.this);}
  });
 }
}
