package org.main.smartmirror.smartmirror;

//fetch sports from New York Times
//article search api key:89899870d1024b962dc582806e3e9c34:3:73303333
//api: http://api.nytimes.com/svc/search/v2/articlesearch
//uri structure: http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchURL {

    public static JSONObject getJSON(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder();
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();
            connection.disconnect();

            JSONObject data = new JSONObject(json.toString());

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
