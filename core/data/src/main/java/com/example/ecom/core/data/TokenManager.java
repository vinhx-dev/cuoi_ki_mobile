package com.example.ecom.core.data;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
/** All token operations share one lock, including logout and refresh rotation. */
public final class TokenManager implements TokenStore {
    public static final Object LOCK = new Object();
    private final SharedPreferences prefs;
    public TokenManager(Context context) { prefs = context.getApplicationContext().getSharedPreferences("auth_v1", Context.MODE_PRIVATE); }
    public String getAccessToken() { synchronized (LOCK) { return prefs.getString("accessToken", null); } }
    public String getRefreshToken() { synchronized (LOCK) { return prefs.getString("refreshToken", null); } }
    public void save(String access, String refresh, User user) {
        synchronized (LOCK) { prefs.edit().putString("accessToken", access).putString("refreshToken", refresh).putString("user", encode(user)).commit(); }
    }
    public void saveUser(User user) { synchronized (LOCK) { prefs.edit().putString("user", encode(user)).commit(); } }
    public User getUser() {
        synchronized (LOCK) {
            try {
                JSONObject j = new JSONObject(prefs.getString("user", "{}"));
                User u = new User(); u.id=j.optLong("id"); u.email=j.optString("email"); u.fullName=j.optString("fullName");
                u.phone=j.isNull("phone")?null:j.optString("phone"); u.role=j.optString("role"); u.status=j.optString("status"); u.createdAt=j.optString("createdAt"); return u;
            } catch (Exception e) { return null; }
        }
    }
    private String encode(User u) {
        if(u==null) return "{}";
        try { JSONObject j=new JSONObject(); j.put("id",u.id);j.put("email",u.email);j.put("fullName",u.fullName);j.put("phone",u.phone);j.put("role",u.role);j.put("status",u.status);j.put("createdAt",u.createdAt);return j.toString(); }
        catch(Exception e) { throw new IllegalArgumentException(e); }
    }
    public void clear() { synchronized (LOCK) { prefs.edit().clear().commit(); } }
}
