package com.shemshei.simpleredditviewer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by roma on 29.05.17.
 */

public class AuthorizationBody {

    @SerializedName("client_id")
    @Expose
    private String mClientId;

    @SerializedName("response_type")
    @Expose
    private String mResponseType;

    @SerializedName("state")
    @Expose
    private String mState;

    @SerializedName("redirect_uri")
    @Expose
    private String mRedirectUri;

    @SerializedName("scope")
    @Expose
    private String mScope;


    public String getClientId() {
        return mClientId;
    }

    public void setClientId(String mClientId) {
        this.mClientId = mClientId;
    }

    public String getResponseType() {
        return mResponseType;
    }

    public void setResponseType(String mResponseType) {
        this.mResponseType = mResponseType;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
    }

    public String getRedirectUri() {
        return mRedirectUri;
    }

    public void setRedirectUri(String mRedirectUri) {
        this.mRedirectUri = mRedirectUri;
    }

    public String getScope() {
        return mScope;
    }

    public void setScope(String mScope) {
        this.mScope = mScope;
    }
}
