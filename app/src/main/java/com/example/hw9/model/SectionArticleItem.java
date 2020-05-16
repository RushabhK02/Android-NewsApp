package com.example.hw9.model;

import android.text.format.DateUtils;
import android.util.Log;

import com.example.hw9.model.ArticleItem.Article;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SectionArticleItem {
    public static final List<Article> ITEMS = new ArrayList<Article>();

    public static final Map<String, Article> ITEM_MAP = new HashMap<String, Article>();

    private static final int COUNT = 10;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Article item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.artId, item);
    }

    private static Article createDummyItem(int position) {
        return new Article(String.valueOf(position), "Item " + position, makeDetails(position), "Item " + position, makeDetails(position)
                , "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
