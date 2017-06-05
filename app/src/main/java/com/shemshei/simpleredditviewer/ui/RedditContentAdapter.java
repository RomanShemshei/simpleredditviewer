package com.shemshei.simpleredditviewer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shemshei.simpleredditviewer.R;
import com.shemshei.simpleredditviewer.pojo.Child;
import com.shemshei.simpleredditviewer.pojo.Data;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by romanshemshei on 6/3/17.
 */

public class RedditContentAdapter extends RecyclerView.Adapter<RedditContentAdapter.BaseViewHolder> {

    public static final int FOOTER_VIEW = 1;
    public static final int CARD_VIEW = 2;
    //
    private List<Child> mContent;
    private Picasso mPicasso;
    //
    private final View.OnClickListener mOnClickListener = new OnClickListenerImpl();
    private final OnItemClickedListener mOnItemClickedListener;

    public RedditContentAdapter(Context context, List<Child> mContent, OnItemClickedListener listener) {
        this.mContent = mContent;
        this.mOnItemClickedListener = listener;
        this.mPicasso = new Picasso.Builder(context).build();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        BaseViewHolder vh;
        if (viewType == FOOTER_VIEW) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_card_view, parent, false);
            vh = new FooterViewHolder(v, mOnClickListener);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.simple_card_view, parent, false);
            vh = new CardViewHolder(v, mOnClickListener);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        if (holder.getType() == CARD_VIEW) {
            CardViewHolder cardHolder = (CardViewHolder) holder;
            Data data = mContent.get(position).getData();
            cardHolder.mTitle.setText(data.getTitle());
            cardHolder.mAuthor.setText(data.getAuthor());
            cardHolder.mEntryDate.setText(getTimeAgo(data.getCreatedUtc()));
            cardHolder.mNumberOfComments.setText(String.valueOf(data.getNumComments()));

            mPicasso.cancelRequest(cardHolder.mThumbnail);
            mPicasso.load(data.getThumbnail()).into(cardHolder.mThumbnail, new Callback() {
                @Override
                public void onSuccess() {
                    // TODO handle
                }

                @Override
                public void onError() {
                    // TODO handle
                }
            });
        }

        // needed for handling onClick
        holder.itemView.setTag(String.valueOf(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mContent.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        // Add extra view to show the footer view
        return mContent.size() + 1;
    }

    public void updateContent(List<Child> updatedContent){
        int prevLast = mContent.size() - 1;
        mContent.addAll(updatedContent);
        notifyItemRangeInserted(prevLast, updatedContent.size());
    }

    /**
     * Copyright 2012 Google Inc.
     * <p>
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * <p>
     * http://www.apache.org/licenses/LICENSE-2.0
     * <p>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    // region OnClickListener ======================================================================
    private class OnClickListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int itemPosition = Integer.parseInt((String)view.getTag());
            if(itemPosition == mContent.size()){
                final int lastPos =  mContent.size() - 1;
                mOnItemClickedListener.onItemClicked(FOOTER_VIEW, mContent.get(lastPos));
            }else{
                mOnItemClickedListener.onItemClicked(CARD_VIEW, mContent.get(itemPosition));
            }
        }
    }
    // endregion ===================================================================================
    //
    // region ViewHolders ==========================================================================
    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(listener);
        }

        abstract int getType();

    }

    static class CardViewHolder extends BaseViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public TextView mAuthor;
        public TextView mEntryDate;
        public TextView mNumberOfComments;
        public ImageView mThumbnail;

        CardViewHolder(View v, View.OnClickListener listener) {
            super(v, listener);
            mTitle = (TextView) v.findViewById(R.id.card_title);
            mAuthor = (TextView) v.findViewById(R.id.card_author);
            mEntryDate = (TextView) v.findViewById(R.id.card_entry_date);
            mNumberOfComments = (TextView) v.findViewById(R.id.card_number_of_comments);
            mThumbnail = (ImageView) v.findViewById(R.id.card_thumbnail);
        }

        @Override
        public int getType() {
            return CARD_VIEW;
        }
    }

    static class FooterViewHolder extends BaseViewHolder {
        FooterViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView, listener);
        }

        @Override
        public int getType() {
            return FOOTER_VIEW;
        }
    }
    // endregion ===================================================================================
    //
    // region OnItemClickedListener ================================================================
    public interface OnItemClickedListener {
        void onItemClicked(int type, Child content);
    }
    // endregion ===================================================================================
}
