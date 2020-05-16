package com.example.hw9.functionalities.search;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.functionalities.BookmarkCache;
import com.example.hw9.functionalities.DetailedArticleActivity;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;
import com.example.hw9.ui.home.HomeFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SearchResultActivity extends AppCompatActivity implements HomeFragment.OnListFragmentInteractionListener {

//    String url = "http://10.0.2.2:8080/search?keyword=";
    String url = "https://wt-hw9-server.wl.r.appspot.com/search?keyword=";
    String query;

    RequestQueue queue;
    private MainActivity activity;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mList;
    private RecyclerView.Adapter currAdapter;
    private LinearLayout progressLayout;
    private TextView noResultsView;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<Article> articleList = new ArrayList<>();
    private SearchRecyclerViewAdapter adapter;
    private Context context;
    BookmarkCache bookmarkCache;

    public BookmarkCache getBookmarkCache() { return bookmarkCache; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
//        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
//        actionBar.setHomeAsUpIndicator(upArrow);

        Intent intent = getIntent();
        handleIntent(intent);
        toolbar.setTitle("Search Results for "+query);
        bookmarkCache = BookmarkCache.getInstance(this);

        queue = Volley.newRequestQueue(this);
        View view = findViewById(R.id.loading_spinner).getRootView();
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_search);
        noResultsView = view.findViewById(R.id.no_search_modal);
        noResultsView.setVisibility(View.GONE);
        progressLayout = findViewById(R.id.loading_spinner);
        mList = view.findViewById(R.id.search_list);
        context = this;
        mList.setVisibility(View.GONE);
        activity = (MainActivity) getParent();
        adapter = new SearchRecyclerViewAdapter(articleList, this, context, this);

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);
        getData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    private void getData() {
        ArrayList<Article> data = new ArrayList<>();
//        Log.i("Fragment data from activity", city);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url+query, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray articles = response.getJSONArray("formattedArticles");
                    for(int i=0; i<articles.length();i++){
                        JSONObject obj = articles.getJSONObject(i);
                        Article article =
                                new Article( obj.getString("artId"),
                                        obj.getString("section"),
                                        obj.getString("title"),
                                        obj.getString("pubDate"),
                                        "",
                                        obj.getString("artUrl"),
                                        obj.getString("imageUrl"));
                        data.add(article);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.i("Search data", String.valueOf(data));
                if(data.isEmpty()) {
                    noResultsView.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }
                adapter.swapItems(data);
                if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                mList.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error getting search data", error.toString());
                progressLayout.setVisibility(View.GONE);
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
//        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null)
        {
            bookmarkCache.refresh();
        }
        if(currAdapter!=null) currAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Log.i("Search Query", query);
        }
    }

    @Override
    public void onListFragmentInteraction(ArticleItem.Article item, Boolean isLongPress, RecyclerView.Adapter adapter, Boolean bookmarkFrag) {
        if(!isLongPress) {
            Intent articleIntent = new Intent(getApplicationContext(), DetailedArticleActivity.class);
            articleIntent.putExtra("article", item);
            currAdapter = adapter;
            startActivity(articleIntent);
        } else {
            openArticleDialog(item, adapter);
        }
    }

    public void openArticleDialog(ArticleItem.Article item, RecyclerView.Adapter adapter) {
        Dialog dialog = new Dialog(SearchResultActivity.this);
        dialog.setContentView(R.layout.article_dialog);
        dialog.setTitle("Article");
        TextView textView = dialog.findViewById(R.id.dialog_title);
        textView.setText(item.title);
        ImageView imageView = dialog.findViewById(R.id.dialog_image);
//        Picasso.get()
        Picasso.with(this)
                .load(item.imageUrl)
                .error(R.drawable.default_guardian)
                .transform(new RoundedCornersTransformation(10,0))
                .fit()
                .into(imageView);

        ImageView twitter = dialog.findViewById(R.id.dialog_twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(activity, "Twitter share clicked!", Toast.LENGTH_SHORT).show();
                String artUrl = item.artUrl;
                Uri webpage = Uri.parse("https://twitter.com/intent/tweet?text="+"Check out this Link: "+artUrl+"&hashtags=CSCI571NewsSearch");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        ImageView bookmark = dialog.findViewById(R.id.dialog_bookmark);
        Activity currentActivity = this;
        bookmark.setImageResource(bookmarkCache.contains(item)?R.drawable.ic_bookmark_red_fill_24dp:R.drawable.ic_bookmark_border_red_24dp);
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarkCache.toggleBookmark(item);
                boolean flag = bookmarkCache.contains(item);
                adapter.notifyDataSetChanged();
                bookmark.setImageResource(flag?R.drawable.ic_bookmark_red_fill_24dp:R.drawable.ic_bookmark_border_red_24dp);

                if(flag) Toast.makeText(currentActivity, "\""+item.title+"\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                else Toast.makeText(currentActivity, "\""+item.title+"\" was removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
//        bookmarkCache.commitChanges();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        bookmarkCache.commitChanges();
        super.onPause();
    }

    @Override
    protected void onStop() {
        bookmarkCache.commitChanges();
        super.onStop();
    }

}
