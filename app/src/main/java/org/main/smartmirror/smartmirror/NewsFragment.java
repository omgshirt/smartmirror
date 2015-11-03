package org.main.smartmirror.smartmirror;

import android.content.Intent;
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


//fetch news from New York Times
//article search api key:89899870d1024b962dc582806e3e9c34:3:73303333
//api: http://api.nytimes.com/svc/search/v2/articlesearch
//uri structure: http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####

public class NewsFragment extends Fragment {

    private TextView txtHeadline;

    Handler mHandler = new Handler();

    // this URL does some filtering so as to only retrieve the "headline" and "snippet" for each item,
    // without the full text and other extras (date, etc).
    // see the NYTIMES API console for JSON format
/*    private String sportsUrl = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3Asports&" +
            "begin_date=20151028&end_date=20151028&sort=newest&fl=headline%2Csnippet&page=0&api-key=";*/

    public NewsFragment() {}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        txtHeadline = (TextView)view.findViewById(R.id.headline);

        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        String apiKey = getString(R.string.nyt_api_key);

        String sportsURL = this.getArguments().getString("url");
        sportsURL += apiKey;
        updateSports(sportsURL);
        return view;
    }


    // Get sports headlines from api and display
    private void updateSports(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.sports_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            renderSports(json);
                        }
                    });
                }
            }
        }.start();

    }


    private void renderSports(JSONObject json){
        try {

            Log.i("NYT_API", json.toString());
            JSONObject response = json.getJSONObject("response");
            JSONObject docs = response.getJSONArray("docs").getJSONObject(0);
            String snippet = docs.getString("snippet");
            JSONObject headline = docs.getJSONObject("headline");

            txtHeadline.setText(headline.getString("main") + "\n" + snippet);


        }catch(Exception e){
            Log.e("SPORTS ERROR", e.toString());
        }
    }

}
