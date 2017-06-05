package com.shemshei.simpleredditviewer.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.shemshei.simpleredditviewer.BuildConfig;
import com.shemshei.simpleredditviewer.pojo.Child;
import com.shemshei.simpleredditviewer.ui.RedditContentAdapter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by romanshemshei on 6/4/17.
 */

public class DataManager {

    public static final String TAG = DataManager.class.getSimpleName();

    public static final String APP_ID = "hHFxuoK11Br8zA";
    public static final String PREFERENCES = BuildConfig.APPLICATION_ID + ".private.preferences";
    public static final String PREFS_DEVICE_ID = "DEVICE_ID";
    public static final String PREFS_TOKEN_OBTAINED_TIME = "TOKEN_TIME";
    public static final String PREFS_TOKEN_VALID_TIME = "TOKEN_VALID_TIME";

    public static final String PARAM_GRAND_TYPE = "https://oauth.reddit.com/grants/installed_client";

    private Retrofit mRetrofit;
    private RedditAPI mApi;
    //
    private Context mContext;

    public DataManager(Context context) {
        mContext = context;

        // should clear info about prev token
        saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, "0");
        saveToPrefs(PREFS_TOKEN_VALID_TIME, "0");

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

                saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, String.valueOf(System.currentTimeMillis()));
                saveToPrefs(PREFS_TOKEN_VALID_TIME, String.valueOf(tokenResponse.getExpiresIn() * 1000));

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

    private void obtainToken() {
        mApi.obtainToken(PARAM_GRAND_TYPE, getDeviceId()).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
                Log.d("MainActivity", "onResponse");

                TokenResponse tokenResponse = response.body();
                if (tokenResponse == null) {
                    // TODO handle
                } else {
                    String type = tokenResponse.getTokenType();
                    String newToken = type + " " + tokenResponse.getAccessToken();

                    saveToPrefs(PREFS_TOKEN_OBTAINED_TIME, String.valueOf(System.currentTimeMillis()));
                    saveToPrefs(PREFS_TOKEN_VALID_TIME, String.valueOf(tokenResponse.getExpiresIn() * 1000));

                    // needed for adding obtained token to all responses
                    createRetrofit(newToken);
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.d("MainActivity", "onFailure");
                // TODO handle
            }
        });
    }

    private void requestTopListing(String startFrom, int count, final OnListingObtainedListener callback) {
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
            mApi.getTopList(count, startFrom).enqueue(requestCallback);
        }
    }

    public void requestTop(final String startFrom, final int count, final OnListingObtainedListener callback) {
        if (isTokenValid()) {
            requestTopListing(startFrom, count, callback);
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
                    requestTopListing(startFrom, count, callback);
                }
            });
        }
    }

    private String getDeviceId() {
        String deviceId = readFromPrefs(PREFS_DEVICE_ID, "");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            saveToPrefs(PREFS_DEVICE_ID, deviceId);
        }

        return deviceId;
    }

    private boolean isTokenValid() {
        final long currTime = System.currentTimeMillis();
        final long tokenObtainedTime = Long.parseLong(readFromPrefs(PREFS_TOKEN_OBTAINED_TIME, "0"));
        final long tokenValidTime = Long.parseLong(readFromPrefs(PREFS_TOKEN_VALID_TIME, "0"));

        final long delta = currTime - tokenObtainedTime;
        return delta < tokenValidTime;
    }

    private void saveToPrefs(String key, String value) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String readFromPrefs(String key, String defaultValue) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
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
    // endregion ===================================================================================
    //
    //
    public interface OnListingObtainedListener{

        void onListingObtained(List<Child> children);

        void onFailedToObtainList();

    }
}
