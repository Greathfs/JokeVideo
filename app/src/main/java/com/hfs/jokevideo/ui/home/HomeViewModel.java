package com.hfs.jokevideo.ui.home;

import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.hfs.jokevideo.model.Feed;
import com.hfs.jokevideo.ui.base.AbsViewModel;
import com.hfs.libnetwork.ApiResponse;
import com.hfs.libnetwork.ApiService;
import com.hfs.libnetwork.JsonCallback;
import com.hfs.libnetwork.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean witchCache = true;

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    ItemKeyedDataSource<Integer, Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            loadData(0, callback);
            witchCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //向后加载分页数据的
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //能够向前加载数据的
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", 0)
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        if (witchCache) {
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback() {
                @Override
                public void onCacheSuccess(ApiResponse response) {
                }
            });
        }
        Request netRequest = null;
        try {
            netRequest = witchCache ? request.clone() : request;
            netRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;

            callback.onResult(data);
            if (key > 0) {
                //通过BoundaryPageData发送数据 告诉UI层 是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(data.size() > 0);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }
}