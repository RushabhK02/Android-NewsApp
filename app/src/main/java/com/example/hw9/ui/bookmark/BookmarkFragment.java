package com.example.hw9.ui.bookmark;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.Volley;
import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.functionalities.BookmarkCache;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.ui.home.ArticleRecyclerViewAdapter;
import com.example.hw9.ui.home.HomeFragment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;
    private HomeFragment.OnListFragmentInteractionListener mListener;

    private MainActivity activity;

    private RecyclerView mList;
    private TextView progressLayout;

    private GridLayoutManager gridLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<ArticleItem.Article> articleList = new ArrayList<>();
    private BookmarkRecyclerViewAdapter adapter;
    private Context context;
    private BookmarkCache bookmarkCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT)>1?getArguments().getInt(ARG_COLUMN_COUNT):2;

        }

        activity = (MainActivity) getActivity();
        bookmarkCache = activity.getBookmarkCache();
        articleList.addAll(bookmarkCache.getFavorites());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        context = view.getContext();
        mList = view.findViewById(R.id.list_bookmark);
        progressLayout = view.findViewById(R.id.bookmark_text);
        if(articleList.size()>0) progressLayout.setVisibility(View.GONE);

        adapter = new BookmarkRecyclerViewAdapter(articleList, mListener, context, activity, progressLayout);

        gridLayoutManager = new GridLayoutManager(context, mColumnCount);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), gridLayoutManager.getOrientation());
        mList.setHasFixedSize(true);
        mList.setLayoutManager(gridLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);

        return view;
    }

    public void toggleEmpty(){
        progressLayout.setVisibility(View.VISIBLE);
        mList.setVisibility(View.GONE);
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

    @Override
    public void onResume() {
        super.onResume();
        articleList.clear();
        articleList.addAll(bookmarkCache.getFavorites());
        adapter.updateList();
    }
}
