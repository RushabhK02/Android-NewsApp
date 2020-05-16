package com.example.hw9.functionalities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;
import com.example.hw9.model.SectionArticleItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TimeZone;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DetailedArticleActivity extends AppCompatActivity {
    final Transformation atransformation = new RoundedCornersTransformation(35, 0, RoundedCornersTransformation.CornerType.TOP);

    String section;
    String date;
    String description;
    String artUrl;
    String artId;
    String image;
    String title;
    boolean bookmarkFlag=false;
    boolean loading = true;

    RequestQueue queue;
//    String url = "http://10.0.2.2:8080"+"/article?id=";
    String url = "https://wt-hw9-server.wl.r.appspot.com"+"/article?id=";
    private LinearLayout progressLayout;
    private ScrollView detailsCard;
    private Menu menu;
    private BookmarkCache bookmarkCache;

    Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
//        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
//        actionBar.setHomeAsUpIndicator(upArrow);

        loading = true;
        queue = Volley.newRequestQueue(getApplicationContext());
        View view = findViewById(R.id.det_layout).getRootView();
        detailsCard = view.findViewById(R.id.det_card);
        detailsCard.setVisibility(View.GONE);
        progressLayout = view.findViewById(R.id.progress_layout);

        article = (Article) getIntent().getSerializableExtra("article");
        MainActivity parent = (MainActivity) getParent();
        bookmarkCache = BookmarkCache.getInstance(this);
        bookmarkFlag = bookmarkCache.contains(article);
        getData(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if(!loading) getMenuInflater().inflate(R.menu.detailed_article_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(loading) return super.onOptionsItemSelected(item);
        int id = item.getItemId();
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_bookmark:
                bookmarkCache.toggleBookmark(article);
                bookmarkFlag = bookmarkCache.contains(article);
                if(bookmarkFlag) Toast.makeText(this, "\""+title+"\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "\""+title+"\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
//                Log.i("Twitter icon clicked", "PING!!");
                Uri webpage = Uri.parse("https://twitter.com/intent/tweet?text="+"Check out this Link: "+artUrl+"&hashtags=CSCI571NewsSearch");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(loading) return super.onPrepareOptionsMenu(menu);
        MenuItem bookmarkItem = this.menu.findItem(R.id.action_bookmark);
        // set your desired icon here based on a flag if you like
        if(bookmarkFlag) bookmarkItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_tab_fill_red));
        else bookmarkItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_tab_border_red));

        return super.onPrepareOptionsMenu(menu);
    }

    public void setMenu(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(article.title);
        invalidateOptionsMenu();
    }

    private void getData(View view){
        String id = article.artId;
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url+id, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject art = response.getJSONObject("formattedArticle");
                    section = art.getString("section");
                    String upperString = section.substring(0, 1).toUpperCase() +
                                            section.substring(1).toLowerCase();
                    section = upperString;
                    image = art.getString("imageUrl");
                    title = art.getString("title");
                    description = art.getString("description");
                    artUrl = art.getString("artUrl");
                    artId = art.getString("artId");
                    date = calcPubDate(art.getString("pubDate"));

                    loadData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error getting detailed Article!",error.getMessage());
            }
        });

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(objectRequest);
    }

    public String calcPubDate(String pubDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime ldt = LocalDateTime.parse(pubDate,formatter);
        ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
        ZonedDateTime zdt = ldt.atZone( zoneId );

        String date = DateTimeFormatter.ofPattern("dd MMM yyyy").format(zdt);
        return date;
    }

    private void loadData(View view){
        ImageView imageView = view.findViewById(R.id.article_img);
//        Log.i("Detailed Article", image);
//        Picasso.get()
        Picasso.with(this)
                .load(image)
                .fit()
                .transform(atransformation)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
//                        Picasso.get()
                        Picasso.with(DetailedArticleActivity.this)
                                .load(R.drawable.default_guardian)
                                .fit()
                                .transform(atransformation)
                                .into(imageView);
                    }
                });
        TextView titleTv = view.findViewById(R.id.det_title);
        titleTv.setText(title);
        TextView sectionTv = view.findViewById(R.id.det_section);
        sectionTv.setText(section);
        TextView dateTv = view.findViewById(R.id.det_date);
        dateTv.setText(date);
        TextView descriptionTv = view.findViewById(R.id.det_desc);
//        descriptionTv.setText(description);
        descriptionTv.setText(Html.fromHtml(description,Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        TextView link = view.findViewById(R.id.det_link);

        link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(view.getContext(), "Open Web page!!", Toast.LENGTH_SHORT).show();
                Uri webpage = Uri.parse(artUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        loading = false;
        setMenu();

        detailsCard.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
//        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null) bookmarkCache.refresh();
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
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onStop();
    }
}
