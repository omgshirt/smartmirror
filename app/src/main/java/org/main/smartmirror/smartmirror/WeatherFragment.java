package org.main.smartmirror.smartmirror;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import java.util.Date;
import java.util.Random;


public class WeatherFragment extends Fragment {

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

    Handler mHandler = new Handler();
    private String mCityCode = "5341114";                 // openweatherapi id for the city to find

    // hold weather data for TTS
    private int mTempMax = -999;
    private int mTempMin = -999;
    private int mCurrentTemp = -999;
    private int mCurrentHumidity = 0;
    private int mCurrentWind = 0;

    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        mPreferences = Preferences.getInstance();
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(mCityCode, mPreferences.getDisplayUnitsAsString());

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

        txtWeatherIcon.setTypeface(weatherFont);
        txtCurrentTemp.setTypeface(weatherFont);
        txtDailyHigh.setTypeface(weatherFont);
        txtDailyLow.setTypeface(weatherFont);

        clkTextClock.setFormat12Hour(mPreferences.getTimeFormat());
        clkDateClock.setFormat12Hour(mPreferences.getDateFormat());

        return view;
    }

    private void saySomethingAboutWeather() {
        Random rand = new Random();
        String text = "";
        switch (rand.nextInt(5)) {
            case 0:
                text = "the current temperature is " + mCurrentTemp + " degrees";
                break;
            case 1:
                text = "the high today is " + mTempMax + " degrees";
                break;
            case 2:
                text = "the low today is " + mTempMin + " degrees";
                break;
            case 3:
                text = "the current humidity is " + mCurrentHumidity + " percent";
                break;
            case 4:
                String speedUnits;
                if( mPreferences.getWindDisplayFormat().equals(Preferences.KPH) )  {
                    speedUnits = "kilometers per hour";
                } else {
                    speedUnits = "miles per hour";
                }
                text = "the current wind speed is " + mCurrentWind + "  " + speedUnits;
                break;
        }

        if ( !text.equals("") ) {
            ((MainActivity) getActivity()).startVoice(text);
        }
    }

    // Get weather data from API and display
    private void updateWeatherData(final String city, final String tempFormat){
        new Thread(){
            public void run(){
                final JSONObject json = FetchWeather.getJSON(getActivity().getApplicationContext(), city, tempFormat);
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
            mCurrentTemp = (int)main.getDouble("temp");
            txtCurrentTemp.setText( mCurrentTemp + tempFormat);

            // set humidity
            mCurrentHumidity = (int)main.getDouble("humidity");
            String humidityText = "Humidity: " + mCurrentHumidity + "%";
            txtCurrentHumidity.setText(humidityText);

            // set Wind Speed
            mCurrentWind = (int) wind.getDouble("speed");
            String windSpeed = "Wind: " + mCurrentWind + " " + mPreferences.getWindDisplayFormat();
            txtCurrentWind.setText( windSpeed );

            // Set the dailyHigh and dailyLow
            mTempMax = (int)main.getDouble("temp_max");
            String maxIcon = getActivity().getString(R.string.weather_arrow_up);
            txtDailyHigh.setText(maxIcon + mTempMax);

            mTempMin = (int)main.getDouble("temp_min");
            String minIcon = getActivity().getString(R.string.weather_arrow_down);
            txtDailyLow.setText(minIcon + mTempMin);

            // TODO: set up hourly weather forecasts
            saySomethingAboutWeather();
        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    // choose weather icon based on returned weatherId
    private void setWeatherIcon(int weatherId, long sunrise, long sunset){
        int id = weatherId / 100;
        String icon = "";
        if(weatherId == 800){
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
