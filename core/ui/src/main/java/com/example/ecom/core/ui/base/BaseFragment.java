package com.example.ecom.core.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.layout.PageLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * Fragment cơ sở cho mọi trang. Mỗi Fragment ≈ 1 PAGE COMPONENT trong React:
 *
 *   React:                                    Android:
 *     function HomePage() {                     public HomeFragment() { super(R.layout.fragment_home); }
 *       const { products } = useHome();         vm = viewModel(HomeViewModel.class);
 *       return <List items={products}/>         observe(vm.getProducts(), adapter::submit);
 *     }
 *
 * Tự bọc nội dung trang bằng layout đã chọn trong AppPages (NONE / MAIN / DETAIL / ADMIN).
 *
 * Cung cấp:
 *  - viewModel(): lấy "hook" của trang
 *  - observe(): render lại khi state đổi, tự bật/tắt loading, tự toast lỗi
 *  - apiCallback(): dùng cho hành động (đăng nhập, đặt hàng...), tự loading + toast lỗi
 *  - bindSearchBar(): dùng thanh tìm kiếm trên header của layout
 */
public abstract class BaseFragment extends Fragment {

    public interface OnSuccess<T> {
        void call(T data);
    }

    private PageLayout pageLayout;

    /** Các state đang loading, để mỗi state chỉ bật loading 1 lần. */
    private final Set<LiveData<?>> loadingSources = new HashSet<>();

    /** @param contentLayout file XML nội dung của trang (vd. R.layout.fragment_home). */
    protected BaseFragment(@LayoutRes int contentLayout) {
        super(contentLayout);
    }

    /** Bọc nội dung trang bằng layout: ≈ <XxxLayout><Page/></XxxLayout>. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        pageLayout = new PageLayout(this, inflater, container);
        View pageContent = super.onCreateView(inflater, pageLayout.content(), savedInstanceState);
        if (pageContent != null) pageLayout.content().addView(pageContent);
        return pageLayout.root();
    }

    @Override
    public void onDestroyView() {
        loadingSources.clear();
        super.onDestroyView();
    }

    // ---------- State (≈ hook) ----------

    /** Lấy ViewModel của trang. ≈ gọi custom hook useXxx(). */
    protected <VM extends ViewModel> VM viewModel(Class<VM> type) {
        return new ViewModelProvider(this).get(type);
    }

    /** Render lại mỗi khi state đổi. Chỉ gọi trong onViewCreated. */
    protected <T> void observe(LiveData<UiState<T>> state, OnSuccess<T> render) {
        state.observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;
            boolean changed = s.loading ? loadingSources.add(state) : loadingSources.remove(state);
            if (changed) showLoading(s.loading);
            String error = s.takeError();
            if (error != null) toast(error);
            if (s.data != null) render.call(s.data);
        });
    }

    // ---------- Hành động ----------

    /**
     * Callback cho một hành động: tự bật/tắt loading, tự toast lỗi, bỏ qua kết quả nếu trang đã đóng.
     * ≈ mutation.mutate(data, { onSuccess })
     */
    protected <T> ApiCallback<T> apiCallback(OnSuccess<T> onSuccess) {
        final PageLayout layout = pageLayout;
        if (layout != null) layout.setLoading(true);

        return new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                if (layout != null) layout.setLoading(false);
                if (isUiActive()) onSuccess.call(data);
            }

            @Override
            public void onError(String message, int statusCode) {
                if (layout != null) layout.setLoading(false);
                if (isUiActive()) toast(message);
            }
        };
    }

    // ---------- Layout ----------

    /** Dùng thanh tìm kiếm của header: hiện sẵn từ khoá hiện tại và nhận từ khoá mới. Gọi trong onViewCreated. */
    protected void bindSearchBar(String currentQuery, PageLayout.SearchListener listener) {
        if (pageLayout != null) pageLayout.bindSearch(currentQuery, listener);
    }

    protected void setSearchHint(CharSequence hint) {
        if (pageLayout != null) pageLayout.setSearchHint(hint);
    }

    protected void showLoading(boolean loading) {
        if (pageLayout != null) pageLayout.setLoading(loading);
    }

    // ---------- Tiện ích ----------

    protected void toast(String message) {
        if (getContext() != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /** Trang còn đang hiển thị (an toàn để cập nhật UI). */
    protected boolean isUiActive() {
        return isAdded() && getView() != null;
    }
}
