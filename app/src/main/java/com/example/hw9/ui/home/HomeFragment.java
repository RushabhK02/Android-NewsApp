package com.example.hw9.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements LocationListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

//    String url = "http://10.0.2.2:8080/apphome";
    String url = "https://wt-hw9-server.wl.r.appspot.com/apphome";
    String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=";
    String weatherCreds = "&units=metric&appid=b3e487183fca16f64304e54d2b4ac4e5";
    RequestQueue queue;
    private MainActivity activity;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final int REQUEST_LOCATION = 123;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    LocationManager locationManager;
    Location location;
    Double myLat, myLong;
    String city="", state="", country="", zipcode="", fullAddress="";

    private RecyclerView mList;
    private LinearLayout progressLayout;

    private LinearLayoutManager linearLayoutManager;
    private View view;
    private DividerItemDecoration dividerItemDecoration;
    private List<Article> articleList = new ArrayList<>();
    private ArticleItem.WeatherCard weather;
    private ArticleRecyclerViewAdapter adapter;
    private Context context;
    private BookmarkCache bookmarkCache;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(int columnCount) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        activity = (MainActivity) getActivity();
        bookmarkCache = activity.getBookmarkCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);
        this.view = view;
        context = view.getContext();
        checkLocationPermission();
        return view;
    }

    public void init(){
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_home);
        queue = Volley.newRequestQueue(context);

        mList = view.findViewById(R.id.list);
        mList.setVisibility(View.GONE);
        adapter = new ArticleRecyclerViewAdapter(articleList, mListener, null, context, activity);
        progressLayout = view.findViewById(R.id.progress_layout);

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
//        dividerItemDecoration.setDrawable(new ColorDrawable(getResources().getColor(R.color.cleanBlack)));
        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);
        getData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getArticleData();
            }
        });
    }

    private void getData() {
        getWeatherData();
        getArticleData();
    }

    public void getArticleData() {
        articleList = new ArrayList<>();
//        Log.i("Fragment data from activity", city);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                        articleList.add(article);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.i("Home data", String.valueOf(data));
                adapter.swapItems(articleList, weather);
                if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                mList.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error getting home data", error.toString());
                progressLayout.setVisibility(View.GONE);
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);
    }

    public void getWeatherData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, weatherUrl+city+weatherCreds, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    double temp = response.getJSONObject("main").getDouble("temp");
                    JSONArray summary = response.getJSONArray("weather");
                    JSONObject forecast = (JSONObject) summary.get(0);
                    String desc = forecast.getString("main");
                    Log.i("Weather data", city+" , "+temp+" , "+desc);
                    weather = new ArticleItem.WeatherCard(city, state, String.valueOf(temp), desc);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter.swapItems(articleList, weather);
                mList.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error getting weather data", error.toString());
                progressLayout.setVisibility(View.GONE);
            }
        });

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            }
            else {
                activity.exit();
            }
        } else {
            activity.exit();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("CheckResult")
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            location = getLocation();
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(myLat, myLong, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!addresses.isEmpty()) {
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                zipcode = addresses.get(0).getPostalCode();
                country = addresses.get(0).getCountryCode();
                fullAddress = addresses.get(0).getAddressLine(0);
            }
//            Log.i("Location details", myLat + " , "+myLong);
//            Log.i("Location details by place", fullAddress);
//            getWeatherData();
            init();
        }
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        Location location = null;
        try {
            locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) { }
            else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        Criteria criteria = new Criteria();
                        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        location = locationManager.getLastKnownLocation(provider);

                        if (location != null) {
                            myLat = location.getLatitude();
                            myLong = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            Criteria criteria = new Criteria();
                            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                            String provider = locationManager.getBestProvider(criteria, true);
                            location = locationManager.getLastKnownLocation(provider);
                            if (location != null) {
                                myLat = location.getLatitude();
                                myLong = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Article item, Boolean isLongPress, RecyclerView.Adapter adapter, Boolean bookmarkFrag);
    }
}
