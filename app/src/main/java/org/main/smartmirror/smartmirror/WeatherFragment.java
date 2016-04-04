package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Fragment that displays the weather information
 * <p/>
 * Commands :
 * "forecast" speaks the 3-day forecast
 * "conditions" speaks the current conditions
 */
public class WeatherFragment extends Fragment implements CacheManager.CacheListener {

    Typeface weatherFont;
    Preferences mPreferences;

    private LinearLayout layTimeLayout;
    private LinearLayout layWeatherLayout;
    private TextView txtWeatherIcon;
    private TextClock clkTime;
    private TextClock clkDate;
    private TextView txtCurrentTemp;
    private TextView txtCurrentWind;
    private TextView txtCurrentHumidity;
    private TextView txtDailyHigh;
    private TextView txtDailyLow;
    private TextView txtAlerts;

    private String mLatitude = "0";
    private String mLongitude = "0";

    private final String TIME_VISIBLE_PREF = "time visible";
    private final String WEATHER_VISIBLE_PREF = "weather visible";

    // default weather values
    private int mCurrentTemp = 0;
    private int mCurrentHumidity = 0;
    private int mCurrentWind = 0;

    private DailyForecast forecastToday;
    private JSONArray mWeatherAlerts;
    private boolean mShowFullAlerts = true;

    private CacheManager mCacheManager = null;
    // time in seconds before weather data expires
    private final int DATA_UPDATE_FREQUENCY = 600;
    public static final String WEATHER_CACHE = "weather cache";

    Handler mHandler = new Handler();

    public WeatherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = Preferences.getInstance(getActivity());
        mCacheManager = CacheManager.getInstance();
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        mLatitude = Double.toString(mPreferences.getLatitude());
        mLongitude = Double.toString(mPreferences.getLongitude());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        layTimeLayout = (LinearLayout) view.findViewById(R.id.layout_time);
        layWeatherLayout = (LinearLayout) view.findViewById(R.id.layout_weather);
        txtCurrentHumidity = (TextView) view.findViewById(R.id.current_humidity);
        txtCurrentTemp = (TextView) view.findViewById(R.id.current_temp);
        txtCurrentWind = (TextView) view.findViewById(R.id.current_wind);
        txtWeatherIcon = (TextView) view.findViewById(R.id.weather_icon);
        txtDailyHigh = (TextView) view.findViewById(R.id.daily_high);
        txtDailyLow = (TextView) view.findViewById(R.id.daily_low);
        txtAlerts = (TextView) view.findViewById(R.id.alert_text);

        txtCurrentTemp.setTypeface(weatherFont);
        txtDailyHigh.setTypeface(weatherFont);
        txtDailyLow.setTypeface(weatherFont);
        txtCurrentHumidity.setTypeface(weatherFont);
        txtCurrentWind.setTypeface(weatherFont);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Preferences.PREFS_NAME, Context.MODE_PRIVATE);

        int timeVisible = mSharedPreferences.getInt(TIME_VISIBLE_PREF, View.VISIBLE);
        if (timeVisible == View.GONE) {
            hideTime();
        }
        int weatherVisible = mSharedPreferences.getInt(WEATHER_VISIBLE_PREF, View.VISIBLE);
        if (weatherVisible == View.GONE) {
            hideWeather();
        }

        clkTime = (TextClock) view.findViewById(R.id.time_clock);
        clkDate = (TextClock) view.findViewById(R.id.date_clock);
        updateTimeDisplay();

        return view;
    }

    public void startWeatherUpdate() {
        String darkSkyRequest = "https://api.forecast.io/forecast/%s/%s,%s?units=%s";
        String darkSkyKey = getActivity().getResources().getString(R.string.dark_sky_forecast_api_key);
        String weatherUnit = "si";
        if (mPreferences.getWeatherUnits().equals(Preferences.ENGLISH)) {
            weatherUnit = "us";
        }
        updateWeatherData(String.format(darkSkyRequest, darkSkyKey, mLatitude, mLongitude, weatherUnit));
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d(Constants.TAG, "Got message:\"" + message + "\"");
            switch (message) {

                case Constants.CONDITIONS:
                    speakCurrentConditions();
                    break;
                case Preferences.CMD_WEATHER_ENGLISH:
                case Preferences.CMD_WEATHER_METRIC:
                    startWeatherUpdate();
                    break;
                case Preferences.CMD_TIME_12HR:
                    mPreferences.setTimeFormat12hr();
                    updateTimeDisplay();
                    break;
                case Preferences.CMD_TIME_24HR:
                    mPreferences.setTimeFormat24hr();
                    updateTimeDisplay();
                    break;
                case Constants.HIDE_TIME:
                    hideTime();
                    break;
                case Constants.HIDE_WEATHER:
                    hideWeather();
                    break;
                case Constants.SHOW_TIME:
                    showTime();
                    break;
                case Constants.SHOW_WEATHER:
                    showWeather();
                    break;
                case Constants.TIME:
                    // TODO: move this broadcast receiver into Mira?
                    Mira mira = Mira.getInstance((MainActivity)getActivity());
                    mira.sayCurrentTime();
                    break;
                case Constants.REMOTE_TOGGLE_TIME_VISIBLE:
                    if (layTimeLayout.getVisibility() == View.VISIBLE) {
                        hideTime();
                    } else {
                        showTime();
                    }
                    break;
                case Constants.REMOTE_TOGGLE_WEATHER_VISIBLE:
                    if (layWeatherLayout.getVisibility() == View.VISIBLE) {
                        hideWeather();
                    } else {
                        showWeather();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Check for any cached weather data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.
        if (!mCacheManager.containsKey(WEATHER_CACHE)) {
            startWeatherUpdate();
        } else {
            renderWeather((JSONObject) mCacheManager.get(WEATHER_CACHE));
            if (mCacheManager.isExpired(WEATHER_CACHE)) {
                Log.i(Constants.TAG, "WeatherCache expired. Refreshing...");
                startWeatherUpdate();
            }
        }
    }

    /**
     * When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     * We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     * voice recognition, the remote control, etc.
     */
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
        mCacheManager.registerCacheListener(WEATHER_CACHE, this);
    }

    // when this goes out of view, halt listening
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCacheManager.unRegisterCacheListener(this);
    }

    // Refresh time and date displays to current preference setting
    public void updateTimeDisplay() {
        clkTime.setFormat12Hour(mPreferences.getTimeFormat());
        clkTime.setFormat24Hour(mPreferences.getTimeFormat());
        clkDate.setFormat12Hour(mPreferences.getDateFormat());
        clkDate.setFormat24Hour(mPreferences.getDateFormat());
    }

    public void hideTime() {
        layTimeLayout.setVisibility(View.GONE);
        saveVisibilityPreference(TIME_VISIBLE_PREF, layTimeLayout.getVisibility());
    }

    public void showTime() {
        layTimeLayout.setVisibility(View.VISIBLE);
        saveVisibilityPreference(TIME_VISIBLE_PREF, layTimeLayout.getVisibility());
    }

    public void hideWeather() {
        layWeatherLayout.setVisibility(View.GONE);
        saveVisibilityPreference(WEATHER_VISIBLE_PREF, layWeatherLayout.getVisibility());
    }

    public void showWeather() {
        layWeatherLayout.setVisibility(View.VISIBLE);
        saveVisibilityPreference(WEATHER_VISIBLE_PREF, layWeatherLayout.getVisibility());
    }

    private void saveVisibilityPreference(String prefName, int value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(prefName, value);
        edit.apply();
    }

    // ----------------------- TTS Feedback -------------------------



    private void speakCurrentConditions() {

        if (forecastToday == null) return;

        String text = " Current temperature is " + mCurrentTemp + " degrees. " +
                " High today " + forecastToday.maxTemp + ", " +
                " low " + forecastToday.minTemp + ". "
                // "Current humidity " + mCurrentHumidity + " percent. "
                ;

        if (mCurrentWind > 1) {
            String speedUnits;
            if (mPreferences.getWeatherUnits().equals(Preferences.METRIC)) {
                speedUnits = "kilometers per hour";
            } else {
                speedUnits = "miles per hour";
            }
            text += " Wind speed " + mCurrentWind + "  " + speedUnits;
        }

        if (!text.equals("")) {
            speakText(text);
        }
    }

    private void speakText(String text) {
        ((MainActivity) getActivity()).speakText(text);
    }

    // Get weather data from API and display
    private void updateWeatherData(final String request) {
        new Thread() {
            public void run() {
                final JSONObject json = FetchURL.getJSON(request);
                if (json == null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            ((MainActivity) getActivity()).showToast(getString(R.string.weather_data_err),
                                    Gravity.CENTER, Toast.LENGTH_LONG);
                            updateWeatherCache(null);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            Log.i(Constants.TAG, "New weather data downloaded");
                            updateWeatherCache(json);
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void updateWeatherCache(JSONObject data) {
        // Update the WEATHER_CACHE stored in cacheManager or create new if it doesn't exist.
        mCacheManager.addCache(WEATHER_CACHE, data, DATA_UPDATE_FREQUENCY);
    }

    private void renderWeather(JSONObject json) {
        try {
            // hourlyArray holds the next 24 hours of forecasts. Get index 0 for current temp data.
            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray hourlyArray = hourly.getJSONArray("data");
            JSONObject currentHour = hourlyArray.getJSONObject(0);

            String weatherDeg = getActivity().getString(R.string.weather_deg);

            // set current temp
            mCurrentTemp = (int) Math.round(currentHour.getDouble("temperature"));
            String tempFormat = mCurrentTemp + mPreferences.getTempString();
            txtCurrentTemp.setText(tempFormat);

            // set humidity
            mCurrentHumidity = (int) Math.round((currentHour.getDouble("humidity") * 100));
            //String humidityText = mCurrentHumidity + " " + getActivity().getString(R.string.weather_humidity);
            String humidityText = "Humidity " + mCurrentHumidity + "%";
            txtCurrentHumidity.setText(humidityText);

            // set Wind Speed & Direction
            mCurrentWind = (int) Math.round(currentHour.getDouble("windSpeed"));
            String windFormat = "";

            /* // show wind units. Removed for now.
            if (mPreferences.getWeatherUnits().equals(Preferences.METRIC) ) {
                windFormat = "k";
            } else {
                windFormat = "m";
            }
            */

            String windBearing = "";
            if (currentHour.has("windBearing")) {
                windBearing = getDirectionFromBearing(currentHour.getInt("windBearing"));
            }
            String windSpeed = windBearing + " " + getActivity().getString(R.string.weather_wind_strong) +
                    " " + mCurrentWind + " " + windFormat;
            txtCurrentWind.setText(windSpeed);


            // ---------------- 3-Day Forecast ---------------
            /*
                Moved to ForecastFragment
            */

            JSONArray dailyData = json.getJSONObject("daily").getJSONArray("data");
            JSONObject today = dailyData.getJSONObject(0);
            forecastToday = new DailyForecast(today);

            // set current weather icon
            setWeatherIcon(getActivity(), weatherFont, txtWeatherIcon, currentHour.getString("icon"),
                    forecastToday.sunrise * 1000,
                    forecastToday.sunset * 1000);

            // Set the dailyHigh and dailyLow
            /*
            String maxTemp= getActivity().getString(R.string.weather_therm_high) + " " +
                    forecastToday.maxTemp + weatherDeg;
                    */
            String maxTemp = "High " + forecastToday.maxTemp + weatherDeg;
            txtDailyHigh.setText(maxTemp);

            /*
            String minTemp = getActivity().getString(R.string.weather_therm_low) + " " +
                    forecastToday.minTemp + weatherDeg;
                    */
            String minTemp = "Low  " + forecastToday.minTemp + weatherDeg;
            txtDailyLow.setText(minTemp);

            // ----------------- 2-Hour forecasts -------------
            for (int i = 1; i <= 7; i++) {
                String resourceName = "forecast_" + i;
                int layoutId = getContext().getResources().getIdentifier(resourceName, "id",
                        getActivity().getPackageName());
                LinearLayout forecastLayout = (LinearLayout) getActivity().findViewById(layoutId);
                JSONObject forecast = hourlyArray.getJSONObject(i * 2);

                // Forecast time
                TextView timeForecast = (TextView) forecastLayout.findViewById(R.id.forecast_time);
                Date date = new Date(forecast.getLong("time") * 1000);
                String shortTimeFormat = mPreferences.getShortTimeFormat();
                SimpleDateFormat sdf = new SimpleDateFormat(shortTimeFormat, Locale.US);
                String time = sdf.format(date).toLowerCase(Locale.US);
                timeForecast.setText(time);

                // Forecast temp
                TextView tempForecast = (TextView) forecastLayout.findViewById(R.id.forecast_temp);
                String textTmp = (int) Math.round(forecast.getDouble("temperature")) + weatherDeg;
                tempForecast.setText(textTmp);
                tempForecast.setTypeface(weatherFont);

                // forecast icon
                TextView iconForecast = (TextView) forecastLayout.findViewById(R.id.forecast_image);
                String icon = forecast.getString("icon");
                setWeatherIcon(getActivity(), weatherFont, iconForecast, icon, forecastToday.sunrise, forecastToday.sunset);

                TextView txtUmbrella = (TextView) forecastLayout.findViewById(R.id.forecast_umbrella);
                TextView rainForecast = (TextView) forecastLayout.findViewById(R.id.forecast_rain);

                int rainProb = (int) (forecast.getDouble("precipProbability") * 100);

                // show chance of rain if >= 10%
                if (rainProb >= 10) {

                    // umbrella icon : disabled
                    /*
                    txtUmbrella.setTypeface(weatherFont);
                    txtUmbrella.setText(getActivity().getResources().getString(R.string.weather_umbrella));
                    txtUmbrella.setVisibility(View.VISIBLE);
                    */

                    // Chance of rain
                    String chanceOfRain = Integer.toString(rainProb) + "%";
                    rainForecast.setText(chanceOfRain);
                    rainForecast.setVisibility(View.VISIBLE);
                } else {
                    txtUmbrella.setVisibility(View.GONE);
                    rainForecast.setVisibility(View.GONE);
                }
            }

            // check for weather alerts.
            if (json.has("alerts")) {
                mWeatherAlerts = json.getJSONArray("alerts");
                String alertText = getWeatherAlerts();
                Log.i(Constants.TAG, "alertText: " + alertText);
                if (alertText.length() > 0) {
                    txtAlerts.setVisibility(View.VISIBLE);
                    txtAlerts.setText(getWeatherAlerts());
                    txtAlerts.setSelected(true);
                }
            } else {
                txtAlerts.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DarkSky", "One or more fields not found in the JSON data");
        }
    }


    public String getWeatherAlerts() {
        StringBuilder alertText = new StringBuilder();
        int i = 0;
        while (i < mWeatherAlerts.length()) {
            alertText.append(getAlertTitle(i));
            if (mShowFullAlerts) {
                alertText.append(getAlertDescription(i));
            }
            i++;
        }
        return alertText.toString();
    }

    private String getAlertTitle(int index) {
        // find expiration time for this alert
        String title = "";
        try {
            long expirationEpochTime = Long.parseLong(mWeatherAlerts.getJSONObject(index).getString("expires"));
            String expirationTime = new SimpleDateFormat(mPreferences.getTimeFormat(), Locale.US)
                    .format(new Date(expirationEpochTime));

            title = mWeatherAlerts.getJSONObject(index).getString("title") + ". Expires " + expirationTime + "\n";
        } catch (JSONException jse) {
            Log.e("DarkSky", "alert index not found");
        }
        return title;
    }

    private String getAlertDescription(int index) {
        String description = "";
        try {
            description = mWeatherAlerts.getJSONObject(index).getString("description") + "\n";
        } catch (JSONException jse) {
            Log.e("DarkSky", "alert index not found");
        }
        return description;
    }


    // choose weather icon based on iconType
    public static void setWeatherIcon(Activity activity, Typeface font, TextView tv, String iconType, long sunrise, long sunset) {
        String icon;
        if (activity == null) return;
        switch (iconType) {
            case "clear-day":
                icon = activity.getString(R.string.weather_sunny);
                break;
            case "clear-night":
                icon = activity.getString(R.string.weather_clear_night);
                break;
            case "cloudy":
                icon = activity.getString(R.string.weather_cloudy);
                break;
            case "partly-cloudy-day":
                icon = activity.getString(R.string.weather_cloudy_day);
                break;
            case "partly-cloudy-night":
                icon = activity.getString(R.string.weather_partly_cloudy_night);
                break;
            case "drizzle":
                icon = activity.getString(R.string.weather_drizzle);
                break;
            case "fog":
                icon = activity.getString(R.string.weather_foggy);
                break;
            case "rain":
                icon = activity.getString(R.string.weather_rainy);
                break;
            case "sleet":
                icon = activity.getString(R.string.weather_sleet);
                break;
            case "snow":
                icon = activity.getString(R.string.weather_snowy);
                break;
            case "thunderstorm":
                icon = activity.getString(R.string.weather_thunder);
                break;
            case "wind":
                icon = activity.getString(R.string.weather_wind_strong);
                break;
            // if we can't find the right icon, set it to generic day or night depending on time
            default:
                long currentTime = new Date().getTime();
                if (currentTime >= sunrise && currentTime < sunset) {
                    icon = activity.getString(R.string.weather_sunny);
                } else {
                    icon = activity.getString(R.string.weather_clear_night);
                }
                break;
        }

        tv.setText(icon);
        tv.setTypeface(font);
    }

    /**
     * Callback from CacheManager
     */
    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(WEATHER_CACHE)) startWeatherUpdate();
    }

    /**
     * Callback from CacheManager
     */
    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startWeatherUpdate() will refresh the views.
    }

    /**
     * Given a wind bearing in degrees, returns the cardinal direction
     *
     * @return String direction as abbreviation (NE, E, W...)
     */
    public static String getDirectionFromBearing(final int bearing) {
        if (bearing < 0 || bearing > 360) return "error";
        String[] directions = {"North", "NEast", "East", "SEast", "South", "SWest", "West", "NWest"};
        int index = ((int) ((bearing + 22.5) / 45)) % 8;
        return directions[index];
    }
}
