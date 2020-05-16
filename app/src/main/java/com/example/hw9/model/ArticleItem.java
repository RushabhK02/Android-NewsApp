package com.example.hw9.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ArticleItem {

    public static final List<Article> ITEMS = new ArrayList<Article>();

    public static final Map<String, Article> ITEM_MAP = new HashMap<String, Article>();

    private static final int COUNT = 10;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Article item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.artId, item);
    }

    private static Article createDummyItem(int position) {
        return new Article(String.valueOf(position), "Item " + position, "Item " + position, makeDetails(position),
                makeDetails(position),"Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Article implements Serializable {
        public String artId;
        public String section;
        public String title;
        public String pubDate;
        public String timeAgo;
        public String artUrl;
        public String imageUrl;
        public String description;

        public Article(String id, String section, String title, String pubDate, String desc, String artUrl, String imageUrl) {
            this.artId = id;
            this.section = section;
            this.title = title;
            this.pubDate = pubDate;
            this.description = desc;
            this.artUrl = artUrl;
            this.imageUrl = imageUrl;
            formattedDate();
//            calcTimeAgo();
        }


       public void formattedDate() {
            try
            {
                LocalDateTime ldt = LocalDateTime.parse(pubDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                ZoneId zoneId = ZoneId.of("America/Los_Angeles");
                ZonedDateTime zdt = ldt.atZone(zoneId);

                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(zdt);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date past = format.parse(date);
                Date now = new Date();
                long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                long minutes= TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                long hours= TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                long days= TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                if(seconds<60)
                {
                    timeAgo = seconds+"s ago";
//                    Log.i("formattedDate: ",seconds+"s ago");
                }
                else if(minutes<60)
                {
                    timeAgo = minutes+"m ago";
//                    Log.i("formattedDate: ",minutes+"m ago");
                }
                else if(hours<24)
                {
                    timeAgo = hours+"h ago";
//                    Log.i("formattedDate: ",hours+"h ago");
                }
                else
                {
                    timeAgo = days+"d ago";
//                    Log.i("formattedDate: ",days+"d ago");
                }
            }
            catch (Exception j){
                j.printStackTrace();
            }
        }

//        private int daysBetween() {
//            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
//            LocalDateTime ldt = LocalDateTime.parse(pubDate, dtFormatter);
//            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
//            ZonedDateTime zdt = ldt.atZone(zoneId);
//
//            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(zdt);
//
//            org.joda.time.format.DateTimeFormatter formatter =  DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
//            org.joda.time.DateTime datetime = formatter.parseDateTime(date);
//
//            LocalDate localDate = new org.joda.time.LocalDateTime(datetime).toLocalDate();
//            LocalDate localDateNow = new org.joda.time.LocalDateTime().toLocalDate();
//
//            int diff = Days.daysBetween(localDate, localDateNow).getDays();
//            return diff;
//        }

//        public void calcTimeAgo(){
////            int days = 1;
//            int days = daysBetween();
//            if(days >= 7){
//                timeAgo = String.valueOf(days)+"d ago";
//            } else {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
//                LocalDateTime ldt = LocalDateTime.parse(pubDate, formatter);
//                ZoneId zoneId = ZoneId.of("America/Los_Angeles");
//                ZonedDateTime zdt = ldt.atZone(zoneId);
//
//                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(zdt);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                long time = 0;
//                try {
//                    time = sdf.parse(date).getTime();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                long now = System.currentTimeMillis();
//                CharSequence ago =
//                        DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS);
//                timeAgo = ago.toString();
//                formatTimeAgo();
//            }
//        }

//        public void formatTimeAgo(){
//            String answer = timeAgo.replace(" hours","h")
//                    .replace(" hour", "h")
//                    .replace(" seconds", "s")
//                    .replace(" second", "s")
//                    .replace(" minutes", "m")
//                    .replace(" minute", "m")
//                    .replace(" days", "d")
//                    .replace(" day", "d")
//                    .replace("Yesterday", "1d ago")
//                    .replace("yesterday", "1d ago");
//
//            timeAgo = answer;
////            Log.i("Time ago", answer);
//        }

        @Override
        public String toString() {
            return "Article{" +
                    "artId='" + artId + '\'' +
                    ", section='" + section + '\'' +
                    ", title='" + title + '\'' +
                    ", pubDate='" + pubDate + '\'' +
                    ", artUrl='" + artUrl + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    '}';
        }
    }

    public static class WeatherCard {
        public String city;
        public String state;
        public String temp;
        public String forecast;

        public WeatherCard(String city, String state, String temp, String forecast) {
            this.city = city;
            this.state = state;
            this.temp = String.valueOf(Math.round(Double.parseDouble(temp)));
            this.forecast = forecast;
        }

        @Override
        public String toString() {
            return "WeatherCard{" +
                    "city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    ", temp='" + temp + '\'' +
                    ", forecast='" + forecast + '\'' +
                    '}';
        }
    }
}
