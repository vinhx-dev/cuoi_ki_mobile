package com.example.ecom.core.api;

import android.content.Context;
import com.example.ecom.core.api.auth.AuthNetwork;
import java.io.IOException;
import okhttp3.*;

/** Compatibility entry point; all callers use the tested shared refresh implementation. */
public final class TokenAuthenticator implements Authenticator {
    private final Authenticator delegate;
    public TokenAuthenticator(Context context) { delegate = AuthNetwork.get(context).client.authenticator(); }
    @Override public Request authenticate(Route route, Response response) throws IOException {
        return delegate.authenticate(route, response);
    }
}
