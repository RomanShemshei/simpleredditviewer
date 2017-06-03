package com.shemshei.simpleredditviewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by romanshemshei on 6/3/17.
 */

public class RedditContentAdapter extends RecyclerView.Adapter<RedditContentAdapter.ViewHolder> {

    private List<Child> mContent;
    private Picasso mPicasso;

    public RedditContentAdapter(Context context, List<Child> mContent) {
        this.mContent = mContent;
        this.mPicasso = new Picasso.Builder(context).build();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = mContent.get(position).getData();
        holder.mTitle.setText(data.getTitle());
        holder.mAuthor.setText(data.getAuthor());
        holder.mEntryDate.setText(getTimeAgo(data.getCreatedUtc()));
        holder.mNumberOfComments.setText(String.valueOf(data.getNumComments()));

        mPicasso.load(data.getThumbnail()).noPlaceholder().into(holder.mThumbnail, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }

    /*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
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

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public TextView mAuthor;
        public TextView mEntryDate;
        public TextView mNumberOfComments;
        public ImageView mThumbnail;

        public ViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.card_title);
            mAuthor = (TextView) v.findViewById(R.id.card_author);
            mEntryDate = (TextView) v.findViewById(R.id.card_entry_date);
            mNumberOfComments = (TextView) v.findViewById(R.id.card_number_of_comments);
            mThumbnail = (ImageView) v.findViewById(R.id.card_thumbnail);
        }
    }
}
