package com.example.kystatham.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.kystatham.nytimessearch.R;
import com.example.kystatham.nytimessearch.adapters.ArticleArrayAdapter;
import com.example.kystatham.nytimessearch.adapters.EndlessScrollListener;
import com.example.kystatham.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {

    GridView gvResults;
    String searchQuery;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    private final int FILTER_REQUEST_CODE = 20;
    private int offset = 0;

    String sortOrder;
    String newsDesk;
    Date beginDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    public void setupViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isNetworkAvailable() && isOnline()) {
                    Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                    Article article = articles.get(position);
                    i.putExtra("article", article);
                    startActivity(i);
                } else {
                    showNoInternetToast();
                }
            }
        });

        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                offset = page;
                onArticleSearch();
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchQuery = query;
                adapter.clear();
                adapter.notifyDataSetChanged();
                offset = 0;
                onArticleSearch();

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showFilterView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FILTER_REQUEST_CODE) {
            sortOrder = data.getStringExtra("sort");
            newsDesk = data.getStringExtra("news_desk");
            beginDate = (Date) data.getSerializableExtra("begin_date");
        }
    }

    public void onArticleSearch() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";

        RequestParams params = new RequestParams();
        params.put("api-key", "bb47ba367da542c69919879967ec9d4a");
        params.put("page", offset);

        if (searchQuery != null) {
            params.put("q", searchQuery);
        }

        if (sortOrder != null) {
            params.put("sort", sortOrder);
        }
        if (newsDesk != null && newsDesk.length() > 0) {
            params.put("fq", "news_desk:" + newsDesk);
        }
        if (beginDate != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            params.put("begin_date", simpleDateFormat.format(beginDate));
        }

        if (isNetworkAvailable() && isOnline()) {
            client.get(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    JSONArray articleJsonResults = null;

                    try {
                        articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                        adapter.addAll(Article.fromJSONArray(articleJsonResults));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (statusCode == 429) {
                        retrySearch();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unexpected error occurred.", Toast.LENGTH_SHORT);
                    }
                }
            });
        } else {
            showNoInternetToast();
        }
    }

    public void retrySearch() {
        Handler handler = new Handler();

         Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                onArticleSearch();
            }
        };

        handler.postDelayed(runnableCode, 2000);
    }

    public void showFilterView() {
        Intent i = new Intent(getApplicationContext(), FilterActivity.class);
        startActivityForResult(i, FILTER_REQUEST_CODE);
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    public void showNoInternetToast() {
        Toast.makeText(getApplicationContext(), "Please connect to internet.", Toast.LENGTH_SHORT).show();
    }
}
