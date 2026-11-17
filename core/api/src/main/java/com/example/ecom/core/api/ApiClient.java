package com.example.ecom.core.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.ecom.core.data.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ===== HÀM GỌI API CHUNG CHO CẢ APP (≈ 1 instance axios dùng chung trong React) =====
 *
 *   api.get("/products", query, parser, cb)                    GET, tham số trên URL
 *   api.post("/orders", ApiClient.json("productId", 1), ...)    gửi JSON
 *   api.put(...) / api.delete(...)
 *   api.upload("/users/me/avatar", fields, files, parser, cb)  gửi ảnh / file (multipart)
 *
 * Tự lo cho mọi request:
 *  - Gắn token đăng nhập (header Authorization)
 *  - Chạy ở background, trả kết quả về Main thread
 *  - Bóc envelope { success, message, data } và parse "data" thành model qua Parser
 *  - Gặp 401 -> xoá phiên đăng nhập, báo UnauthorizedListener (MainActivity đưa về Login)
 *
 * Mỗi feature tự viết class API riêng (vd. feature/home/data/ProductApi) dùng các hàm ở đây.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";
    private static volatile ApiClient instance;

    /** Chuyển "data" (JSONObject / JSONArray / số / chuỗi) thành model. */
    public interface Parser<T> {
        T parse(Object data) throws JSONException;
    }

    public interface UnauthorizedListener {
        void onUnauthorized();
    }

    private final ApiTransport transport;
    private final ApiTransport mockTransport;
    private final SessionManager session;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile UnauthorizedListener unauthorizedListener;

    private ApiClient(Context context) {
        this.session = new SessionManager(context.getApplicationContext());
        this.transport = new HttpTransport(context);
        this.mockTransport = new MockTransport();
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

    public UnauthorizedListener getUnauthorizedListener() {
        return unauthorizedListener;
    }

    // ---------- JSON ----------

    public <T> void get(String path, Map<String, String> query, Parser<T> parser, ApiCallback<T> cb) {
        request("GET", path, query, null, parser, cb);
    }

    public <T> void post(String path, JSONObject body, Parser<T> parser, ApiCallback<T> cb) {
        request("POST", path, null, jsonOrNull(body), parser, cb);
    }

    public <T> void put(String path, JSONObject body, Parser<T> parser, ApiCallback<T> cb) {
        request("PUT", path, null, jsonOrNull(body), parser, cb);
    }

    public <T> void delete(String path, Parser<T> parser, ApiCallback<T> cb) {
        request("DELETE", path, null, null, parser, cb);
    }

    /** Strict network calls for customer catalogue and checkout: allow MockTransport fallback when server unreachable. */
    public <T> void getRemote(String path, Map<String, String> query, Parser<T> parser, ApiCallback<T> cb) {
        requestInternal("GET", path, query, null, parser, cb, true);
    }

    public <T> void postRemote(String path, JSONObject body, Parser<T> parser, ApiCallback<T> cb) {
        requestInternal("POST", path, null, jsonOrNull(body), parser, cb, true);
    }

    // ---------- Ảnh / file (multipart/form-data) ----------

    /**
     * Gửi form có kèm file/ảnh. ≈ axios.post(url, formData)
     * @param fields các trường chữ (có thể null)
     * @param files  các ảnh/file, tạo bằng FilePart.fromUri(...) (có thể null)
     */
    public <T> void upload(String path, Map<String, String> fields, List<FilePart> files,
                           Parser<T> parser, ApiCallback<T> cb) {
        request("POST", path, null, new MultipartBody(fields, files), parser, cb);
    }

    // ---------- Tiện ích ----------

    /** Tạo JSON nhanh: json("phone", phone, "password", pass) -> {"phone":..., "password":...} */
    public static JSONObject json(Object... keyValues) {
        JSONObject obj = new JSONObject();
        try {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                obj.put((String) keyValues[i], keyValues[i + 1]);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
        return obj;
    }

    private static RequestBody jsonOrNull(JSONObject body) {
        return body != null ? new JsonBody(body) : null;
    }

    // ---------- Lõi ----------

    public <T> void request(String method, String path, Map<String, String> query, RequestBody body,
                            Parser<T> parser, ApiCallback<T> cb) {
        requestInternal(method, path, query, body, parser, cb, true);
    }

    private <T> void requestInternal(String method, String path, Map<String, String> query, RequestBody body,
                                    Parser<T> parser, ApiCallback<T> cb, boolean allowMock) {
        final String token = session.getToken();
        executor.execute(() -> {
            try {
                RawResponse tempRaw;
                try {
                    tempRaw = transport.execute(method, path, query, body, token);
                } catch (Exception e) {
                    if (!allowMock) throw e;
                    Log.w(TAG, "Server connection failed (" + e.getMessage() + "), using MockTransport fallback");
                    tempRaw = mockTransport.execute(method, path, query, body, token);
                }
                final RawResponse raw = tempRaw;
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
