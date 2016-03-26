package org.main.smartmirror.smartmirror;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Constants {


    public static final String TAG = "SmartMirror";

    //-------- Account Activity ---------
    public static final String KEY_STORE = "AndroidKeyStore";
    public static final String PICASA = "https://picasaweb.google.com/data/";
    public static final String REVOKE_ACCOUNT = "https://accounts.google.com/o/oauth2/revoke?token=";
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
    public static final String MIRA_WAKE = "mira wake";
    public static final String MIRA_SLEEP = "mira sleep";
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
    public static final String SHOW_HELP = "show help";
    public static final String TIME = "current time";
    public static final String TWITTER = "twitter";

    // ------------- NEWS ------------
    public static final String BUSINESS = "business";
    public static final String MEDIA = "media";
    public static final String SCIENCE = "science";
    public static final String SPORTS = "sports";
    public static final String TECHNOLOGY = "technology";
    public static final String TRAVEL = "travel";
    public static final String WORLD = "world";
    public static final String NEWS_BODY = "news body";
    public static final String[] NEWS_DESKS = {BUSINESS, MEDIA, SCIENCE, SPORTS, TECHNOLOGY, TRAVEL,
            WORLD};
    public static final List<String> DESK_LIST = Arrays.asList(NEWS_DESKS);
    public static final HashSet<String> DESK_HASH = new HashSet<>(DESK_LIST);

    // ------------ MUSIC ------------------
    public static final String ALTERNATIVE = "play alternative";
    public static final String AMBIENT = "play ambient";
    public static final String CLASSICAL = "play classical";
    public static final String DANCE = "play dance";
    public static final String JAZZ = "play jazz";
    public static final String RAP = "play rap";
    public static final String ROCK = "play rock";
    public static final String[] MUSIC_STATIONS = { ALTERNATIVE, AMBIENT, CLASSICAL, DANCE, JAZZ, RAP, ROCK };
    public static final HashSet<String> MUSIC_HASH = new HashSet<>(Arrays.asList(MUSIC_STATIONS));


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

    public static final String ARTICLE_1 = "article 1";
    public static final String ARTICLE_2 = "article 2";
    public static final String ARTICLE_3 = "article 3";
    public static final String ARTICLE_4 = "article 4";
    public static final String ARTICLE_5 = "article 5";
    public static final String ARTICLE_6 = "article 6";
    public static final String ARTICLE_7 = "article 7";
    public static final String ARTICLE_8 = "article 8";
    public static final String ARTICLE_9 = "article 9";
    public static final String ARTICLE_10 = "article 10";

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


    /** Initialize COMMAND_SET. These are the words the voice recognition will
     *  actively look for when analyzing user speech. Words or phrases not in this list will not
     *  be detected.
     */
    public static final String[] COMMANDS = {
            BUSINESS, CAMERA, CALENDAR, CLOSE_SCREEN, CLOSE_WINDOW,
            CONDITIONS, EIGHT, FACEBOOK, FIVE,
            FORECAST, FOUR, FULL_SCREEN, GALLERY, GMAIL,
            GO_BACK, GO_FORWARD, GO_TO_SLEEP, HELP, HIDE_TIME,
            HIDE_WEATHER, HIDE_SCREEN, MAXIMIZE,
            MEDIA, MENU, MINIMIZE,
            MIRA_SLEEP, MIRA_WAKE, MUSIC, NIGHT_LIGHT,
            NEWS, NEXT, NINE, ONE,
            OPEN_WINDOW, OPTIONS, PAUSE, PLAY, PHOTOS, QUOTES, SCIENCE,
            SCROLL_DOWN, SCROLL_UP, SPORTS,
            SETTINGS, SEVEN, SHOW_LIGHT, SHOW_HELP,
            SHOW_TIME, SHOW_WEATHER, SIX,
            SLEEP, SMALL_SCREEN, STOP, TAKE_PICTURE,
            THREE, TECHNOLOGY, TIME, TRAVEL, TWITTER, TWO,
            WAKE, WAKE_UP, WIDE_SCREEN, WORLD,

            ALTERNATIVE, AMBIENT, CLASSICAL, DANCE, JAZZ, RAP, ROCK,

            Preferences.CMD_REMOTE_OFF, Preferences.CMD_REMOTE_ON,
            Preferences.CMD_ENABLE_REMOTE, Preferences.CMD_DISABLE_REMOTE,
            Preferences.CMD_SOUND_OFF, Preferences.CMD_SOUND_ON, Preferences.CMD_MIRA_SOUND,

            Preferences.CMD_TIME_24HR, Preferences.CMD_TIME_12HR,
            Preferences.CMD_VOICE_OFF, Preferences.CMD_VOICE_ON,
            Preferences.CMD_WEATHER_ENGLISH, Preferences.CMD_WEATHER_METRIC,

            Preferences.CMD_STAY_AWAKE
    };

    public static HashSet<String> createCommandSet() {

        List<String> commandList = Arrays.asList(COMMANDS);
        return new HashSet<>(commandList);
    }

    public static final Set<String> COMMAND_SET = createCommandSet();
}
