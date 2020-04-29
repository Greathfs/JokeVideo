package com.hfs.jokevideo.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

/**
 * 列表Fragment,下拉刷新/上拉加载
 */
public abstract class AbsListFragment<T> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    private LayoutRefreshViewBinding mBinding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;

    protected PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;

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


    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            mAdapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size()>0);
    }

    public abstract PagedListAdapter<T, RecyclerView.ViewHolder> getAdapter();
}
