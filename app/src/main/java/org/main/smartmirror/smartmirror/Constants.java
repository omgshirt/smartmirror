package org.main.smartmirror.smartmirror;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Constants {


    public static final String TAG = "SmartMirror";
    //-------------- DISPLAY -----------
    public static final String BACK = "back";
    public static final String CLOSE_WINDOW = "close window";
    public static final String CLOSE_SCREEN = "close screen";
    public static final String FULL_SCREEN = "full screen";
    public static final String GO_BACK = "go back";
    public static final String GO_TO_SLEEP = "go to sleep";
    public static final String HIDE_SCREEN = "hide screen";
    public static final String MAXIMIZE = "maximize";
    public static final String MENU = "menu";
    public static final String MINIMIZE = "minimize";
    public static final String OPTIONS = "options";
    public static final String SLEEP = "sleep";
    public static final String SMALL_SCREEN = "small screen";
    public static final String WAKE = "wake";
    public static final String WAKE_UP = "wake up";
    public static final String WIDE_SCREEN = "wide screen";

    //--------------FRAGMENTS------------
    public static final String CALENDAR = "calendar";
    public static final String CAMERA = "camera";
    public static final String FACEBOOK = "facebook";
    public static final String GALLERY = "gallery";
    public static final String NEWS = "news";
    public static final String NIGHT_LIGHT = "night light";
    public static final String PHOTOS = "photos";
    public static final String QUOTES = "quotes";
    public static final String SETTINGS = "settings";
    public static final String TIME = "current time";
    public static final String TRAFFIC = "traffic";
    public static final String TWITTER = "twitter";

    // ------------- NEWS ------------
    public static final String BUSINESS = "business";
    public static final String MEDIA = "media";
    public static final String SCIENCE = "science";
    public static final String SPORTS = "sports";
    public static final String TECHNOLOGY = "technology";
    public static final String TRAVEL = "travel";
    public static final String WORLD = "world";

    public static final String NEWS_BODY= "news body";

    public static final String[] NEWS_DESKS = { BUSINESS, MEDIA, SCIENCE, SPORTS, TECHNOLOGY, TRAVEL,
                                WORLD };

    public static final List<String> DESK_LIST = Arrays.asList(NEWS_DESKS);
    public static final HashSet<String> DESK_HASH = new HashSet<>(DESK_LIST);

    //--------------HELP-----------------
    public static final String HELP = "help";

    //--------------WEATHER--------------
    public static final String CONDITIONS = "conditions";
    public static final String FORECAST = "forecast";
    public static final String SHOW_WEATHER = "show weather";
    public static final String HIDE_WEATHER = "hide weather";
    public static final String SHOW_TIME = "show time";
    public static final String HIDE_TIME = "hide time";

    //--------------CAMERA---------------
    public static final String TAKE_PICTURE="take a picture";

    //--------------TWITTER--------------
    public static final String TWITTER_CONSUMER_KEY = "mQ51h9ZbAz9Xk2AZtsUBJAGlx"; // from twitter
    public static final String TWITTER_CONSUMER_SECRET= "uSRCxg6AqE9DyIiuKjVD2ZzKC7CsGmuUcEljx2yafBwYHW74Rt"; // from twitter
    public static final String TWITTER_FABRIC_CONSUMER_KEY = "GAZWTz0pd3bduld0PzZMexSrH"; // from fabric, enabled for guest auth
    public static final String TWITTER_FABRIC_CONSUMER_SECRET= "oUtQGP4NX9L9ZGVZ8mvYSqaJ3dJCzMDtjLcojybwrT9PL9keud"; // from fabric, enabled for guest auth
    public static final String mLogin= "log me in";
    public static final String mGet= "get";
    public static final String mRefresh= "refresh";

    //--------------FACEBOOK-------------
    public static final String SCROLL_UP ="scroll up";
    public static final String SCROLL_DOWN ="scroll down";

    //--------------NEWS--------------
    public static final String ONE = "one";
    public static final String FIRST = "first";
    public static final String TWO = "two";
    public static final String SECOND = "second";
    public static final String THREE = "three";
    public static final String THIRD = "third";
    public static final String FOUR = "four";
    public static final String FOURTH = "fourth";
    public static final String FIVE = "five";
    public static final String FIFTH = "fifth";
    public static final String SIX = "six";
    public static final String SIXTH = "sixth";
    public static final String SEVEN = "seven";
    public static final String SEVENTH = "seventh";
    public static final String EIGHT = "eight";
    public static final String EIGHTH = "eighth";
    public static final String NINE = "nine";
    public static final String NINTH = "ninth";


    // used to initialize COMMAND_SET
    public static final String[] COMMANDS = {
            BACK, BUSINESS,
            CAMERA, CALENDAR, CLOSE_SCREEN, CLOSE_WINDOW,
            CONDITIONS, EIGHT, EIGHTH, FACEBOOK, FIVE,
            FORECAST, FOUR, FULL_SCREEN, GALLERY,
            GO_BACK, GO_TO_SLEEP, HELP, HIDE_TIME,
            HIDE_WEATHER, HIDE_SCREEN, MAXIMIZE,
            MEDIA, MENU, MINIMIZE, NIGHT_LIGHT,
            NEWS, NINE, NINTH,
            ONE, OPTIONS, PHOTOS, QUOTES, SCIENCE,
            SCROLL_DOWN, SCROLL_UP, SPORTS, SECOND,
            SETTINGS, SEVEN, SEVENTH,
            SHOW_TIME, SHOW_WEATHER, SIX,
            SIXTH, SLEEP, SMALL_SCREEN, TAKE_PICTURE,
            THIRD, THREE, TECHNOLOGY, TIME,
            TRAFFIC, TRAVEL, TWITTER, TWO,
            WAKE, WAKE_UP, WIDE_SCREEN, WORLD,
            "black", "blue", "gray", "green",
            "magenta", "orange", "purple",
            "red", "yellow", "white",
            "first", "second", "third", "fourth",
            "fifth", "sixth", "seventh", "eighth",
            Preferences.CMD_CAMERA_OFF, Preferences.CMD_CAMERA_ON,

            Preferences.CMD_LIGHT_HIGH, Preferences.CMD_LIGHT_LOW,
            Preferences.CMD_LIGHT_MEDIUM, Preferences.CMD_LIGHT_VHIGH,
            Preferences.CMD_LIGHT_VLOW,

            Preferences.CMD_REMOTE_OFF, Preferences.CMD_REMOTE_ON,

            Preferences.CMD_SCREEN_HIGH, Preferences.CMD_SCREEN_LOW,
            Preferences.CMD_SCREEN_MEDIUM, Preferences.CMD_SCREEN_VHIGH,
            Preferences.CMD_SCREEN_VLOW,

            Preferences.CMD_SPEECH_OFF, Preferences.CMD_SPEECH_VLOW,
            Preferences.CMD_SPEECH_LOW, Preferences.CMD_SPEECH_MEDIUM,
            Preferences.CMD_SPEECH_HIGH, Preferences.CMD_SPEECH_VHIGH,

            Preferences.CMD_SPEECH_RARE,
            Preferences.CMD_SPEECH_FREQUENT, Preferences.CMD_SPEECH_ALWAYS,

            Preferences.CMD_TIME_24HR, Preferences.CMD_TIME_12HR,
            Preferences.CMD_VOICE_OFF, Preferences.CMD_VOICE_ON,
            Preferences.CMD_WEATHER_ENGLISH, Preferences.CMD_WEATHER_METRIC
    };

    public static HashSet<String> createCommandSet(){

        List<String> commandList = Arrays.asList(COMMANDS);
        return  new HashSet<>(commandList);
    }

    public static final Set<String> COMMAND_SET = createCommandSet();

    //--------------TRAFFIC--------------
    public static final String DISTANCE_MATRIX_API =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&departure_time=now&traffic_model=best_guess&units=%s&key=%s";
}
