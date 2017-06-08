package com.shemshei.simpleredditviewer.rest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by roma on 29.05.17.
 */

interface RedditAPI {

    @FormUrlEncoded
    @POST("https://www.reddit.com/api/v1/access_token")
    Call<TokenResponse> obtainToken(@Field("grant_type") String grantType, @Field("device_id") String deviceId);

    @GET("/top")
    Call<ListingResponse> getTopList(@Query("limit") int limit);

    @GET("/top")
    Call<ListingResponse> getTopList(@Query("after") String after, @Query("limit") int limit);

}
