package com.example.myapplication.frontend.data.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Gọi server thật bằng HttpURLConnection (không cần thư viện ngoài).
 * Server thật cần trả về cùng format: { success, message, data }.
 * Có thể thay bằng Retrofit/OkHttp sau này mà không ảnh hưởng tầng trên.
 */
public class HttpTransport implements ApiTransport {

    @Override
    public RawResponse execute(String method, String path, Map<String, String> query,
                               JSONObject body, String token) throws Exception {
        URL url = new URL(ApiConfig.BASE_URL + path + buildQuery(query));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(ApiConfig.TIMEOUT_MS);
            conn.setReadTimeout(ApiConfig.TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);

            if (body != null && !"GET".equals(method)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            return new RawResponse(code, readAll(is));
        } finally {
            conn.disconnect();
        }
    }

    private static String buildQuery(Map<String, String> query) throws Exception {
        if (query == null || query.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, String> e : query.entrySet()) {
            if (e.getValue() == null) continue;
            if (sb.length() > 1) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), "UTF-8"))
              .append('=')
              .append(URLEncoder.encode(e.getValue(), "UTF-8"));
        }
        return sb.toString();
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
