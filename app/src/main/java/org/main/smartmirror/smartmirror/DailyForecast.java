package org.main.smartmirror.smartmirror;

import org.json.JSONException;
import org.json.JSONObject;

// holds forecast data for one day
public class DailyForecast {
    public int minTemp;
    public int maxTemp;
    public String summary;
    public String icon;
    public int windSpeed;
    public long sunrise;
    public long sunset;
    public double precipProbability;
    public long forecastTime;

    public DailyForecast() {
    }

    /**
     * Construct with JSON
     *
     * @param today JSONObject corresponding to one daily: { data: { 'today' } }
     */
    public DailyForecast(JSONObject today) {
        try {
            maxTemp = (int) Math.round(today.getDouble("apparentTemperatureMax"));
            minTemp = (int) Math.round(today.getDouble("apparentTemperatureMin"));
            summary = today.getString("summary");
            icon = today.getString("icon");
            sunrise = today.getLong("sunriseTime");
            sunset = today.getLong("sunsetTime");
            precipProbability = today.getDouble("precipProbability");
            windSpeed = (int) Math.round(today.getDouble("windSpeed"));
            forecastTime = today.getLong("time");
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }
}