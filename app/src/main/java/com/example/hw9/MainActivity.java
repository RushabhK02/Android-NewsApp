package com.example.hw9;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.functionalities.BookmarkCache;
import com.example.hw9.functionalities.DetailedArticleActivity;
import com.example.hw9.functionalities.search.AutoSuggestAdapter;
import com.example.hw9.functionalities.search.SearchResultActivity;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.ui.bookmark.BookmarkRecyclerViewAdapter;
import com.example.hw9.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnListFragmentInteractionListener{

    private static final int TRIGGER_AUTO_COMPLETE = 300;
    private static final long AUTO_COMPLETE_DELAY = 500;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    RecyclerView.Adapter currAdapter;
    AppCompatActivity activity;
    BookmarkCache bookmarkCache;
    Integer bookmarkCount;
    RequestQueue queue;
    String url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=";
    String bingKey = "f81f3d5657bb44d9a54392d44f7346f1";

    public BookmarkCache getBookmarkCache() { return bookmarkCache; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    public void init() {
        if(bookmarkCache!= null) return;
        bookmarkCache = BookmarkCache.getInstance(activity);
        queue = Volley.newRequestQueue(this);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_headlines, R.id.navigation_trending, R.id.navigation_bookmark)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_action_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

//        searchView.setBackgroundColor(getResources().getColor(R.color.sectionTabColor));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchAutoComplete.setTextColor(R.color.cleanBlack);
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setColorFilter(getResources().getColor(R.color.searchIcons), PorterDuff.Mode.SRC_ATOP);

//        searchAutoComplete.setBackgroundColor(getResources().getColor(R.color.sectionTabColor));
        ImageView searchHintIcon = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchHintIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        ComponentName component = new ComponentName(this, SearchResultActivity.class);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(component);
        searchView.setSearchableInfo(searchableInfo);

//         Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText(queryString);
            }
        });

        setupAutoSuggest(searchView);
        return true;
    }

    public void exit(){
        finishAffinity();
        finishAndRemoveTask();
        finish();
        System.exit(0);
    }

    private void setupAutoSuggest(SearchView searchView) {

        final AppCompatAutoCompleteTextView autoCompleteTextView =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        autoCompleteTextView.setTextColor(getResources().getColor(R.color.cleanBlack));
        autoCompleteTextView.setDropDownBackgroundResource(R.color.sectionTabColor);
        autoCompleteTextView.setDropDownHeight(1100);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(3);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        fetchSuggestions(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void fetchSuggestions(String toString) {
        if(toString.length()<3) {
            autoSuggestAdapter.clearData();
            autoSuggestAdapter.notifyDataSetChanged();
            return;
        }
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET,
                url+toString, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<String> stringList = new ArrayList<>();
//                Log.i("Autosuggest data",response.toString());

                try {
                    JSONArray suggestionGroups = response.getJSONArray("suggestionGroups");

                    JSONArray searchSuggestions = suggestionGroups.getJSONObject(0)
                                                    .getJSONArray("searchSuggestions");
                    for(int i=0; i<searchSuggestions.length(); i++){
                       JSONObject obj = new JSONObject(searchSuggestions.getString(i));
                        String value = obj.getString("displayText");
                        stringList.add(value);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Log.i("Suggestions", String.valueOf(stringList));
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
                Log.e("Error getting search data", error.toString() + error.getMessage());

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Ocp-Apim-Subscription-Key", bingKey);
                return headers;
            }
        };

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(objectRequest);

    }

    @Override
    protected void onDestroy() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onDestroy();
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

    @Override
    protected void onRestart() {
//        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null) bookmarkCache.refresh();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        bookmarkCount = bookmarkCache.getFavorites().size();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onStop();
    }

    @Override
    public void onListFragmentInteraction(ArticleItem.Article item, Boolean isLongPress, RecyclerView.Adapter adapter,
                                          Boolean bookmarkFrag) {
        if(!isLongPress) {
            Intent articleIntent = new Intent(getApplicationContext(), DetailedArticleActivity.class);
            articleIntent.putExtra("article", item);
            currAdapter = adapter;
            startActivity(articleIntent);
        } else {
            openArticleDialog(item, adapter, bookmarkFrag);
        }
    }

    public void openArticleDialog(ArticleItem.Article item, RecyclerView.Adapter adapter, Boolean bookmarkFrag) {
        Dialog dialog = new Dialog(MainActivity.this);
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
        bookmark.setImageResource(bookmarkCache.contains(item)?R.drawable.ic_bookmark_red_fill_24dp:R.drawable.ic_bookmark_border_red_24dp);
//       TODO: Check bookmark
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarkCache.toggleBookmark(item);
                boolean flag = bookmarkCache.contains(item);
                if(bookmarkFrag) {
                    BookmarkRecyclerViewAdapter viewAdapter = (BookmarkRecyclerViewAdapter) adapter;
                    if(!flag) {
                        viewAdapter.removeItem(item);
                        dialog.dismiss();
                    } else viewAdapter.updateList();
                } else adapter.notifyDataSetChanged();
                bookmark.setImageResource(flag?R.drawable.ic_bookmark_red_fill_24dp:R.drawable.ic_bookmark_border_red_24dp);

                if(flag) Toast.makeText(activity, "\""+item.title+"\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                else Toast.makeText(activity, "\""+item.title+"\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
