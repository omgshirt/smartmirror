package org.main.smartmirror.smartmirror;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


public class FetchWeather {

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?id=%s&units=%s";

    //private static final String OPEN_WEATHER_MAP_API =
      //      "http://api.openweathermap.org/data/2.5/forecast?id=%s&units=%s";

    public static JSONObject getJSON(Context context, String city, String tempFormat){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city, tempFormat));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.open_weather_map_api_key));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder();
            String tmp;
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // IF not successful, return a null
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
            
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
