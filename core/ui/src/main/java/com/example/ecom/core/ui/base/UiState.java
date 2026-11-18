package com.example.ecom.core.ui.base;

/**
 * Trạng thái của 1 lần tải dữ liệu.
 * ≈ const { data, isLoading, error } = useQuery(...) trong React.
 *
 * Khi đang tải lại, data cũ vẫn được giữ để màn hình không bị trống.
 */
public final class UiState<T> {

    public final T data;
    public final boolean loading;
    private String error;

    private UiState(T data, boolean loading, String error) {
        this.data = data;
        this.loading = loading;
        this.error = error;
    }

    public static <T> UiState<T> loading(T oldData) {
        return new UiState<>(oldData, true, null);
    }

    public static <T> UiState<T> success(T data) {
        return new UiState<>(data, false, null);
    }

    public static <T> UiState<T> failure(String error, T oldData) {
        return new UiState<>(oldData, false, error);
    }

    /** Lấy lỗi đúng 1 lần, để xoay màn hình không hiện lại toast lỗi cũ. */
    public String takeError() {
        String e = error;
        error = null;
        return e;
    }
}
