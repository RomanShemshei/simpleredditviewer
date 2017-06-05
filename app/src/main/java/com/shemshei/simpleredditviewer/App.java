package com.shemshei.simpleredditviewer;

import android.app.Application;

import com.shemshei.simpleredditviewer.rest.DataManager;
import com.shemshei.simpleredditviewer.rest.RedditAPI;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by roma on 29.05.17.
 */

public class App extends Application {

    private Retrofit mRetrofit;
    private RedditAPI mApi;

    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
//        final String authToken = Credentials.basic("reddit_simple_reader", "hHFxuoK11Br8zA");
        final String authToken = Credentials.basic("hHFxuoK11Br8zA", "");
//        final String authToken = Credentials.basic("reddit_simple_reader", "");

//        createRetrofit(authToken);


        mDataManager = new DataManager(getApplicationContext());
    }

    public RedditAPI getApi() {
        return mApi;
    }

    public DataManager getDataManager(){
        return mDataManager;
    }

    public void createRetrofit(String token){
        if(mRetrofit != null){
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

    public class AuthenticationInterceptor implements Interceptor {

        private String authToken;

        public AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .header("Authorization", authToken)
                    .header("Accept", "application/json").build();
            return chain.proceed(authenticatedRequest);


//            Request original = chain.request();
//
//            Request.Builder builder = original.newBuilder()
//                    .header("Authorization", authToken);
//
//            Request request = builder.build();
//            return chain.proceed(request);
        }
    }
}
