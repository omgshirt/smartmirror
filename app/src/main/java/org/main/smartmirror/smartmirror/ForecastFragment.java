package org.main.smartmirror.smartmirror;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForecastFragment extends Fragment implements CacheManager.CacheListener {

    private Typeface weatherFont;
    private DailyForecast[] dailyForecasts;
    private final int DAYS_TO_CONVERT = 6;

    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.forecast_fragment, container, false);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        if (savedInstanceState == null) {
            dailyForecasts = new DailyForecast[DAYS_TO_CONVERT];
            updateForecasts();
            renderForecasts(view);
            speakWeatherForecast();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        CacheManager.getInstance().registerCacheListener(WeatherFragment.WEATHER_CACHE, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        CacheManager.getInstance().unRegisterCacheListener(WeatherFragment.WEATHER_CACHE, this);
    }

    private void updateForecasts() {
        JSONObject json = (JSONObject) CacheManager.getInstance().get(WeatherFragment.WEATHER_CACHE);

        if (json == null) {
            Log.e(Constants.TAG, "forecast: json null");
            return;
        }

        // If cache is empty, return
        if (json.length() == 0) {
            Log.e(Constants.TAG, "forecast: json length 0");
            return;
        }

        // Create dailyForecasts for 4 days, including current day
        try {
            JSONArray dailyData = json.getJSONObject("daily").getJSONArray("data");
            for (int i = 0; i < dailyForecasts.length; i++) {
                JSONObject today = dailyData.getJSONObject(i);
                dailyForecasts[i] = new DailyForecast(today);
                //Log.i(Constants.TAG, "DailyForecast :: " + dailyForecasts[i]);
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    /**
     * Show the forecast
     * @param view LinearLayout to add forecast items
     */
    private void renderForecasts(LinearLayout view) {

        // render days 1,2,3
        try {
            for (int i = 1; i < dailyForecasts.length; i++) {

                String resourceName = "daily_forecast_" + i;
                int layoutId = getContext().getResources().getIdentifier(resourceName, "id",
                        getActivity().getPackageName());
                RelativeLayout forecastLayout = (RelativeLayout) view.findViewById(layoutId);

                TextView txtDay = (TextView) forecastLayout.findViewById(R.id.daily_forecast_day);
                TextView txtForecastIcon = (TextView) forecastLayout.findViewById(R.id.daily_forecast_icon);
                TextView txtHighTemp = (TextView) forecastLayout.findViewById(R.id.daily_forecast_temp);

                Date date = new Date(dailyForecasts[i].forecastTime * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.US);
                String day = sdf.format(date);
                txtDay.setText(day);

                String icon = dailyForecasts[i].icon;
                WeatherFragment.setWeatherIcon(getActivity(), weatherFont, txtForecastIcon, icon,
                        dailyForecasts[i].sunrise, dailyForecasts[i].sunset);
                txtForecastIcon.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);

                String textTemp = Math.round(dailyForecasts[i].maxTemp) +
                        getResources().getString(R.string.weather_deg);
                txtHighTemp.setText(textTemp);
                txtHighTemp.setTypeface(weatherFont);
                txtHighTemp.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    // compile and say weather forecast for the next 3 days
    private void speakWeatherForecast() {
        try {
            /*
            String forecast = "Tomorrow " + dailyForecasts[1].summary +
                    " high of " + dailyForecasts[1].maxTemp +
                    " degrees, low " + dailyForecasts[1].minTemp + ". ";
            */
            String forecast = "";

            for (int i = 1; i <= 3; i++) {
                Date date = new Date(dailyForecasts[i].forecastTime * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("cccc", Locale.US);
                String dayName = sdf.format(date);

                forecast += dayName + ", " + dailyForecasts[i].summary +
                        " high of " + dailyForecasts[i].maxTemp +
                        ", low of " + dailyForecasts[i].minTemp +
                        " degrees. ";
            }
            if (!forecast.equals("")) {
                speakText(forecast);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }


    }

    private void speakText(String text) {
        ((MainActivity) getActivity()).speakText(text);
    }

    @Override
    public void onCacheExpired(String cacheName) {

    }

    @Override
    public void onCacheChanged(String cacheName) {
        if (cacheName.equals(WeatherFragment.WEATHER_CACHE)) {
            updateForecasts();
            renderForecasts( (LinearLayout)getView() );
        }
    }
}
