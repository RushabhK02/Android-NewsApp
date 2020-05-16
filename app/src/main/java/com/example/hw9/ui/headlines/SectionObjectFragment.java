package com.example.hw9.ui.headlines;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;
import com.example.hw9.model.SectionArticleItem;
import com.example.hw9.ui.home.HomeFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SectionObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    public static final String ARG_COLUMN_COUNT = "column-count";
    public final String SECTION_NAME = "section";

    private int mColumnCount = 1;
    private String section = "";
    private HomeFragment.OnListFragmentInteractionListener mListener;
    private MainActivity activity;
//    String url = "http://10.0.2.2:8080/";
    String url = "https://wt-hw9-server.wl.r.appspot.com/";
    RequestQueue queue;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mList;
    private LinearLayout progressLayout;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<Article> articleList = new ArrayList<>();
    private SectionRecyclerViewAdapter adapter;
    private Context context;

    public SectionObjectFragment() {}

    public static SectionObjectFragment newInstance(int columnCount) {
        SectionObjectFragment fragment = new SectionObjectFragment();
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
            section = getArguments().getString(SECTION_NAME);
        }
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_section);
        context = view.getContext();
        queue = Volley.newRequestQueue(context);
        mList = view.findViewById(R.id.list_section);
        mList.setVisibility(View.GONE);

        adapter = new SectionRecyclerViewAdapter(articleList, mListener, context, activity);
        progressLayout = view.findViewById(R.id.progress_layout);

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
        mList.setHasFixedSize(false);

        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);
        mList.setLayoutManager(linearLayoutManager);
        getData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                toggleData();
            }
        });

        return view;
    }

    public void toggleData() {
        getData();
    }

    private void getData() {
        ArrayList<Article> data = new ArrayList<>();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url+section.toLowerCase(), null, new Response.Listener<JSONObject>() {
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
                                        obj.getString("description"),
                                        obj.getString("artUrl"),
                                        obj.getString("imageUrl"));
                        data.add(article);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.i("Section data", String.valueOf(data));
                adapter.swapItems(data);
                if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                mList.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error getting section data for "+section, error.toString());
                progressLayout.setVisibility(View.GONE);
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragment.OnListFragmentInteractionListener) {
            mListener = (HomeFragment.OnListFragmentInteractionListener) context;
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


}
