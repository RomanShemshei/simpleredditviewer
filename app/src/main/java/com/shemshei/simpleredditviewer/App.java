package com.shemshei.simpleredditviewer;

import android.app.Application;

import com.shemshei.simpleredditviewer.rest.DataManagerFactory;
import com.shemshei.simpleredditviewer.rest.IDataManager;

/**
 * Created by roma on 29.05.17.
 */

public class App extends Application {

    private IDataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mDataManager = DataManagerFactory.createDataManager(DataManagerFactory.SOURCE_REDDIT, getApplicationContext());
    }

    public IDataManager getDataManager(){
        return mDataManager;
    }
}
