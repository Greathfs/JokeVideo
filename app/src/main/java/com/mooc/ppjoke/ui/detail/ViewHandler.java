package com.mooc.ppjoke.ui.detail;

import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mooc.libcommon.utils.PixUtils;
import com.mooc.libcommon.view.EmptyView;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.LayoutFeedDetailBottomInateractionBinding;
import com.mooc.ppjoke.model.Comment;
import com.mooc.ppjoke.model.Feed;

public abstract class ViewHandler {

    protected FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInateractionBinding mInateractionBinding;
    protected FeedCommentAdapter mListAdapter;
    private final FeedDetailViewModel viewModel;

    public ViewHandler(FragmentActivity activity) {

        mActivity = activity;
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mInateractionBinding.setOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        mListAdapter = new FeedCommentAdapter(mActivity);
        mRecyclerView.setAdapter(mListAdapter);

        viewModel.setItemId(mFeed.itemId);
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                mListAdapter.submitList(comments);
                handleEmpty(comments.size() > 0);
            }
        });

    }

    private EmptyView mEmptyView;

    public void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                mListAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                mEmptyView.setLayoutParams(layoutParams);
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
            }
            mListAdapter.addHeaderView(mEmptyView);
        }
    }

}
