package com.hfs.libnetwork;

/**
 * 网络请求回调
 */
public abstract class JsonCallback<T> {
    public void onSuccess(ApiResponse<T> response) {

    }

    public void onError(ApiResponse<T> response) {

    }

    public void onCacheSuccess(ApiResponse<T> response) {

    }
}
