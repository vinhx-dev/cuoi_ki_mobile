package com.example.ecom.core.data.api;

/** Callback luôn được gọi trên Main thread. */
public interface ApiCallback<T> {
    void onSuccess(T data);

    void onError(String message, int statusCode);
}
