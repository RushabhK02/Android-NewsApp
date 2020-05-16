package com.example.hw9.ui.home;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.WeatherCard;
import com.example.hw9.ui.home.HomeFragment.OnListFragmentInteractionListener;
import com.example.hw9.model.ArticleItem.Article;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.example.hw9.model.ArticleItem.Article} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ArticleRecyclerViewAdapter extends RecyclerView.Adapter<ArticleRecyclerViewAdapter.ViewHolder> {

    private List<ArticleItem.Article> mValues;
    private WeatherCard weather;
    private final OnListFragmentInteractionListener mListener;
    private final MainActivity activity;
    private Context context;
    private RecyclerView.Adapter adapter;
    private static HashMap<String, Integer> imageMapping = new HashMap<String, Integer>();

    final Transformation atransformation = new RoundedCornersTransformation(45, 0, RoundedCornersTransformation.CornerType.LEFT);
    final Transformation wtransformation = new RoundedCornersTransformation(50, 0);

    static {
        imageMapping.put("clouds", R.drawable.cloudy_weather);
        imageMapping.put("clear", R.drawable.clear_weather);
        imageMapping.put("snow", R.drawable.snowy_weather);
        imageMapping.put("thunderstorm", R.drawable.thunder_weather);
        imageMapping.put("rain", R.drawable.rainy_weather);
        imageMapping.put("drizzle", R.drawable.rainy_weather);
        imageMapping.put("rain/drizzle", R.drawable.rainy_weather);
        imageMapping.put("default", R.drawable.sunny_weather);
    }
        @Override
        public int getItemViewType(int position) {
            if(position==0) return 0;
            return 1;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(adapter==null) adapter = this;
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewType==1?R.layout.fragment_article: R.layout.fragment_weather, parent, false);
            switch (viewType) {
                case 0: return new WeatherViewHolder(view);
                default: return new ArticleViewHolder(view);
            }
        }

    @Override
        public void onBindViewHolder(final ArticleRecyclerViewAdapter.ViewHolder holder,int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    final WeatherViewHolder vh = (WeatherViewHolder) holder;
                    vh.mWeather = weather;
                    if(weather!=null) vh.mForecast.setText(weather.forecast);
                    if(weather!=null) vh.mTemp.setText(weather.temp+"\u00B0"+ "C");
                    if(weather!=null) vh.mCity.setText(weather.city);
                    if(weather!=null) vh.mState.setText(weather.state);
                    if(weather!=null)
//                        Picasso.get()
                        Picasso.with(context)
                            .load(imageMapping.containsKey(weather.forecast.toLowerCase())?
                                    imageMapping.get(weather.forecast.toLowerCase()):imageMapping.get("default"))
                            .transform(wtransformation)
                            .fit()
                            .into(vh.mImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
//                                    Picasso.get()
                                    Picasso.with(context)
                                            .load(imageMapping.get("default"))
                                            .transform(wtransformation)
                                            .fit()
                                            .into(vh.mImage);
                                }
                            });
                    else
//                        Picasso.get()
                    Picasso.with(context)
                            .load(imageMapping.get("default"))
                            .transform(wtransformation)
                            .fit()
                            .into(vh.mImage);
                    break;
                }
                case 1: {
                    final ArticleViewHolder viewHolder = (ArticleViewHolder) holder;
                    if(position==0) position = position+1;
                    viewHolder.setmItem(mValues.get(position-1));
                    viewHolder.mTitle.setText(mValues.get(position-1).title);
                    viewHolder.mTime.setText(mValues.get(position-1).timeAgo);
                    String upperString = mValues.get(position-1).section.substring(0, 1).toUpperCase() + mValues.get(position-1).section.substring(1).toLowerCase();
                    mValues.get(position-1).section = upperString;
                    viewHolder.mTag.setText(upperString);
//                    Picasso.get()
                    Picasso.with(context)
                            .load(mValues.get(position-1).imageUrl)
                            .transform(atransformation)
                            .fit()
                            .into(viewHolder.mImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
//                                    Picasso.get()
                                    Picasso.with(context)
                                            .load(R.drawable.default_guardian)
                                            .transform(atransformation)
                                            .fit()
                                            .into(viewHolder.mImage);
                                }
                            });

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mListener) {
                                mListener.onListFragmentInteraction(viewHolder.mItem, false, adapter, false);
                            }
                        }
                    });
                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener(){
                        @Override
                        public boolean onLongClick(View v) {
                            if (null != mListener) {
                                mListener.onListFragmentInteraction(viewHolder.mItem, true, adapter, false);
                                return true;
                            }
                            return false;
                        }
                    });
                    break;
                }
            }
        }

    public ArticleRecyclerViewAdapter(List<ArticleItem.Article> items, OnListFragmentInteractionListener listener,
                                      WeatherCard weather, Context context, MainActivity activity) {
        mValues = items;
        mListener = listener;
        this.weather = weather;
        this.context = context;
        this.activity = activity;
    }

    public void swapItems(List<Article> items, WeatherCard weather) {
        mValues = items;
        this.weather = weather;
        this.notifyDataSetChanged();
    }

    public void notifyChange(){
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size()+(weather==null?0:1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImage;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImage = (ImageView) view.findViewById(R.id.item_image);
            }


        @Override
            public String toString() {
                return super.toString();
            }
    }

    class WeatherViewHolder extends ViewHolder {
        public final View mView;
        public final TextView mCity;
        public final TextView mState;
        public final TextView mForecast;
        public final TextView mTemp;

        public WeatherCard mWeather;

        public WeatherViewHolder(View view) {
            super(view);
            mView = view;
            mCity = view.findViewById(R.id.city_name);
            mState = view.findViewById(R.id.state_name);
            mForecast = view.findViewById(R.id.forecast);
            mTemp = view.findViewById(R.id.temperature);
        }

    }

    class ArticleViewHolder extends ViewHolder {
        public final View mView;
        public final ImageView mbookmark;
        public final TextView mTitle;
        public final TextView mTag;
        public final TextView mTime;
        public boolean flag=false; //check with bookmarked articles
        public Article mItem;

        public ArticleViewHolder(View view) {
            super(view);
            mView = view;
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

        public void setmItem(Article article){
            mItem = article;
            flag = activity.getBookmarkCache().contains(article);
            toggleBookmark();
        }

        public void toggleBookmark() {
            mbookmark.setImageResource(!flag?R.drawable.ic_bookmark_border_red_24dp:R.drawable.ic_bookmark_red_fill_24dp);
        }
    }
}
