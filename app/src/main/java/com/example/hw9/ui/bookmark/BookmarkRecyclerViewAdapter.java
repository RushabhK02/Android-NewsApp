package com.example.hw9.ui.bookmark;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.ui.home.HomeFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static com.example.hw9.model.ArticleItem.*;

public class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {

    private List<Article> mValues;
    private final HomeFragment.OnListFragmentInteractionListener mListener;
    private Context context;
    private RecyclerView.Adapter adapter;
    private final MainActivity activity;
    private TextView toggleEmpty;

    public BookmarkRecyclerViewAdapter(List<Article> items, HomeFragment.OnListFragmentInteractionListener listener,
                                       Context context, MainActivity activity, TextView emptyText) {
        mValues = items;
        mListener = listener;
        this.context = context;
        this.activity = activity;
        toggleEmpty = emptyText;
        adapter = this;
    }

//    @Override
//    public int getItemViewType(int position) {
//        if(mValues.isEmpty()) return 0;
//        return 1;
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_bookmark_item, parent, false);
            return new ArticleHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, int position) {
            ArticleHolder holder = (ArticleHolder) vh;
            holder.setmItem(mValues.get(position));
            holder.mTitle.setText(mValues.get(position).title);
            holder.mDate.setText(calcDate(mValues.get(position).pubDate));
            String upperString = mValues.get(position).section.substring(0, 1).toUpperCase() + mValues.get(position).section.substring(1).toLowerCase();
            holder.mTag.setText(upperString);
//            Picasso.get()
            Picasso.with(context)
                    .load(mValues.get(position).imageUrl)
                    .fit()
                    .transform(new RoundedCornersTransformation(30, 0, RoundedCornersTransformation.CornerType.TOP))
                    .into(holder.mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
//                            Picasso.get()
                            Picasso.with(context)
                                    .load(R.drawable.default_guardian)
                                    .fit()
                                    .transform(new RoundedCornersTransformation(30, 0, RoundedCornersTransformation.CornerType.TOP))
                                    .into(holder.mImage);
                        }
                    });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(holder.mItem, false, adapter, true);
                    }
                }
            });
//            holder.infoLine.setOnClickListener() {
//
//            };
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(holder.mItem, true, adapter, true);
                        return true;
                    }
                    return false;
                }
            });
    }

    public void removeItem(Article article){
        mValues.remove(article);
        notifyDataSetChanged();
        toggleEmpty.setVisibility(mValues.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void updateList(){
        mValues = activity.getBookmarkCache().getFavorites();
//        mValues = items;
        toggleEmpty.setVisibility(mValues.size() > 0 ? View.GONE : View.VISIBLE);
        this.notifyDataSetChanged();
    }

//    @Override
//    public int getItemCount() {
//            return mValues.size();
//    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public String calcDate(String pubDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime ldt = LocalDateTime.parse(pubDate,formatter);
        ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
        ZonedDateTime zdt = ldt.atZone( zoneId );

        String date = DateTimeFormatter.ofPattern("dd MMM").format(zdt);
        return date;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class ArticleHolder extends ViewHolder {

        public final View mView;
//        public final View infoLine;
        public final ImageView mImage;
        public final ImageView mbookmark;
        public final TextView mTitle;
        public final TextView mTag;
        public final TextView mDate;
        private boolean flag = false; //Check with bookmarked articles
        public Article mItem;


        public ArticleHolder(View view) {
            super(view);
            mView = view;
//            infoLine = view.findViewById(R.id.info_line);
            mImage = view.findViewById(R.id.article_img);
            mbookmark = view.findViewById(R.id.bookmark_btn);
            mTitle = view.findViewById(R.id.article_title);
            mDate = view.findViewById(R.id.article_date);
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

        public void setmItem(Article article){
            mItem = article;
            flag = activity.getBookmarkCache().contains(article);
            toggleBookmark();
        }

        public void toggleBookmark() {
            mbookmark.setImageResource(!flag?R.drawable.ic_bookmark_border_red_24dp:R.drawable.ic_bookmark_red_fill_24dp);
            if(!flag) removeItem(mItem);
        }
    }

    public class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
