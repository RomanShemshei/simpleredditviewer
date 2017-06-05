package com.shemshei.simpleredditviewer.rest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

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

    @GET
    Call<String> authorize(@Url String url, @QueryMap Map<String, String> map);

    @GET
    Call<String> authorize(@Url String url,
                           @Query("client_id") String clientId,
                           @Query("response_type") String ResponseType,
                           @Query("state") String state,
                           @Query("redirect_uri") String redirectUri,
                           @Query("scope") String scope);

}
