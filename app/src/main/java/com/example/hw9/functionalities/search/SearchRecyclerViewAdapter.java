package com.example.hw9.functionalities.search;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;
import com.example.hw9.model.SectionArticleItem;
import com.example.hw9.ui.home.HomeFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<com.example.hw9.functionalities.search.SearchRecyclerViewAdapter.ViewHolder> {

    private List<ArticleItem.Article> mValues;
    private final HomeFragment.OnListFragmentInteractionListener mListener;
    private Context context;
    private RecyclerView.Adapter adapter;
    private final SearchResultActivity activity;

    final Transformation transformation = new RoundedCornersTransformation(45, 0, RoundedCornersTransformation.CornerType.LEFT);

    public SearchRecyclerViewAdapter(List<ArticleItem.Article> items, HomeFragment.OnListFragmentInteractionListener listener,
                                      Context context, SearchResultActivity activity) {
        mValues = items;
        mListener = listener;
        this.context = context;
        this.activity = activity;
        adapter = this;
    }

    @Override
    public com.example.hw9.functionalities.search.SearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_article, parent, false);
        return new com.example.hw9.functionalities.search.SearchRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final com.example.hw9.functionalities.search.SearchRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.setmItem(mValues.get(position));
        holder.mTitle.setText(mValues.get(position).title);
        holder.mTime.setText(mValues.get(position).timeAgo);
        String upperString = mValues.get(position).section.substring(0, 1).toUpperCase() + mValues.get(position).section.substring(1).toLowerCase();
        mValues.get(position).section = upperString;
        holder.mTag.setText(upperString);
//        Picasso.get()
        Picasso.with(context)
                .load(mValues.get(position).imageUrl)
                .fit()
                .transform(transformation)
                .into(holder.mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
//                        Picasso.get()
                        Picasso.with(context)
                                .load(R.drawable.default_guardian)
                                .fit()
                                .transform(transformation)
                                .into(holder.mImage);
                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, false, adapter, false);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, true, adapter, false);
                    return true;
                }
                return false;
            }
        });
    }

    public void swapItems(ArrayList<ArticleItem.Article> items){
        this.mValues = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImage;
        public final ImageView mbookmark;
        public final TextView mTitle;
        public final TextView mTag;
        public final TextView mTime;
        private boolean flag = false; //Check with bookmarked articles
        public ArticleItem.Article mItem;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = view.findViewById(R.id.item_image);
            mbookmark = view.findViewById(R.id.bookmark_btn);
            mTitle = view.findViewById(R.id.article_title);
            mTime = view.findViewById(R.id.article_time);
            mTag = view.findViewById(R.id.article_tag);
            mbookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.i("Bookmark clicked", "PING!!");
                    flag = activity.getBookmarkCache().toggleBookmark(mItem);
//                    flag = !flag;
                    toggleBookmark();
                    if(flag) Toast.makeText(context, "\""+mItem.title+"\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(context, "\""+mItem.title+"\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void setmItem(ArticleItem.Article article){
            mItem = article;
            flag = activity.getBookmarkCache().contains(article);
            toggleBookmark();
        }

        public void toggleBookmark() {
            mbookmark.setImageResource(!flag?R.drawable.ic_bookmark_border_red_24dp:R.drawable.ic_bookmark_red_fill_24dp);
        }
    }
}
