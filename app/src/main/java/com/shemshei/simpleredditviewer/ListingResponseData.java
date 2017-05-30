package com.shemshei.simpleredditviewer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by romanshemshei on 5/30/17.
 */

public class ListingResponseData {

    @SerializedName("modhash")
    @Expose
    private String modhash;

    @SerializedName("children")
    @Expose
    private List<Child> children;

    @SerializedName("after")
    @Expose
    private String after;

    @SerializedName("before")
    @Expose
    private String before;

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
}
