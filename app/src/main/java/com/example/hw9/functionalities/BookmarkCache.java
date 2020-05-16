package com.example.hw9.functionalities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookmarkCache implements Serializable {
    private static BookmarkCache INSTANCE = null;
    private static boolean refreshFlag = false; //false - need to refresh, true - refreshed
    private static boolean commitFlag = false;
    private HashMap<String, Article> articles;
    private JSONObject articleData;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @NonNull
    @Override
    public String toString() {
        Log.i("Bookmark hashmap", String.valueOf(articles));
        return articles.toString();
    }

    private BookmarkCache(Activity activity) {
        try {
            preferences = activity.getApplicationContext().getSharedPreferences("bookmark", 0);
            editor = preferences.edit();
            String obj = preferences.getString("articles", "");
            articles = new HashMap<>();
            if(obj.length()>0) {
                articleData = new JSONObject(obj);

                for (int i = 0; i < articleData.names().length(); i++) {
                    String key = articleData.names().getString(i);
                    Article article = getArticle(articleData.getJSONObject(key));
                    articles.put(key, article);
                }
            }
            refreshFlag = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static BookmarkCache getInstance(Activity activity) {
        if (INSTANCE == null) {
            INSTANCE = new BookmarkCache(activity);
        }
        return(INSTANCE);
    }

    public boolean contains(Article obj){
        return INSTANCE.articles.containsKey(obj.artId);
    }

    public boolean toggleBookmark(Article article){
        refreshFlag = false;
        commitFlag = false;
        if(articles.containsKey(article.artId)) {
            INSTANCE.articles.remove(article.artId);
            return false;
        } else {
            INSTANCE.articles.put(article.artId, article);
            return true;
        }
    }

    public boolean commitChanges(){
        if(commitFlag) return true;

        INSTANCE.articleData = new JSONObject();

        for(String id : INSTANCE.articles.keySet()) {
            JSONObject json = new JSONObject();
            Article article = INSTANCE.articles.get(id);
            try {
                json.put("artId", article.artId);
                json.put("artUrl", article.artUrl);
                json.put("imageUrl", article.imageUrl);
                json.put("pubDate", article.pubDate);
                json.put("description", article.description);
                json.put("section", article.section);
                json.put("title", article.title);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            try {
                INSTANCE.articleData.put(id, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("articles", INSTANCE.articleData.toString());
//        editor.apply();
        editor.commit();
        commitFlag = true;
        return true;
    }

    public List<Article> getFavorites() {
        return new ArrayList<Article>(articles.values());
    }

    public void refresh() {
        if(refreshFlag) return;
        try {
            String obj = preferences.getString("articles", "");
            articles = new HashMap<>();
            if(obj.length()>0) {
                articleData = new JSONObject(obj);

                for (int i = 0; i < articleData.names().length(); i++) {
                    String key = articleData.names().getString(i);
                    Article article = getArticle(articleData.getJSONObject(key));
                    articles.put(key, article);
                }
            }
            refreshFlag = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Article getArticle(JSONObject object){
        try {
        Article article = new Article(
                object.getString("artId"),
                object.getString("section"),
                object.getString("title"),
                object.getString("pubDate"),
                object.getString("description"),
                object.getString("artUrl"),
                object.getString("imageUrl")
        );
//        article.calcTimeAgo();
        return article;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
