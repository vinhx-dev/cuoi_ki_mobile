package com.example.myapplication.frontend.ui.base;

import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.frontend.MainActivity;
import com.example.myapplication.frontend.data.api.ApiCallback;

/**
 * Fragment cơ sở cho mọi trang. Cung cấp:
 *  - showLoading / toast / setTitle
 *  - apiCallback(): tự bật/tắt loading, tự toast lỗi, bỏ qua kết quả nếu trang đã đóng
 */
public abstract class BaseFragment extends Fragment {

    public interface OnSuccess<T> {
        void call(T data);
    }

    protected void showLoading(boolean loading) {
        FragmentActivity act = getActivity();
        if (act instanceof MainActivity) ((MainActivity) act).setLoading(loading);
    }

    protected void setTitle(CharSequence title) {
        FragmentActivity act = getActivity();
        if (act instanceof MainActivity) ((MainActivity) act).setHeaderTitle(title);
    }

    protected void toast(String message) {
        if (getContext() != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /** Trang còn đang hiển thị (an toàn để cập nhật UI). */
    protected boolean isUiActive() {
        return isAdded() && getView() != null;
    }

    protected <T> ApiCallback<T> apiCallback(OnSuccess<T> onSuccess) {
        final FragmentActivity act = requireActivity();
        if (act instanceof MainActivity) ((MainActivity) act).setLoading(true);

        return new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                done();
                if (isUiActive()) onSuccess.call(data);
            }

            @Override
            public void onError(String message, int statusCode) {
                done();
                if (isUiActive()) toast(message);
            }

            private void done() {
                // Luôn tắt loading kể cả khi trang đã đóng
                if (act instanceof MainActivity && !act.isFinishing()) ((MainActivity) act).setLoading(false);
            }
        };
    }
}
