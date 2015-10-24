package org.main.smartmirror.smartmirror;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    public static final String DEGREE_SYMBOL = "\u00B0";

    Typeface weatherFont;
    Preferences mPreferences;

    private TextView txtWeatherIcon;
    private TextView txtCurrentDate;
    private TextView txtCurrentTime;
    private TextView txtCurrentTemp;
    private TextView txtCurrentWind;
    private TextView txtCurrentHumidity;
    private TextView txtDailyHigh;
    private TextView txtDailyLow;

    private TextView cityField;

    Handler handler = new Handler();

    public WeatherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        mPreferences = Preferences.getInstance();
        String city = "LosAngeles,us";
        updateWeatherData(city, mPreferences.getTempFormat());
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        txtCurrentDate = (TextView)view.findViewById(R.id.current_date);
        txtCurrentTime =(TextView)view.findViewById(R.id.current_time);
        txtCurrentHumidity = (TextView)view.findViewById(R.id.current_humidity);
        txtCurrentTemp = (TextView)view.findViewById(R.id.current_temp);
        txtCurrentWind = (TextView)view.findViewById(R.id.current_wind);
        txtWeatherIcon = (TextView)view.findViewById(R.id.weather_icon);
        txtDailyHigh = (TextView)view.findViewById(R.id.daily_high);
        txtDailyLow = (TextView)view.findViewById(R.id.daily_low);
        cityField = (TextView)view.findViewById(R.id.cityField);

        txtWeatherIcon.setTypeface(weatherFont);

        // set the date and time fields based on Preferences
        txtCurrentDate.setText(getFormattedTime(mPreferences.getDateFormat()));
        txtCurrentTime.setText(getFormattedTime(mPreferences.getTimeFormat()));
        return view;
    }

    // convert the current time into the format provided
    private String getFormattedTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        String date = sdf.format(new Date());
        Log.d("%s", date);
        return date;
    }

    private void updateWeatherData(final String city, final String tempFormat){
        new Thread(){
            public void run(){
                final JSONObject json = FetchWeather.getJSON(getActivity().getApplicationContext(), city, tempFormat);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
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
            /*
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));
            */

            Log.i("WEATHER_API", json.toString());

            JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");

            // set Icon
            setWeatherIcon(weather.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

            // Set Temp
            String tempFormat = mPreferences.getTempUnits();
            txtCurrentTemp.setText(
                    String.format("%.0f", main.getDouble("temp")) + DEGREE_SYMBOL + " " + tempFormat);

            // set humidity
            txtCurrentHumidity.setText("Humidity: " + main.getString("humidity") + "%");

            // set Wind Speed
            String windSpeed = String.format("%.0f", wind.getDouble("speed"));
            txtCurrentWind.setText("Wind: " + windSpeed + " " + mPreferences.getWindDisplayFormat());

            // Set the dailyHigh and dailyLow
            Double tempMax = main.getDouble("temp_max");
            txtDailyHigh.setText(String.format("High: %.0f", tempMax));
            Double tempMin = main.getDouble("temp_min");
            txtDailyLow.setText(String.format("Low: %.0f", tempMin));

            // TODO: set weather forecast

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
       txtWeatherIcon.setText(icon);
    }
}
