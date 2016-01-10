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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class NewsFragment extends Fragment{

    // the guardian api
    public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "all&order-by=newest&q=world&api-key=";
    public static String mNewsSection = "world";
    public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&q=";
    public static String mPostURL = "&api-key=";
    public static String mGuardURL = mPreURL + mNewsSection + mPostURL;

    private TextView mTxtHeadline;
    private TextView mTxtHeadline2;
    private TextView mTxtHeadline3;
    private TextView mTxtHeadline4;
    private TextView mTxtHeadline5;
    private TextView mTxtHeadline6;
    private TextView mTxtHeadline7;
    private TextView mTxtHeadline8;
    private TextView txtNewsDesk;

    Handler mHandler = new Handler();

    public NewsFragment() {}

    String mNewURL;
    String mGuardAPIKey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);

        // Initialize Items
        mNewsSection = "world";

        mTxtHeadline = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);
        mTxtHeadline6 = (TextView)view.findViewById(R.id.headline6);
        mTxtHeadline7 = (TextView)view.findViewById(R.id.headline7);
        mTxtHeadline8 = (TextView)view.findViewById(R.id.headline8);

        txtNewsDesk = (TextView)view.findViewById(R.id.txtNewsDesk);

        clearLayout();

        txtNewsDesk.setText(mNewsSection.toUpperCase());

        // set onClickListener
        mTxtHeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[0];
                Constants.mHeadline = Constants.hl[0];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[1];
                Constants.mHeadline = Constants.hl[1];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[2];
                Constants.mHeadline = Constants.hl[2];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[3];
                Constants.mHeadline = Constants.hl[3];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[4];
                Constants.mHeadline = Constants.hl[4];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[5];
                Constants.mHeadline = Constants.hl[5];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[6];
                Constants.mHeadline = Constants.hl[6];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });
        mTxtHeadline8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.mArticleFullBody = Constants.article[7];
                Constants.mHeadline = Constants.hl[7];
                clearLayout();
                Fragment fragment = new NewsBodyFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.news_fragment_frame, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit();
            }
        });

        mGuardAPIKey = getString(R.string.guardian_api_key); // the guardian api key
        updateNews(mDefaultGuardURL+mGuardAPIKey);

        return view;
    }

    private void clearLayout() {
        mTxtHeadline.setText("");
        mTxtHeadline2.setText("");
        mTxtHeadline3.setText("");
        mTxtHeadline4.setText("");
        mTxtHeadline5.setText("");
        mTxtHeadline6.setText("");
        mTxtHeadline7.setText("");
        mTxtHeadline8.setText("");
        txtNewsDesk.setText("");
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
                case Constants.BACK:
                    getFragmentManager().popBackStack();
                    break;
                case Constants.ONE:
                case Constants.FIRST:
                    Constants.mArticleFullBody = Constants.article[0];
                    Constants.mHeadline = Constants.hl[0];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.TWO:
                case Constants.SECOND:
                    Constants.mArticleFullBody = Constants.article[1];
                    Constants.mHeadline = Constants.hl[1];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.THREE:
                case Constants.THIRD:
                    Constants.mArticleFullBody = Constants.article[2];
                    Constants.mHeadline = Constants.hl[2];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.FOUR:
                case Constants.FOURTH:
                    Constants.mArticleFullBody = Constants.article[3];
                    Constants.mHeadline = Constants.hl[3];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.FIVE:
                case Constants.FIFTH:
                    Constants.mArticleFullBody = Constants.article[4];
                    Constants.mHeadline = Constants.hl[4];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.SIX:
                case Constants.SIXTH:
                    Constants.mArticleFullBody = Constants.article[5];
                    Constants.mHeadline = Constants.hl[5];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.SEVEN:
                case Constants.SEVENTH:
                    Constants.mArticleFullBody = Constants.article[6];
                    Constants.mHeadline = Constants.hl[6];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                case Constants.EIGHT:
                case Constants.EIGHTH:
                    Constants.mArticleFullBody = Constants.article[7];
                    Constants.mHeadline = Constants.hl[7];
                    clearLayout();
                    //Fragment fragment = new NewsBodyFragment();
                    //FragmentManager fm = getFragmentManager();
                    getFragmentManager().beginTransaction().replace(R.id.news_fragment_frame, new NewsBodyFragment())
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .commit();
                    break;
                default:
                    String[] urlArr = getResources().getStringArray(R.array.guardian_sections);
                    int i = 0;
                    while (i < urlArr.length) {
                        if (message.contains(urlArr[i])) {
                            mNewsSection = urlArr[i];
                            mGuardURL = mPreURL + mNewsSection + mPostURL;
                            mNewURL = mGuardURL + mGuardAPIKey;
                            Log.i("voice news desk: ", urlArr[i]);
                            txtNewsDesk.setText(mNewsSection.toUpperCase());
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
     *  We're interested in the 'inputAction' intent, which carries any inputs send to
     *  MainActivity from voice recognition, the remote control, etc.
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
                                    getActivity().getString(R.string.news_error),
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
            //Log.i("NEWS JSON", json.toString());
            JSONObject response = null;
            JSONObject results = null;
            JSONObject fields = null;

            int i = 0;


            while (i < Constants.numItems) {
                response = json.getJSONObject("response");
                results = response.getJSONArray("results").getJSONObject(i);
                Constants.webTitle = results.getString("webTitle");
                Constants.hl[i] = Constants.webTitle;
                fields = results.getJSONObject("fields");
                Constants.body = fields.getString("body");
                Constants.article[i] = Constants.body;
                Constants.trailText = fields.getString("trailText");
                Constants.snippets[i] = Constants.trailText;
                i++;
            }

            String txt0 = "<b>" + Constants.hl[0] + "</b> " + "<br>" + Constants.snippets[0] + "<br>";
            mTxtHeadline.setText(Html.fromHtml(txt0));

            String txt1 = "<b>" + Constants.hl[1] + "</b> " + "<br>" + Constants.snippets[1] + "<br>";
            mTxtHeadline2.setText(Html.fromHtml(txt1));

            String txt2 = "<b>" + Constants.hl[2] + "</b> " + "<br>" + Constants.snippets[2] + "<br>";
            mTxtHeadline3.setText(Html.fromHtml(txt2));

            String txt3 = "<b>" + Constants.hl[3] + "</b> " + "<br>" + Constants.snippets[3] + "<br>";
            mTxtHeadline4.setText(Html.fromHtml(txt3));

            String txt4 = "<b>" + Constants.hl[4] + "</b> " + "<br>" + Constants.snippets[4] + "<br>";
            mTxtHeadline5.setText(Html.fromHtml(txt4));

            String txt5 = "<b>" + Constants.hl[5] + "</b> " + "<br>" + Constants.snippets[5] + "<br>";
            mTxtHeadline6.setText(Html.fromHtml(txt5));

            String txt6 = "<b>" + Constants.hl[6] + "</b> " + "<br>" + Constants.snippets[6] + "<br>";
            mTxtHeadline7.setText(Html.fromHtml(txt6));

            String txt7 = "<b>" + Constants.hl[7] + "</b> " + "<br>" + Constants.snippets[7] + "<br>";
            mTxtHeadline8.setText(Html.fromHtml(txt7));


        }catch(Exception e){
            Log.e("NEWS ERROR", e.toString());
        }
    }

}
