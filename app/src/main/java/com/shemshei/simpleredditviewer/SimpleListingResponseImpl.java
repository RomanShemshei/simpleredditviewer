package com.shemshei.simpleredditviewer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by romanshemshei on 5/30/17.
 */

public class SimpleListingResponseImpl {

    @SerializedName("kind")
    @Expose
    private String kind;

    @SerializedName("data")
    @Expose
    private ListingResponseData data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setData(ListingResponseData data) {
        this.data = data;
    }

    public ListingResponseData getData() {
        return data;
    }
}
