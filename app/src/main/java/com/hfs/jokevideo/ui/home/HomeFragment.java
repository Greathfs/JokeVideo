package com.hfs.jokevideo.ui.home;

import android.os.Bundle;

import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hfs.jokevideo.model.Feed;
import com.hfs.jokevideo.ui.base.AbsListFragment;
import com.hfs.libnavannotation.FragmentDestination;

/**
 * 首页
 */
@FragmentDestination(pageUrl = "main/tabs/home" ,asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {
    private static final String TAG = "HomeFragment";

    private String mFeedType;

    @Override
    public PagedListAdapter getAdapter() {
        mFeedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), mFeedType);
    }
}