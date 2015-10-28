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

//fetch sports from New York Times
//article search api key:89899870d1024b962dc582806e3e9c34:3:73303333
//api: http://api.nytimes.com/svc/search/v2/articlesearch
//uri structure: http://api.nytimes.com/svc/search/v2/articlesearch.response-format?[q=search term&fq=filter-field:(filter-term)&additional-params=values]&api-key=####

public class SportsFragment extends Fragment {

    private TextView txtHeadline;

    Handler mHandler = new Handler();

    public SportsFragment() {}

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        String headline = "Sports";
        String apiKey = getString(R.string.nyt_api_key);

        updateSports(headline, apiKey);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sports_fragment, container, false);


        txtHeadline = (TextView)view.findViewById(R.id.headline);


        return view;
    }


    // Get sports headlines from api and display
    private void updateSports(final String headline, final String apiKey){
        new Thread(){
            public void run(){
                final JSONObject json = FetchSports.getJSON(headline, apiKey);

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

            JSONObject sportsHeadline = json.getJSONArray("Sports").getJSONObject(0);
            //JSONObject sportsHeadline = json.getJSONObject("Sports");
            txtHeadline.setText(sportsHeadline.toString());


        }catch(Exception e){
            Log.e("SimpleSports", "One or more fields not found in the JSON data");
        }
    }




}
