package com.shemshei.simpleredditviewer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import okhttp3.Credentials;

/**
 * Created by roma on 29.05.17.
 */

public class ObtainTokenBody {

    @SerializedName("grant_type")
    @Expose
    private String mGrantType;

    @SerializedName("device_id")
    @Expose
    private String mDeviceId;

    public ObtainTokenBody(String grantType, String deviceId) {
        this.mGrantType = grantType;
        this.mDeviceId = deviceId;
    }

    /**
     * @return The site
     */
    public String getGrantType() {
        return mGrantType;
    }

    /**
     * @return Site name
     */
    public String getDeviceId() {
        return mDeviceId;
    }
}
