package com.mooc.ppjoke.ui.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

/**
 * ViewModel基类
 */
public abstract class AbsViewModel<T> extends ViewModel {

    private DataSource mDataSource;
    private LiveData<PagedList<T>> mPageData;
    protected PagedList.Config mConfig;

    private MutableLiveData<Boolean> mBoundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {

        mConfig = new PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(12)
                // .setMaxSize(100)；
                // .setEnablePlaceholders(false)
                // .setPrefetchDistance()
                .build();

        mPageData = new LivePagedListBuilder(mFactory, mConfig)
                .setInitialLoadKey(0)
                .setBoundaryCallback(mCallback)
                .build();
    }


    public LiveData<PagedList<T>> getPageData() {
        return mPageData;
    }

    public DataSource getDataSource() {
        return mDataSource;
    }

    public LiveData<Boolean> getBoundaryPageData() {
        return mBoundaryPageData;
    }

    //PagedList数据被加载 情况的边界回调callback
    //但 不是每一次分页 都会回调这里，具体请看 ContiguousPagedList#mReceiver#onPageResult
    //deferBoundaryCallbacks
    PagedList.BoundaryCallback<T> mCallback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {
            //新提交的PagedList中没有数据
            mBoundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            mBoundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };

    DataSource.Factory mFactory = new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            if (mDataSource == null || mDataSource.isInvalid()) {
                mDataSource = createDataSource();
            }
            return mDataSource;
        }
    };

    /**
     * 创建DataSource
     */
    public abstract DataSource createDataSource();


    /**
     * 可以在这个方法里 做一些清理 的工作
     */
    @Override
    protected void onCleared() {

    }
}
