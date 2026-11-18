package com.example.ecom.feature.auth.data;
import android.content.Context;
import com.example.ecom.core.api.auth.AuthRepository;
/** Feature facade; shared repository also serves Splash and Profile. */
public class AuthApi {
 public final AuthRepository repository;
 public AuthApi(Context context) { repository=new AuthRepository(context); }
}
