package org.main.smartmirror.smartmirror;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Constants {


    public static final String TAG = "SmartMirror";

    //-------- Account Activity ---------
    public static final String KEY_STORE = "AndroidKeyStore";
    public static final String PICASA = "https://picasaweb.google.com/data/";
    public static final String GMAIL_PROVIDER_PERMISSION = "com.google.android.gm.permission.READ_CONTENT_PROVIDER";

    //-------------- DISPLAY -----------
    public static final String CLOSE_WINDOW = "close window";
    public static final String CLOSE_SCREEN = "close screen";
    public static final String FULL_SCREEN = "full screen";
    public static final String GO_BACK = "go back";
    public static final String GO_FORWARD = "go forward";
    public static final String GO_TO_SLEEP = "go to sleep";
    public static final String HIDE_SCREEN = "hide screen";
    public static final String MAXIMIZE = "maximize";
    public static final String MENU = "menu";
    public static final String MINIMIZE = "minimize";
    public static final String MIRA_LISTEN = "mira listen";
    public static final String MIRA_SLEEP = "mira sleep";
    public static final String MIRA_WAKE = "mira wake";
    public static final String MIRROR_MIRROR = "mirror mirror on the wall";
    public static final String OPEN_WINDOW = "open window";
    public static final String OPTIONS = "options";
    public static final String PAUSE = "pause music";
    public static final String PLAY = "play music";
    public static final String SCROLL_UP = "scroll up";
    public static final String SCROLL_DOWN = "scroll down";
    public static final String SLEEP = "sleep";
    public static final String SMALL_SCREEN = "small screen";
    public static final String STOP = "stop music";
    public static final String WAKE = "wake";
    public static final String WAKE_UP = "wake up";
    public static final String WIDE_SCREEN = "wide screen";

    //--------------FACEBOOK-------------
    public static final String FACEBOOK_URL = "https://m.facebook.com/";
    public static final String FACEBOOK_SMARTMIRROR = "https://m.facebook.com/Smart-Mirror-384754131698096";

    //--------------FRAGMENTS------------
    public static final String CALENDAR = "calendar";
    public static final String CAMERA = "camera";
    public static final String FACEBOOK = "facebook";
    public static final String FORECAST = "forecast";
    public static final String GALLERY = "gallery";
    public static final String GMAIL = "gmail";
    public static final String LIGHT = "light";
    public static final String MUSIC = "music";
    public static final String NEWS = "news";
    public static final String NIGHT_LIGHT = "night light";
    public static final String PHOTOS = "photos";
    public static final String QUOTES = "quotes";
    public static final String SETTINGS = "settings";
    public static final String SHOW_LIGHT = "show light";
    public static final String TIME = "current time";
    public static final String TWITTER = "twitter";

    // ------------- NEWS ------------
    public static final String BOOKS = "books";
    public static final String BUSINESS = "business";
    public static final String ECONOMICS = "economics";
    public static final String ENVIRONMENT = "environment";
    public static final String FASHION = "fashion";
    public static final String GAMES = "games";
    public static final String LIFESTYLE = "lifestyle";
    public static final String MEDIA = "media";
    public static final String MOVIES = "movies";
    public static final String OPINION = "opinion";
    public static final String SCIENCE = "science";
    public static final String SPORTS = "sports";
    public static final String TECHNOLOGY = "technology";
    public static final String TRAVEL = "travel";
    public static final String WORLD = "world";

    public static final String NEWS_BODY = "news body";
    public static final String[] NEWS_DESKS = {BUSINESS, BOOKS, ECONOMICS, ENVIRONMENT, FASHION,
            GAMES, LIFESTYLE, MEDIA, MOVIES, OPINION, SCIENCE, SPORTS, TECHNOLOGY, TRAVEL, WORLD};
    public static final List<String> DESK_LIST = Arrays.asList(NEWS_DESKS);
    public static final HashSet<String> DESK_HASH = new HashSet<>(DESK_LIST);

    // ------------ MUSIC ------------------
    // music commands used by voice recognition. these will only work while the music player is paused or stopped.
    public static final String ALTERNATIVE = "play alternative";
    public static final String AMBIENT = "play ambient";
    public static final String CLASSICAL = "play classical";
    public static final String DANCE = "play dance";
    public static final String JAZZ = "play jazz";
    public static final String RAP = "play rap";
    public static final String ROCK = "play rock";

    // music commands sent by the remote - these will trigger stations to change while stream is active.
    public static final String R_ALTERNATIVE = "remote play alternative";
    public static final String R_AMBIENT = "remote play ambient";
    public static final String R_CLASSICAL = "remote play classical";
    public static final String R_DANCE = "remote play dance";
    public static final String R_JAZZ = "remote play jazz";
    public static final String R_RAP = "remote play rap";
    public static final String R_ROCK = "remote play rock";

    //--------------HELP-----------------
    public static final String HELP = "help";

    //--------------WEATHER--------------
    public static final String CONDITIONS = "conditions";
    public static final String SHOW_WEATHER = "show weather";
    public static final String HIDE_WEATHER = "hide weather";
    public static final String SHOW_TIME = "show time";
    public static final String HIDE_TIME = "hide time";

    //--------------CAMERA---------------
    public static final String TAKE_PICTURE = "take a picture";

    //--------------TRAFFIC--------------
    public static final String DISTANCE_MATRIX_API =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&departure_time=now&traffic_model=best_guess&units=%s&key=%s";

    //--------------TWITTER--------------
    public static final String TWITTER_CONSUMER_KEY = "W9cvqANm0cXASr4MyzIUKDiaK";
    public static final String TWITTER_CONSUMER_SECRET = "WP9HyhszNP2x4Y9WGPtF5DfXT23v55O515PrukTCpkewuuNngT";

    //--------------NEWS--------------
    public static final String ONE = "article one";
    public static final String TWO = "article two";
    public static final String THREE = "article three";
    public static final String FOUR = "article four";
    public static final String FIVE = "article five";
    public static final String SIX = "article six";
    public static final String SEVEN = "article seven";
    public static final String EIGHT = "article eight";
    public static final String NINE = "article nine";
    public static final String TEN = "article ten";

    public static final String REMOTE_ARTICLE_1 = "article 1";
    public static final String REMOTE_ARTICLE_2 = "article 2";
    public static final String REMOTE_ARTICLE_3 = "article 3";
    public static final String REMOTE_ARTICLE_4 = "article 4";
    public static final String REMOTE_ARTICLE_5 = "article 5";
    public static final String REMOTE_ARTICLE_6 = "article 6";
    public static final String REMOTE_ARTICLE_7 = "article 7";
    public static final String REMOTE_ARTICLE_8 = "article 8";
    public static final String REMOTE_ARTICLE_9 = "article 9";
    public static final String REMOTE_ARTICLE_10 = "article 10";

    //--------------GMAIL-----------------
    public static final String NEXT = "next";

    //--------------- Remote Control Specific Commands --------------------
    public static final String REMOTE_INCREASE_SCREEN ="increase screen size";
    public static final String REMOTE_TOGGLE_SLEEPS_STATE = "toggle wake";
    public static final String REMOTE_TOGGLE_LISTENING = "toggle listening";
    public static final String REMOTE_TOGGLE_SOUND = "toggle sound";
    public static final String REMOTE_TOGGLE_TIME_VISIBLE = "time visible";
    public static final String REMOTE_TOGGLE_WEATHER_VISIBLE = "weather visible";
    public static final String REMOTE_TOGGLE_TIME_FORMAT = "toggle time format";
    public static final String REMOTE_TOGGLE_WEATHER_FORMAT = "toggle weather format";

}
