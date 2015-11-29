package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.net.Uri;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

public class NewsFragment extends Fragment {
    public static String mPreURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3A";
    public static String mPostURL = "&sort=newest&api-key=";
    public static String mNewsDesk = "U.S.";
    //public static String mNewsDefault = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";
    public static String mNewsDefault ="http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3A";
    public static String mNYTURL = mPreURL + mNewsDesk + mPostURL;

    private TextView mTxtHeadline;
    private TextView mTxtHeadline2;
    private TextView mTxtHeadline3;
    private TextView mTxtHeadline4;
    private TextView mTxtHeadline5;
    private TextView mTxtHeadline6;
    private TextView mTxtHeadline7;
    private TextView mTxtHeadline8;
    private ImageButton mNYTButton;
    private TextView txtNewsDesk;

    Handler mHandler = new Handler();

    // this URL does some filtering so as to only retrieve the "headline" and "snippet" for each item,
    // without the full text and other extras (date, etc).
    // see the NYTIMES API console for JSON format
/*    private String sportsUrl = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3Asports&" +
            "begin_date=20151028&end_date=20151028&sort=newest&fl=headline%2Csnippet&page=0&api-key=";*/

    public NewsFragment() {}

    String mApiKey;
    String mNewURL;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);

        // Initialize Items
        mNewsDesk = "U.S.";

        mNYTButton  = (ImageButton)view.findViewById(R.id.btnNYTbranding);
        mTxtHeadline = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);
        mTxtHeadline6 = (TextView)view.findViewById(R.id.headline6);
        mTxtHeadline7 = (TextView)view.findViewById(R.id.headline7);
        mTxtHeadline8 = (TextView)view.findViewById(R.id.headline8);

        txtNewsDesk = (TextView)view.findViewById(R.id.txtNewsDesk);

        mTxtHeadline.setText("");
        mTxtHeadline2.setText("");
        mTxtHeadline3.setText("");
        mTxtHeadline4.setText("");
        mTxtHeadline5.setText("");
        mTxtHeadline6.setText("");
        mTxtHeadline7.setText("");
        mTxtHeadline8.setText("");

        txtNewsDesk.setText(mNewsDesk.toUpperCase());

        // set onClickListener
        mNYTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.nyt_url);
                Intent intntNYT = new Intent(Intent.ACTION_VIEW);
                intntNYT.setData(Uri.parse(url));
                startActivity(intntNYT);
            }
        });


        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }
        mApiKey = getString(R.string.nyt_api_key);

        String newsURL = this.getArguments().getString("url");
        newsURL += mApiKey;
        updateNews(newsURL);

        //updateNews(mNewsDefault+mNewsDesk+mPostURL+mApiKey);

        return view;

    }


    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtNewsDesk.setText("");
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            switch (message) {
                case MainActivity.BACK:
                    getFragmentManager().popBackStack();
                    break;
                default:
                    String[] urlArr = getResources().getStringArray(R.array.nyt_news_desk);
                    int i = 0;
                    while (i < urlArr.length) {
                        if (message.contains(urlArr[i])) {
                            mNewsDesk = urlArr[i];
                            mNYTURL = mPreURL + mNewsDesk + mPostURL;
                            mNewURL = mNYTURL + mApiKey;
                            Log.i("voice news desk: ", urlArr[i]);
                            txtNewsDesk.setText(mNewsDesk.toUpperCase());
                            updateNews(mNewURL);
                            break;
                        } else {
                            i++;
                            Log.i("I heard: ", message);
                        }
                    }
                    Log.d("News", "Got message:\"" + message + "\"");
                    break;
            }
        }
    };

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
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
            String hl[] = new String[10];

            JSONObject response = null;
            JSONObject docs = null;
            String snippet = null;
            JSONObject headline = null;

            int i = 0;
            int numFeeds = 8;
            while (i < numFeeds) {
                response = json.getJSONObject("response");
                docs = response.getJSONArray("docs").getJSONObject(i);
                snippet = docs.getString("snippet");
                headline = docs.getJSONObject("headline");
                hl[i] = headline.getString("main");
                newsFeed[i] = snippet;
                i++;
            }


            //mytextview.setText(Html.fromHtml(sourceString)); //format

            String txt0 = "<b>" + hl[0] + "</b> " + "<br>" + newsFeed[0] + "<br>";
            mTxtHeadline.setText(Html.fromHtml(txt0));

            String txt1 = "<b>" + hl[1] + "</b> " + "<br>" + newsFeed[1] + "<br>";
            mTxtHeadline2.setText(Html.fromHtml(txt1));

            String txt2 = "<b>" + hl[2] + "</b> " + "<br>" + newsFeed[2] + "<br>";
            mTxtHeadline3.setText(Html.fromHtml(txt2));

            String txt3 = "<b>" + hl[3] + "</b> " + "<br>" + newsFeed[3] + "<br>";
            mTxtHeadline4.setText(Html.fromHtml(txt3));

            String txt4 = "<b>" + hl[4] + "</b> " + "<br>" + newsFeed[4] + "<br>";
            mTxtHeadline5.setText(Html.fromHtml(txt4));

            String txt5 = "<b>" + hl[5] + "</b> " + "<br>" + newsFeed[5] + "<br>";
            mTxtHeadline6.setText(Html.fromHtml(txt5));

            String txt6 = "<b>" + hl[6] + "</b> " + "<br>" + newsFeed[6] + "<br>";
            mTxtHeadline7.setText(Html.fromHtml(txt6));

            String txt7 = "<b>" + hl[7] + "</b> " + "<br>" + newsFeed[7] + "<br>";
            mTxtHeadline8.setText(Html.fromHtml(txt7));

        }catch(Exception e){
            Log.e("NEWS ERROR", e.toString());
        }
    }

}
