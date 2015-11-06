package org.main.smartmirror.smartmirror;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//fetch news from New York Times
//article search api key:89899870d1024b962dc582806e3e9c34:3:73303333
//api: http://api.nytimes.com/svc/search/v2/articlesearch
//uri structure: http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####

public class NewsFragment extends Fragment {

    private TextView mTxtHeadline;
    private TextView mTxtHeadline2;
    private TextView mTxtHeadline3;
    private TextView mTxtHeadline4;
    private TextView mTxtHeadline5;

    Handler mHandler = new Handler();

    // this URL does some filtering so as to only retrieve the "headline" and "snippet" for each item,
    // without the full text and other extras (date, etc).
    // see the NYTIMES API console for JSON format
/*    private String sportsUrl = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3Asports&" +
            "begin_date=20151028&end_date=20151028&sort=newest&fl=headline%2Csnippet&page=0&api-key=";*/

    public NewsFragment() {}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        mTxtHeadline = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);

        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        String apiKey = getString(R.string.nyt_api_key);

        String newsURL = this.getArguments().getString("url");
        newsURL += apiKey;
        updateNews(newsURL);


        return view;

    }


    // Get news headlines from api and display
    private void updateNews(final String query){
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
                            renderNews(json);
                        }
                    });
                }
            }
        }.start();

    }


    private void renderNews(JSONObject json){
        try {
            String newsFeed[] = new String[10];

            JSONObject response = null;
            JSONObject docs = null;
            String snippet = null;
            JSONObject headline = null;

            int i = 0;
            int numFeeds = 5;
            while (i < numFeeds) {
                Log.i("NYT_API", json.toString());
                response = json.getJSONObject("response");
                docs = response.getJSONArray("docs").getJSONObject(i);
                snippet = docs.getString("snippet");
                headline = docs.getJSONObject("headline");

                newsFeed[i] = snippet;
                Log.i("news string : ", newsFeed[i]);
                i++;
            }

            //mTxtHeadline.setText(headline.getString("main") + "\n" + snippet);
            mTxtHeadline.setText("\n" + newsFeed[0]);
            mTxtHeadline2.setText("\n" + newsFeed[1]);
            mTxtHeadline3.setText("\n" + newsFeed[2]);
            mTxtHeadline4.setText("\n" + newsFeed[3]);
            mTxtHeadline5.setText("\n" + newsFeed[4]);

        }catch(Exception e){
            Log.e("SPORTS ERROR", e.toString());
        }
    }

}
