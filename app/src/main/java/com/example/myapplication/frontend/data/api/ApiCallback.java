package com.example.myapplication.frontend.data.api;

/** Callback luôn được gọi trên Main thread. */
public interface ApiCallback<T> {
    void onSuccess(T data);

    void onError(String message, int statusCode);
}
