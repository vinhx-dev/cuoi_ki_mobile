package com.example.ecom.core.api;

import android.content.Context;
import com.example.ecom.core.data.TokenManager;
import java.io.IOException;
import okhttp3.*;

/** Compatibility entry point for existing feature code. */
public final class AuthInterceptor implements Interceptor {
    private final Interceptor delegate;
    public AuthInterceptor(Context context) {
        delegate = new com.example.ecom.core.api.auth.AuthInterceptor(new TokenManager(context));
    }
    @Override public Response intercept(Chain chain) throws IOException { return delegate.intercept(chain); }
}
