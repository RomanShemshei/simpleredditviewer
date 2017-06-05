package com.shemshei.simpleredditviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.shemshei.simpleredditviewer.pojo.Child;
import com.shemshei.simpleredditviewer.rest.DataManager;
import com.shemshei.simpleredditviewer.rest.ListingResponse;
import com.shemshei.simpleredditviewer.rest.TokenResponse;
import com.shemshei.simpleredditviewer.ui.RedditContentAdapter;
import com.shemshei.simpleredditviewer.ui.RedditContentAdapter.OnItemClickedListener;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.shemshei.simpleredditviewer.ui.RedditContentAdapter.CARD_VIEW;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeToRefresh;
    private RecyclerView mRecyclerView;
    private RedditContentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //
    private final OnItemClickedListener mOnItemClickListener = new OnItemClickedListenerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.my_swipe_to_refresh);
        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                obtainList("", 10);
            }
        });
        obtainList("", 10);
    }

    private void hideRefreshingProgress(){
        mSwipeToRefresh.post(new Runnable() {
            @Override
            public void run() {
                mSwipeToRefresh.setRefreshing(false);
            }
        });
    }

    private void obtainList(final String startFrom, int count) {
        final App application = (App) getApplication();

        application.getDataManager().requestTop(startFrom, count, new DataManager.OnListingObtainedListener() {
            @Override
            public void onListingObtained(final List<Child> children) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mAdapter == null){
                            mAdapter = new RedditContentAdapter(getApplicationContext(), children, mOnItemClickListener);
                            mRecyclerView.setAdapter(mAdapter);
                        }else{
                            mAdapter.updateContent(children, TextUtils.isEmpty(startFrom));
                        }
                        hideRefreshingProgress();
                    }
                });
            }

            @Override
            public void onFailedToObtainList() {
                hideRefreshingProgress();
            }
        });
    }

    private void proceed() {
        final App application = (App) getApplication();

        String grantType = "https://oauth.reddit.com/grants/installed_client";

        SharedPreferences prefs = getSharedPreferences("my.private.preferences", Context.MODE_PRIVATE);

        String deviceId = prefs.getString("DEVICE_ID", "");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("DEVICE_ID", deviceId);
            editor.apply();
        }

        application.getApi().obtainToken(grantType, deviceId).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                Log.d("MainActivity", "onResponse");

                String newToken = "bearer " + response.body().getAccessToken();
                application.createRetrofit(newToken);

                application.getApi().getTopList(15).enqueue(new Callback<ListingResponse>() {
                    @Override
                    public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                        Log.d("MainActivity", "onResponse2");

                        // specify an adapter (see also next example)
                        mAdapter = new RedditContentAdapter(getApplicationContext(), response.body().getData().getChildren(), mOnItemClickListener);
                        mRecyclerView.setAdapter(mAdapter);
                        hideRefreshingProgress();
                    }

                    @Override
                    public void onFailure(Call<ListingResponse> call, Throwable t) {
                        Log.e("MainActivity", "onFailure2", t);
                        hideRefreshingProgress();
                    }
                });
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.d("MainActivity", "onFailure");
            }
        });
    }

    // region OnItemClickedListener ================================================================
    private class OnItemClickedListenerImpl implements OnItemClickedListener {
        @Override
        public void onItemClicked(int type, Child content) {
            if (type == CARD_VIEW) {
                // TODO view large image
            } else {
                // TODO load more items
                String from = content.getData().getName();
                obtainList(from, 10);
            }
        }
    }
    // endregion ===================================================================================
}
