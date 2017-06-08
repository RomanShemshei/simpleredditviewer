package com.shemshei.simpleredditviewer;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.shemshei.simpleredditviewer.pojo.Child;
import com.shemshei.simpleredditviewer.rest.OnListingObtainedListener;
import com.shemshei.simpleredditviewer.ui.AbstractContentAdapter;
import com.shemshei.simpleredditviewer.ui.AbstractContentAdapter.OnContentAdapterListener;
import com.shemshei.simpleredditviewer.ui.ContentAdapterFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());
    //
    private static final int NUMBER_OF_ITEMS_PER_PAGE = 10;
    private static final String START_FROM_BEGINNING = "";
    //
    private SwipeRefreshLayout mSwipeToRefresh;
    private RecyclerView mRecyclerView;
    private AbstractContentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //
    private final OnContentAdapterListener mOnItemClickListener = new OnItemClickedListenerImpl();
    //
    private final AtomicBoolean mRefreshingInProgress = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.my_swipe_to_refresh);
        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                obtainList(START_FROM_BEGINNING, NUMBER_OF_ITEMS_PER_PAGE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mAdapter == null
                || mAdapter.getItemCount() == 0){
            showRefreshingProgress();
            obtainList(START_FROM_BEGINNING, NUMBER_OF_ITEMS_PER_PAGE);
        }
    }

    private void hideRefreshingProgress(){
        mSwipeToRefresh.post(new Runnable() {
            @Override
            public void run() {
                mSwipeToRefresh.setRefreshing(false);
            }
        });
    }

    private void showRefreshingProgress(){
        mSwipeToRefresh.post(new Runnable() {
            @Override
            public void run() {
                mSwipeToRefresh.setRefreshing(true);
            }
        });
    }

    private void obtainList(final String startFrom, int count) {
        final App application = (App) getApplication();
        mRefreshingInProgress.set(true);
        application.getDataManager().requestTopListing(startFrom, count, new OnListingObtainedListener() {
            @Override
            public void onListingObtained(final List<Child> children) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mAdapter == null){
                            mAdapter = ContentAdapterFactory.createContentAdapter(
                                    ContentAdapterFactory.SOURCE_REDDIT,
                                    getApplicationContext(),
                                    children,
                                    mOnItemClickListener
                            );
                            mRecyclerView.setAdapter(mAdapter);
                        }else{
                            mAdapter.updateContent(children, TextUtils.isEmpty(startFrom));
                        }
                        hideRefreshingProgress();
                        mRefreshingInProgress.set(false);
                    }
                });
            }

            @Override
            public void onFailedToObtainList() {
                hideRefreshingProgress();
                mRefreshingInProgress.set(false);
                Toast.makeText(getApplicationContext(), "Failed lo obtain list...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUrlInBrowser(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    // region OnItemClickedListener ================================================================
    private class OnItemClickedListenerImpl implements OnContentAdapterListener {

        @Override
        public void onItemClicked(Child clickedItem) {
            if(mRefreshingInProgress.get()){
                logger.warning("Won't lopen in browser. Refreshing in progress");
                return;
            }
            String url = clickedItem.getData().getUrl();
            if(TextUtils.isEmpty(url)){
                // TODO notify user
            }else{
                openUrlInBrowser(url);
            }
        }

        @Override
        public void onMoreContentNeeded(Child lastItem) {
            if(mRefreshingInProgress.get()){
                logger.warning("Won't load content. refreshing on progress");
                return;
            }
            String from = lastItem.getData().getName();
            showRefreshingProgress();
            obtainList(from, NUMBER_OF_ITEMS_PER_PAGE);
        }
    }
    // endregion ===================================================================================
}
