package com.shemshei.simpleredditviewer.ui;

import android.content.Context;

import com.shemshei.simpleredditviewer.pojo.Child;

import java.util.ArrayList;
import java.util.List;

import static com.shemshei.simpleredditviewer.ui.AbstractContentAdapter.*;

/**
 * Created by romanshemshei on 6/5/17.
 */

public class ContentAdapterFactory {

    public static final int SOURCE_REDDIT = 1;

    public static AbstractContentAdapter createContentAdapter(int source, Context context, OnContentAdapterListener listener){
        return createContentAdapter(source, context, new ArrayList<Child>(0), listener);
    }

    public static AbstractContentAdapter createContentAdapter(int source, Context context, List<Child> content, OnContentAdapterListener listener){
        switch (source){
            case SOURCE_REDDIT:
                return new RedditContentAdapter(context, content, listener);
            default:
                throw new IllegalArgumentException("Unknown source");
        }
    }
}
