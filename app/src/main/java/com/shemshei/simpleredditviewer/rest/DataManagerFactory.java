package com.shemshei.simpleredditviewer.rest;

import android.content.Context;

/**
 * Created by romanshemshei on 6/5/17.
 */

public class DataManagerFactory {

    public static final int SOURCE_REDDIT = 1;

    public static IDataManager createDataManager(int source, Context context){
        switch (source){
            case SOURCE_REDDIT:
                return new DataManager(context);
            default:
                throw new IllegalArgumentException("Unknown source");
        }
    }
}
