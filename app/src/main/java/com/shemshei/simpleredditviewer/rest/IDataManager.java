package com.shemshei.simpleredditviewer.rest;

/**
 * Created by romanshemshei on 6/5/17.
 */

public interface IDataManager {

    void requestTopListing(String startFrom, int count, final OnListingObtainedListener callback);

}
