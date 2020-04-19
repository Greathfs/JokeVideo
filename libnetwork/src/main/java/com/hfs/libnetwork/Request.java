package com.hfs.libnetwork;

import android.util.Log;

import androidx.annotation.IntDef;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 请求类
 */
public abstract class Request<T, R extends Request> {
    protected String mUrl;
    protected HashMap<String, String> mHeaders = new HashMap<>();
    protected HashMap<String, Object> mParams = new HashMap<>();

    /**
     * 仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
     */
    public static final int CACHE_ONLY = 1;
    /**
     * 先访问缓存，同时发起网络的请求，成功后缓存到本地
     */
    public static final int CACHE_FIRST = 2;
    /**
     * 仅仅只访问服务器，不存任何存储
     */
    public static final int NET_ONLY = 3;
    /**
     * 先访问网络，成功后缓存到本地
     */
    public static final int NET_CACHE = 4;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_CACHE, NET_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CacheStrategy {
    }

    private Type mType;
    private String cacheKey;
    private int mCacheStrategy = NET_ONLY;

    public Request(String url) {
        mUrl = url;
    }

    public R addHeader(String key, String value) {
        mHeaders.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }
        //int byte char short long double float boolean 和他们的包装类型，但是除了 String.class 所以要额外判断
        try {
            if (value.getClass() == String.class) {
                mParams.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class claz = (Class) field.get(null);
                if (claz.isPrimitive()) {
                    mParams.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class claz) {
        mType = claz;
        return (R) this;
    }


    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        return ApiService.OK_HTTP_CLIENT.newCall(request);
    }

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法,response 返回值 类型必须设置");
        }
        ApiResponse<T> result = null;
        try {
            Response response = getCall().execute();
            result = parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new ApiResponse<>();
            result.message = e.getMessage();
        }
        return result;
    }

    public void execute(final JsonCallback callback) {
        getCall().enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ApiResponse<T> apiResponse = new ApiResponse<>();
                apiResponse.message = e.getMessage();
                callback.onError(apiResponse);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ApiResponse<T> apiResponse = parseResponse(response,callback);
                if (!apiResponse.success) {
                    callback.onError(apiResponse);
                }else {
                    callback.onSuccess(apiResponse);
                }
            }
        });
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                }
                else {
                    Log.e("request", "parseResponse: 无法解析 ");
                }
            } else {
                message = content;
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            status = 0;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        return result;
    }
}
