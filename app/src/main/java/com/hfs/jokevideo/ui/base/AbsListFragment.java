package com.hfs.jokevideo.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hfs.jokevideo.R;
import com.hfs.jokevideo.databinding.LayoutRefreshViewBinding;
import com.hfs.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 列表Fragment,下拉刷新/上拉加载
 */
public abstract class AbsListFragment<T, M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    private LayoutRefreshViewBinding mBinding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;
    protected M mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutRefreshViewBinding.inflate(inflater, container, false);

        mRecyclerView = mBinding.recyclerView;
        mRefreshLayout = mBinding.refreshLayout;
        mEmptyView = mBinding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);

        //默认给列表中的Item 一个 10dp的ItemDecoration
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);
        genericViewModel();
        return mBinding.getRoot();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    public void finishRefresh(boolean hasData) {
        //当前列表数据
        PagedList<T> currentList = mAdapter.getCurrentList();
        //是否有数据
        hasData = hasData || currentList != null && currentList.size() > 0;
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter && state.isOpening) {
            mRefreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {
            mRefreshLayout.finishRefresh();
        }

        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void genericViewModel() {
        //利用 子类传递的 泛型参数实例化出absViewModel 对象。
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClaz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M) ViewModelProviders.of(this).get(modelClaz);

            //触发页面初始化数据加载的逻辑
            mViewModel.getPageData().observe(this, pagedList -> submitList(pagedList));

            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            mViewModel.getBoundaryPageData().observe(this, hasData -> finishRefresh(hasData));
        }
    }

    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            mAdapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size()>0);
    }

    public abstract PagedListAdapter getAdapter();
}
