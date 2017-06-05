package com.shemshei.simpleredditviewer.rest;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by romanshemshei on 6/4/17.
 */

 class DataManager implements IDataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    private static final String APP_ID = "hHFxuoK11Br8zA";
    private static final String PREFS_DEVICE_ID = "DEVICE_ID";
    private static final String PREFS_TOKEN_OBTAINED_TIME = "TOKEN_TIME";
    private static final String PREFS_TOKEN_VALID_TIME = "TOKEN_VALID_TIME";

    private static final String PARAM_GRAND_TYPE = "https://oauth.reddit.com/grants/installed_client";

    private Retrofit mRetrofit;
    private RedditAPI mApi;
    //
    private final SharedPrefsHelper mPrefsHelper;

    DataManager(Context context) {
        this.mPrefsHelper = new SharedPrefsHelper(context);

        // should clear info about prev token
        mPrefsHelper.saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, "0");
        mPrefsHelper.saveToPrefs(PREFS_TOKEN_VALID_TIME, "0");

        final String authToken = Credentials.basic(APP_ID, "");
        createRetrofit(authToken);

        Log.d(TAG, "DataManager created");
    }

    private void createRetrofit(String token) {
        Log.d(TAG, "createRetrofit: About to create Retrofit instance");
        if (mRetrofit != null) {
            mRetrofit = null;
        }


        Log.d(TAG, "createRetrofit: About to create AuthenticationInterceptor");
        AuthenticationInterceptor interceptor =
                new AuthenticationInterceptor(token);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (!httpClient.interceptors().contains(interceptor)) {
            httpClient.addInterceptor(interceptor);
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (!httpClient.interceptors().contains(loggingInterceptor)) {
            httpClient.addInterceptor(loggingInterceptor);
        }


        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://oauth.reddit.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        mApi = mRetrofit.create(RedditAPI.class);
    }

    private String obtainTokenSynchronously() {
        Log.d(TAG, "obtainTokenSynchronously: About to obtain token synchronously");
        try {
            Response<TokenResponse> response = mApi.obtainToken(PARAM_GRAND_TYPE, getDeviceId()).execute();
            TokenResponse tokenResponse = response.body();
            if (tokenResponse == null) {
                // TODO handle
                return "";
            } else {
                String type = tokenResponse.getTokenType();
                String newToken = type + " " + tokenResponse.getAccessToken();

                mPrefsHelper.saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, String.valueOf(System.currentTimeMillis()));
                mPrefsHelper.saveToPrefs(PREFS_TOKEN_VALID_TIME, String.valueOf(tokenResponse.getExpiresIn() * 1000));

                // needed for adding obtained token to all responses
                createRetrofit(newToken);
                return newToken;
            }
        } catch (IOException e) {
            Log.e(TAG, "obtainTokenSynchronously: Failed to obtain token synchronously");
            e.printStackTrace();
        }
        return "";
    }

//    private void obtainToken() {
//        mApi.obtainToken(PARAM_GRAND_TYPE, getDeviceId()).enqueue(new Callback<TokenResponse>() {
//            @Override
//            public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
//                Log.d("MainActivity", "onResponse");
//
//                TokenResponse tokenResponse = response.body();
//                if (tokenResponse == null) {
//                    // TODO handle
//                } else {
//                    String type = tokenResponse.getTokenType();
//                    String newToken = type + " " + tokenResponse.getAccessToken();
//
//                    mPrefsHelper.saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, String.valueOf(System.currentTimeMillis()));
//                    mPrefsHelper.saveToPrefs(PREFS_TOKEN_VALID_TIME, String.valueOf(tokenResponse.getExpiresIn() * 1000));
//
//                    // needed for adding obtained token to all responses
//                    createRetrofit(newToken);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<TokenResponse> call, Throwable t) {
//                Log.d("MainActivity", "onFailure");
//                // TODO handle
//            }
//        });
//    }

    private void requestListing(String startFrom, int count, final OnListingObtainedListener callback) {
        Log.d(TAG, "requestTopListing: About to obtain listing. startFrom= " + startFrom + "; count= " + count);
        Callback requestCallback = new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, retrofit2.Response<ListingResponse> response) {
                Log.d(TAG, "requestTopListing: onResponse");
                ListingResponse listing = response.body();
                if(listing == null){
                    callback.onFailedToObtainList();
                }else{
                    callback.onListingObtained(listing.getData().getChildren());
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Log.e(TAG, "requestTopListing: onFailure", t);
            }
        };

        if(TextUtils.isEmpty(startFrom)){
            mApi.getTopList(count).enqueue(requestCallback);
        }else{
            mApi.getTopList(startFrom, count).enqueue(requestCallback);
        }
    }

    @Override
    public void requestTopListing(final String startFrom, final int count, final OnListingObtainedListener callback) {
        if (isTokenValid()) {
            requestListing(startFrom, count, callback);
        } else {
            Log.d(TAG, "requestTop: token is invalid");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    String token = obtainTokenSynchronously();
                    if (TextUtils.isEmpty(token)) {
                        callback.onFailedToObtainList();
                    }
                    requestListing(startFrom, count, callback);
                }
            });
        }
    }

    private String getDeviceId() {
        String deviceId = mPrefsHelper.readFromPrefs(PREFS_DEVICE_ID, "");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            mPrefsHelper.saveToPrefs(PREFS_DEVICE_ID, deviceId);
        }

        return deviceId;
    }

    private boolean isTokenValid() {
        final long currTime = System.currentTimeMillis();
        final long tokenObtainedTime = Long.parseLong(mPrefsHelper.readFromPrefs(PREFS_TOKEN_OBTAINED_TIME, "0"));
        final long tokenValidTime = Long.parseLong(mPrefsHelper.readFromPrefs(PREFS_TOKEN_VALID_TIME, "0"));

        final long delta = currTime - tokenObtainedTime;
        return delta < tokenValidTime;
    }

    // region AuthenticationInterceptor ============================================================
    public class AuthenticationInterceptor implements Interceptor {

        public static final String HEADER_AUTHORIZATION = "Authorization";
        public static final String HEADER_ACCEPT = "Accept";
        public static final String HEADER_ACCEPT_VALUE = "application/json";

        private String authToken;

        public AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .header(HEADER_AUTHORIZATION, authToken)
                    .header(HEADER_ACCEPT, HEADER_ACCEPT_VALUE).build();
            return chain.proceed(authenticatedRequest);
        }
    }

}
