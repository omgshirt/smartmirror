package org.main.smartmirror.smartmirror;

//fetch sports from New York Times
//article search api key:89899870d1024b962dc582806e3e9c34:3:73303333
//api: http://api.nytimes.com/svc/search/v2/articlesearch
//uri structure: http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####


import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSports {

    static final String API_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?&fq=news_desk:(%s)&api-key=%s";


    //private static final String API_KEY = "89899870d1024b962dc582806e3e9c34:3:73303333";

    //private static final String NYT_SEARCH_API =
           // "http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####";


    public static JSONObject getJSON(String headline, String apiKey){
        try {
            URL url = new URL(String.format(API_URL, headline, apiKey));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            //connection.addRequestProperty("x-api-key",context.getString(R.string.nyt_api_key));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            Log.i("sportapi", data.toString());

            // IF not successful, return a null
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;

        }catch(Exception e){
            return null;
        }
    }




}
