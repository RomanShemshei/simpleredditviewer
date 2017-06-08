package com.shemshei.simpleredditviewer.rest;

import com.shemshei.simpleredditviewer.pojo.Child;

import java.util.List;

/**
 * Created by romanshemshei on 6/5/17.
 */
public interface OnListingObtainedListener {

    void onListingObtained(List<Child> children);

    void onFailedToObtainList();

}
