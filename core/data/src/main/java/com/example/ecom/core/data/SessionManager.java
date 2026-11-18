package com.example.ecom.core.data;
import android.content.Context;
public class SessionManager {
    private final TokenManager tokens;
    private final LocalStorage legacy;
    public SessionManager(Context context) { tokens = new TokenManager(context); legacy=new LocalStorage(context); }
    public void saveSession(String token, String userName) { User u=new User();u.fullName=userName;tokens.save(token,null,u); }
    public String getToken() { return tokens.getAccessToken(); }
    public String getUserName() { User u=tokens.getUser();return u==null||u.fullName==null?"":u.fullName; }
    public boolean isLoggedIn() { return getToken()!=null; }
    public void clear() { tokens.clear();legacy.remove("token");legacy.remove("user_name"); }
}
