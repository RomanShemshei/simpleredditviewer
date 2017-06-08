package com.shemshei.simpleredditviewer.rest;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(DataManager.class.getName());

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
        logger.fine("DataManager created");
    }

    private void createRetrofit(String token) {
        logger.fine("About to create Retrofit instance");
        if (mRetrofit != null) {
            mRetrofit = null;
        }

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
        logger.fine("About to obtain token synchronously");
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
            logger.severe("Failed to obtain token synchronously. Error= " + e.getMessage());
        }
        return "";
    }

    private void requestListing(String startFrom, int count, final OnListingObtainedListener callback) {
        logger.fine("About to obtain listing. startFrom= " + startFrom + "; count= " + count);
        Callback requestCallback = new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, retrofit2.Response<ListingResponse> response) {
                logger.fine("requestTopListing: onResponse");
                ListingResponse listing = response.body();
                if (listing == null) {
                    logger.fine("requestTopListing: response body is null");
                    callback.onFailedToObtainList();
                } else {
                    callback.onListingObtained(listing.getData().getChildren());
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                logger.severe("requestListing: onFailure. Error= " + t.getMessage());
                callback.onFailedToObtainList();
            }
        };

        if (TextUtils.isEmpty(startFrom)) {
            mApi.getTopList(count).enqueue(requestCallback);
        } else {
            mApi.getTopList(startFrom, count).enqueue(requestCallback);
        }
    }

    @Override
    public void requestTopListing(final String startFrom, final int count, final OnListingObtainedListener callback) {
        if (isTokenValid()) {
            requestListing(startFrom, count, callback);
        } else {
            logger.warning("token is invalid");
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
