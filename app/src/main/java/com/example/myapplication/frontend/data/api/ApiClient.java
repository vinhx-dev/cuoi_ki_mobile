package com.example.myapplication.frontend.data.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.frontend.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ===== HÀM GỌI BACKEND DÙNG CHUNG =====
 * - Tự gắn token đăng nhập
 * - Chạy request ở background, trả kết quả về Main thread
 * - Bóc envelope { success, message, data } và parse "data" thành model qua Parser
 * - Gặp 401 -> báo cho UnauthorizedListener (MainActivity sẽ đưa về màn đăng nhập)
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";
    private static volatile ApiClient instance;

    /** Chuyển "data" (JSONObject / JSONArray / primitive) thành model. */
    public interface Parser<T> {
        T parse(Object data) throws JSONException;
    }

    public interface UnauthorizedListener {
        void onUnauthorized();
    }

    private final ApiTransport transport;
    private final SessionManager session;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private UnauthorizedListener unauthorizedListener;

    private ApiClient(Context context) {
        this.session = new SessionManager(context.getApplicationContext());
        this.transport = ApiConfig.USE_FAKE_BACKEND ? new FakeBackendTransport() : new HttpTransport();
    }

    public static ApiClient getInstance(Context context) {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) instance = new ApiClient(context);
            }
        }
        return instance;
    }

    public void setUnauthorizedListener(UnauthorizedListener listener) {
        this.unauthorizedListener = listener;
    }

    // ---------- Shortcut ----------

    public <T> void get(String path, Map<String, String> query, Parser<T> parser, ApiCallback<T> cb) {
        request("GET", path, query, null, parser, cb);
    }

    public <T> void post(String path, JSONObject body, Parser<T> parser, ApiCallback<T> cb) {
        request("POST", path, null, body, parser, cb);
    }

    public <T> void put(String path, JSONObject body, Parser<T> parser, ApiCallback<T> cb) {
        request("PUT", path, null, body, parser, cb);
    }

    public <T> void delete(String path, Parser<T> parser, ApiCallback<T> cb) {
        request("DELETE", path, null, null, parser, cb);
    }

    // ---------- Core ----------

    public <T> void request(String method, String path, Map<String, String> query, JSONObject body,
                            Parser<T> parser, ApiCallback<T> cb) {
        final String token = session.getToken();
        executor.execute(() -> {
            try {
                RawResponse raw = transport.execute(method, path, query, body, token);
                JSONObject json = new JSONObject(raw.body);
                boolean success = json.optBoolean("success", raw.statusCode < 400);
                String message = json.optString("message", "Có lỗi xảy ra");

                if (success && raw.statusCode < 400) {
                    Object data = json.isNull("data") ? null : json.get("data");
                    T result = parser != null ? parser.parse(data) : null;
                    mainHandler.post(() -> cb.onSuccess(result));
                } else {
                    if (raw.statusCode == 401 && token != null) {
                        session.clear();
                        mainHandler.post(() -> {
                            if (unauthorizedListener != null) unauthorizedListener.onUnauthorized();
                        });
                    }
                    mainHandler.post(() -> cb.onError(message, raw.statusCode));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Parse error: " + method + " " + path, e);
                mainHandler.post(() -> cb.onError("Dữ liệu trả về không hợp lệ", -1));
            } catch (Exception e) {
                Log.e(TAG, "Network error: " + method + " " + path, e);
                mainHandler.post(() -> cb.onError("Không thể kết nối tới máy chủ", -1));
            }
        });
    }
}
