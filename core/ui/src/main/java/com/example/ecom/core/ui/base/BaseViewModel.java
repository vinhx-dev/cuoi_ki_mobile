package com.example.ecom.core.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.ecom.core.api.ApiCallback;

/**
 * ViewModel cơ sở. Mỗi trang có 1 ViewModel, đóng vai trò như CUSTOM HOOK trong React:
 *
 *   React:   function useHome() { const [products, setProducts] = useState(); ... return { products, search } }
 *   Android: class HomeViewModel extends BaseViewModel { LiveData products; void search(q) }
 *
 * Khác React: ViewModel sống sót khi xoay màn hình / đổi dark mode, nên không bị gọi API lại.
 * Fragment KHÔNG gọi API trực tiếp, chỉ gọi ViewModel. ViewModel gọi class API của feature (data/XxxApi).
 */
public abstract class BaseViewModel extends AndroidViewModel {

    /** Một lời gọi API: nhận callback và gọi class API của feature. */
    public interface Request<T> {
        void run(ApiCallback<T> callback);
    }

    protected BaseViewModel(@NonNull Application app) {
        super(app);
    }

    /**
     * Gọi API rồi đổ kết quả vào state. ≈ useQuery.
     * Ví dụ: fetch(products, cb -> productApi.getProducts(query, cb));
     */
    protected <T> void fetch(MutableLiveData<UiState<T>> state, Request<T> request) {
        UiState<T> current = state.getValue();
        T oldData = current != null ? current.data : null;
        state.setValue(UiState.loading(oldData));

        request.run(new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                state.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message, int statusCode) {
                state.setValue(UiState.failure(message, oldData));
            }
        });
    }

    /**
     * Bọc callback của một hành động (đặt hàng, huỷ đơn...) để khi thành công thì cập nhật luôn state.
     * ≈ useMutation({ onSuccess: (data) => setQueryData(data) })
     */
    protected <T> ApiCallback<T> updateOnSuccess(MutableLiveData<UiState<T>> state, ApiCallback<T> callback) {
        return new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                state.setValue(UiState.success(data));
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message, int statusCode) {
                callback.onError(message, statusCode);
            }
        };
    }
}
