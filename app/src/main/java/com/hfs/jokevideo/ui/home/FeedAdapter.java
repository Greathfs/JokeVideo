package com.hfs.jokevideo.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hfs.jokevideo.databinding.LayoutFeedTypeImageBinding;
import com.hfs.jokevideo.databinding.LayoutFeedTypeVideoBinding;
import com.hfs.jokevideo.model.Feed;

public class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    protected String mCategory;

    protected FeedAdapter(Context context,String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        mLayoutInflater = LayoutInflater.from(context);
        mCategory = category;
    }

    @Override
    public int getItemViewType(int position) {
        Feed feed = getItem(position);
        return feed.itemType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;
        if (viewType == Feed.TYPE_IMAGE_TEXT) {
            binding = LayoutFeedTypeImageBinding.inflate(mLayoutInflater, parent, false);
        } else {
            binding = LayoutFeedTypeVideoBinding.inflate(mLayoutInflater, parent, false);
        }
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mBinding;
        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
            }
        }
    }
}
