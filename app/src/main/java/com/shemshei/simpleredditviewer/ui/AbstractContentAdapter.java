package com.shemshei.simpleredditviewer.ui;

import android.support.v7.widget.RecyclerView;

import com.shemshei.simpleredditviewer.pojo.Child;

import java.util.List;

/**
 * Created by romanshemshei on 6/5/17.
 */

public abstract class AbstractContentAdapter extends RecyclerView.Adapter<RedditContentAdapter.BaseViewHolder> {

    public abstract void updateContent(List<Child> content, boolean forced);

    public interface OnContentAdapterListener {
        void onItemClicked(Child clickedItem);

        void onMoreContentNeeded(Child lastItem);
    }
}
