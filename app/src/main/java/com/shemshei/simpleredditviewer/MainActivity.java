package com.shemshei.simpleredditviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final App application = (App) getApplication();
        //

//        https://www.reddit.com/api/v1/authorize?scope=read&state=RomaDev&response_type=token&redirect_uri=https://www.google.com&client_id=hHFxuoK11Br8zA


//        Map<String, String> map = new TreeMap<>();
//        map.put("client_id", "hHFxuoK11Br8zA");
//        map.put("response_type", "token");
//        map.put("state", "RomaDev");
//        map.put("redirect_uri", "https://www.google.com");
//        map.put("scope", "read");



//        application.getApi().authorize("https://www.reddit.com/api/v1/authorize",
//                "hHFxuoK11Br8zA",
//                "token",
//                "RomaDev",
//                "http://localhost:8080",
//                "read").enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                Log.d("MainActivity", "onResponse authorization");
//                proceed();
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                Log.d("MainActivity", "onFailure authorization");
//            }
//        });


        proceed();
    }

    private void proceed(){
        final App application = (App) getApplication();

        String grantType = "https://oauth.reddit.com/grants/installed_client";

        SharedPreferences prefs = getSharedPreferences("my.private.preferences", Context.MODE_PRIVATE);

        String deviceId = prefs.getString("DEVICE_ID", "");
        if(TextUtils.isEmpty(deviceId)){
            deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("DEVICE_ID", deviceId);
            editor.apply();
        }

//        ObtainTokenBody tokenBody = new ObtainTokenBody(grantType, deviceId);

        application.getApi().obtainToken(grantType, deviceId).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                Log.d("MainActivity", "onResponse");

                String newToken = "bearer " + response.body().getAccessToken();
                application.createRetrofit(newToken);

                application.getApi().getTopList().enqueue(new Callback<Child>() {
                    @Override
                    public void onResponse(Call<Child> call, Response<Child> response) {
                        Log.d("MainActivity", "onResponse2");
                    }

                    @Override
                    public void onFailure(Call<Child> call, Throwable t) {
                        Log.d("MainActivity", "onFailure2");
                    }
                });
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.d("MainActivity", "onFailure");
            }
        });
    }
}
