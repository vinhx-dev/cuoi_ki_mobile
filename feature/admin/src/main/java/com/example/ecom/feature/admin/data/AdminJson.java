package com.example.ecom.feature.admin.data;
import org.json.*;
import java.text.*;
import java.util.Locale;

final class AdminJson {
    private AdminJson() {}
    static String text(JSONObject j, String key) { return j.isNull(key) ? "" : j.optString(key, ""); }
    static long date(JSONObject j, String key) throws JSONException {
        String value = text(j, key);
        if (value.isEmpty()) throw new JSONException("Missing " + key);
        try { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(value).getTime(); }
        catch (ParseException e) { throw new JSONException("Invalid " + key); }
    }
}
