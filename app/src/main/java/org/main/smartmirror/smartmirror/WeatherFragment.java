package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


/**
 * Fragment that displays the weather information
 *
 * Commands :
 *              "forecast" speaks the 3-day forecast
 *              "conditions" speaks the current conditions
 */
public class WeatherFragment extends Fragment implements LocationListener {

    Typeface weatherFont;
    Preferences mPreferences;

    private TextView txtWeatherIcon;
    private TextClock clkTextClock;
    private TextClock clkDateClock;
    private TextView txtCurrentTemp;
    private TextView txtCurrentWind;
    private TextView txtCurrentHumidity;
    private TextView txtDailyHigh;
    private TextView txtDailyLow;
    private TextView txtAlerts;
    private TextView txtWeatherAlert;

    private static String darkSkyRequest = "https://api.forecast.io/forecast/%s/%s,%s?units=%s";
    private String latitude = "0";
    private String longitude = "0";

    // default weather values
    private int mCurrentTemp = 0;
    private int mCurrentHumidity = 0;
    private int mCurrentWind = 0;
    private DailyForecast forecasts[];              // summary of data for 3 days (starting today)

    Handler mHandler = new Handler();

    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mPreferences = Preferences.getInstance();
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        forecasts = new DailyForecast[3];

        // TODO: Find current lat and long positions. This should be within its own class so other frags can use it.
        // maybe store this value in preferences?
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (getActivity().checkCallingOrSelfPermission(LocationManager.GPS_PROVIDER) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location != null) {
            try {
                latitude = Double.toString(location.getLatitude());
                longitude = Double.toString(location.getLongitude());
                Log.d("old","lat :  " + latitude);
                Log.d("old","long :  " + longitude);
                this.onLocationChanged(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // some static locations for now
        latitude = "34";
        longitude = "-118";

        String darkSkyKey = getActivity().getResources().getString(R.string.dark_sky_forecast_api_key);
        String weatherUnit;
        if (mPreferences.getWeatherUnits().equals(Preferences.ENGLISH)) {
            weatherUnit = "us";
        } else {
            weatherUnit = "si";
        }
        updateWeatherData(String.format(darkSkyRequest, darkSkyKey, latitude, longitude, weatherUnit));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        clkTextClock = (TextClock)view.findViewById(R.id.time_clock);
        clkDateClock = (TextClock)view.findViewById(R.id.date_clock);
        txtCurrentHumidity = (TextView)view.findViewById(R.id.current_humidity);
        txtCurrentTemp = (TextView)view.findViewById(R.id.current_temp);
        txtCurrentWind = (TextView)view.findViewById(R.id.current_wind);
        txtWeatherIcon = (TextView)view.findViewById(R.id.weather_icon);
        txtDailyHigh = (TextView)view.findViewById(R.id.daily_high);
        txtDailyLow = (TextView)view.findViewById(R.id.daily_low);
        txtAlerts = (TextView)view.findViewById(R.id.alert_text);
        txtWeatherAlert = (TextView)view.findViewById(R.id.weather_alert);

        txtCurrentTemp.setTypeface(weatherFont);
        txtDailyHigh.setTypeface(weatherFont);
        txtDailyLow.setTypeface(weatherFont);

        clkTextClock.setFormat12Hour(mPreferences.getTimeFormat());
        clkDateClock.setFormat12Hour(mPreferences.getDateFormat());

        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("Weather", "Got message: " + message);
            switch (message) {
                case MainActivity.FORECAST:
                    speakWeatherForecast();
                    break;
                case MainActivity.CONDITIONS:
                    speakCurrentConditions();
                    break;
            }
        }
    };

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    // ----------------------- TTS Feedback -------------------------

    private void speakCurrentConditions() {

        if (forecasts == null) return;

        String text = " Current temperature is " + mCurrentTemp + " degrees. " +
                    " High today " + forecasts[0].maxTemp + ", " +
                    " low " + forecasts[0].minTemp + ". "
                    // "Current humidity " + mCurrentHumidity + " percent. "
                    ;

        if (mCurrentWind > 1) {
            String speedUnits;
            if( mPreferences.getWeatherUnits().equals(Preferences.METRIC))  {
                speedUnits = "kilometers per hour";
            } else {
                speedUnits = "miles per hour";
            }
            text += " Wind speed " + mCurrentWind + "  " + speedUnits;
        }

        if ( !text.equals("") ) {
            ((MainActivity) getActivity()).startTTS(text);
        }
    }

    // compile and say weather forecast for the next 3 days
    private void speakWeatherForecast(){

        if (forecasts == null) return;

        String today = "Today " + forecasts[0].summary + " high of " + forecasts[0].maxTemp +
                " degrees, low tonight " + forecasts[0].minTemp;

        String tomorrow = "Tomorrow " + forecasts[1].summary + " high of " + forecasts[1].maxTemp +
                " degrees, low temperature " + forecasts[1].minTemp + " degrees";

        Date date = new Date(forecasts[2].forecastTime * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("cccc", Locale.US);
        String dayThreeName = sdf.format(date);

        String nextDay = dayThreeName + ", " + forecasts[2].summary + " high of " + forecasts[2].maxTemp +
                ", low of " + forecasts[2].minTemp + " degrees ";

        String forecast = today + ". " + tomorrow + ". " + nextDay;
        if ( !forecast.equals("") ) {
            ((MainActivity) getActivity()).startTTS(forecast);
        }
    }

    // Get weather data from API and display
    private void updateWeatherData(final String request){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(request);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {
            // hourlyArray holds the next 24 hours of forecasts. Get index 0 for current temp data.
            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray hourlyArray = hourly.getJSONArray("data");
            JSONObject currentHour = hourlyArray.getJSONObject(0);

            // set current temp
            mCurrentTemp = (int)Math.round(currentHour.getDouble("temperature"));
            String tempFormat = mPreferences.getTempString();
            txtCurrentTemp.setText(mCurrentTemp + tempFormat);

            // set humidity
            mCurrentHumidity = (int) Math.round((currentHour.getDouble("humidity") * 100));
            String humidityText = "Humidity " + mCurrentHumidity + "%";
            txtCurrentHumidity.setText(humidityText);

            // set Wind Speed & Direction
            mCurrentWind = (int)Math.round(currentHour.getDouble("windSpeed"));
            String windFormat;
            if (mPreferences.getWeatherUnits().equals(Preferences.METRIC) ) {
                windFormat = "kph";
            } else {
                windFormat = "mph";
            }
            String windBearing = "";
            if (currentHour.has("windBearing")) {
                windBearing = getDirectionFromBearing(currentHour.getInt("windBearing"));
            }
            String windSpeed = "Wind " + windBearing + " " + mCurrentWind + " " + windFormat;
            txtCurrentWind.setText(windSpeed);


            // ---------------- 3-Day Forecast ---------------
            JSONArray dailyData = json.getJSONObject("daily").getJSONArray("data");
            for (int i = 0; i < 3; i++) {
                JSONObject today = dailyData.getJSONObject(i);
                forecasts[i] = new DailyForecast();
                forecasts[i].maxTemp = (int)Math.round(today.getDouble("apparentTemperatureMax"));
                forecasts[i].minTemp = (int)Math.round(today.getDouble("apparentTemperatureMin"));
                forecasts[i].summary = today.getString("summary");
                forecasts[i].sunrise = today.getLong("sunriseTime");
                forecasts[i].sunset = today.getLong("sunsetTime");
                forecasts[i].precipProbability = today.getDouble("precipProbability");
                forecasts[i].windSpeed = (int)Math.round(today.getDouble("windSpeed"));
                forecasts[i].forecastTime = today.getLong("time");
            }

            // set current weather icon
            setWeatherIcon(txtWeatherIcon, currentHour.getString("icon"), forecasts[0].sunrise * 1000,
                    forecasts[0].sunset * 1000 );

            // Set the dailyHigh and dailyLow
            String maxIcon = getActivity().getString(R.string.weather_arrow_up) + forecasts[0].maxTemp;
            txtDailyHigh.setText(maxIcon);
            String minIcon = getActivity().getString(R.string.weather_arrow_down) + forecasts[0].minTemp;
            txtDailyLow.setText(minIcon);

            // ----------------- 2-Hour forecasts -------------
            for (int i = 1; i <= 6; i++) {
                String template = "forecast_" + i;
                int layoutId = getContext().getResources().getIdentifier(template, "id" ,
                        getActivity().getPackageName() );
                LinearLayout forecastLayout = (LinearLayout) getActivity().findViewById(layoutId);
                JSONObject forecast = hourlyArray.getJSONObject(i*2);

                TextView timeForecast = (TextView)forecastLayout.findViewById(R.id.forecast_time);
                Date date = new Date(forecast.getLong("time") * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("ha", Locale.US);
                String time = sdf.format(date);
                timeForecast.setText(time);
                timeForecast.setTextSize(15);

                TextView tempForecast = (TextView)forecastLayout.findViewById(R.id.forecast_temp);
                int temp = (int)Math.round(forecast.getDouble("temperature"));
                tempForecast.setText( temp + getResources().getString(R.string.weather_deg) );
                tempForecast.setTypeface(weatherFont);
                tempForecast.setTextSize(15);

                TextView iconForecast = (TextView)forecastLayout.findViewById(R.id.forecast_image);
                String icon = forecast.getString("icon");
                setWeatherIcon(iconForecast, icon, forecasts[0].sunrise, forecasts[0].sunset);
                iconForecast.setTextSize(15);

                TextView rainForecast = (TextView) forecastLayout.findViewById(R.id.forecast_rain);
                String chanceOfRain = Integer.toString( (int)(forecast.getDouble("precipProbability") * 100)) + "%";
                rainForecast.setText(chanceOfRain);
            }

            // check for weather alerts. Display and speak if they exist
            if (json.has("alerts")) {
                JSONArray alerts = json.getJSONArray("alerts");
                StringBuilder title = new StringBuilder();
                int i = 0;
                while (i < alerts.length()) {
                    // Alert descriptions can get really long. Ignoring for now.
                    //String description = alerts.getJSONObject(i).getString("description");
                    title.append(alerts.getJSONObject(i).getString("title") + "\n");
                    txtWeatherAlert.setVisibility(View.VISIBLE);
                    i++;
                }
                if (title.length() > 0) {
                    ((MainActivity) getActivity()).startTTS(title.toString());
                    txtAlerts.setText(title.toString());
                }
            }
            speakCurrentConditions();
        }catch(Exception e){
            e.printStackTrace();
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    // choose weather icon based on iconType
    private void setWeatherIcon(TextView tv, String iconType, long sunrise, long sunset){
        String icon = "";
        switch(iconType) {
            case "clear-day": icon = getActivity().getString(R.string.weather_sunny);
                break;
            case "clear-night": icon = getActivity().getString(R.string.weather_clear_night);
                break;
            case "cloudy" : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case "partly-cloudy-day" : icon = getActivity().getString(R.string.weather_cloudy_day);
                break;
            case "partly-cloudy-night" :  icon = getActivity().getString(R.string.weather_partly_cloudy_night);
                break;
            case "drizzle" : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case "fog" : icon = getActivity().getString(R.string.weather_foggy);
                break;
            case "rain" : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case "sleet" : icon = getActivity().getString(R.string.weather_sleet);
                break;
            case "snow" : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case "thunderstorm" : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case "wind" : icon = getActivity().getString(R.string.weather_wind_strong);
                break;
            default:
                long currentTime = new Date().getTime();
                if(currentTime>=sunrise && currentTime<sunset) {
                    icon = getActivity().getString(R.string.weather_sunny);
                } else {
                    icon = getActivity().getString(R.string.weather_clear_night);
                }
                break;
        }

        tv.setText(icon);
        tv.setTypeface(weatherFont);
    }

    // Location Listener Implementation
    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location", "Location change detected");
        latitude = Double.toString(location.getLatitude());
        longitude = Double.toString(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // holds forecast data for one day
    private class DailyForecast {
        public int minTemp;
        public int maxTemp;
        public String summary;
        public int windSpeed;
        public long sunrise;
        public long sunset;
        public double precipProbability;
        public long forecastTime;
    }

    /**
     * Given a wind bearing, returns the direction
     * @return String direction as abbreviation (NE, E, W...)
     */
    public String getDirectionFromBearing(int bearing){
        if (bearing < 0 || bearing > 360) return "error";
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) ((bearing + 22.5) / 45);
        return directions[ index ];
    }
}
