package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

public class NewsFragment extends Fragment{

    // the guardian api
    public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "all&order-by=newest&q=world&api-key=";
    /*public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "thumbnail&order-by=newest&q=sports&api-key=";*/
    public static String mNewsSection = "world";
    public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&q=";
    public static String mPostURL = "&api-key=";
    public static String mGuardURL = mPreURL + mNewsSection + mPostURL;

    // time in minutes before news data is considered old and is discarded
    private final int DATA_UPDATE_FREQUENCY = 10;
    public static DataCache<JSONObject> mNewsCache = null;

    private TextView mTxtHeadline;
    private TextView mTxtHeadline2;
    private TextView mTxtHeadline3;
    private TextView mTxtHeadline4;
    private TextView mTxtHeadline5;
    private TextView mTxtHeadline6;
    private TextView mTxtHeadline7;
    private TextView mTxtHeadline8;
    private TextView txtNewsDesk;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private ImageView img4;
    private ImageView img5;
    private ImageView img6;
    private ImageView img7;
    private ImageView img8;


    Handler mHandler = new Handler();

    public NewsFragment() {}

    String mNewURL;
    String mGuardAPIKey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);

        // Initialize Items
        //mNewsSection = "world";

        mTxtHeadline = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);
        mTxtHeadline6 = (TextView)view.findViewById(R.id.headline6);
        mTxtHeadline7 = (TextView)view.findViewById(R.id.headline7);
        mTxtHeadline8 = (TextView)view.findViewById(R.id.headline8);

        txtNewsDesk = (TextView)view.findViewById(R.id.txtNewsDesk);

        img1 = (ImageView)view.findViewById(R.id.img1);
        img2 = (ImageView)view.findViewById(R.id.img2);
        img3 = (ImageView)view.findViewById(R.id.img3);
        img4 = (ImageView)view.findViewById(R.id.img4);
        img5 = (ImageView)view.findViewById(R.id.img5);
        img6 = (ImageView)view.findViewById(R.id.img6);
        img7 = (ImageView)view.findViewById(R.id.img7);
        img8 = (ImageView)view.findViewById(R.id.img8);

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

        return view;
    }

    public void startNewsUpdate(){
        mGuardAPIKey = getString(R.string.guardian_api_key); // the guardian api key
        updateNews(mDefaultGuardURL+mGuardAPIKey);
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

        img1.setImageResource(android.R.color.transparent);
        img2.setImageResource(android.R.color.transparent);
        img3.setImageResource(android.R.color.transparent);
        img4.setImageResource(android.R.color.transparent);
        img5.setImageResource(android.R.color.transparent);
        img6.setImageResource(android.R.color.transparent);
        img7.setImageResource(android.R.color.transparent);
        img8.setImageResource(android.R.color.transparent);
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
                    if(message.contains(Constants.HELP) || message.contains(Constants.HIDE)) {
                        txtNewsDesk.setText(mNewsSection.toUpperCase());
                    }
                    Log.d("News", "Got message:\"" + message + "\"");
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Check for any cached news data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.
        if (mNewsCache == null) {
            Log.i(Constants.TAG,"NewsCache does not exist, updating");
            startNewsUpdate();
        } else {
            renderNews(mNewsCache.getData());
            if (mNewsCache.isExpired()) {
                Log.i(Constants.TAG, "NewsCache expired. Refreshing..." );
                startNewsUpdate();
            }
        }
    }

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
                            updateNewsCache(json);
                            Log.i("NEWS ", json.toString());
                            renderNews(json);
                        }
                    });
                }
            }
        }.start();
    }
    private void updateNewsCache(JSONObject data){
        mNewsCache = new DataCache<>(data, DATA_UPDATE_FREQUENCY);
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
                try {
                    Constants.thumbnail = fields.getString("thumbnail");
                    Constants.thumbs[i] = Constants.thumbnail;
                }
                catch (Exception e) {
                    Constants.thumbs[i] = "@drawable/guardian";
                }
                i++;
            }


            String txt0 = "<b>" + Constants.hl[0] + "</b> " + "<br>" + Constants.snippets[0] + "<br>";
            mTxtHeadline.setText(Html.fromHtml(txt0));
            Picasso.with(getContext()).load(Constants.thumbs[0]).fit().centerInside().into(img1);

            String txt1 = "<b>" + Constants.hl[1] + "</b> " + "<br>" + Constants.snippets[1] + "<br>";
            mTxtHeadline2.setText(Html.fromHtml(txt1));
            Picasso.with(getContext()).load(Constants.thumbs[1]).fit().centerInside().into(img2);

            String txt2 = "<b>" + Constants.hl[2] + "</b> " + "<br>" + Constants.snippets[2] + "<br>";
            mTxtHeadline3.setText(Html.fromHtml(txt2));
            Picasso.with(getContext()).load(Constants.thumbs[2]).fit().centerInside().into(img3);

            String txt3 = "<b>" + Constants.hl[3] + "</b> " + "<br>" + Constants.snippets[3] + "<br>";
            mTxtHeadline4.setText(Html.fromHtml(txt3));
            Picasso.with(getContext()).load(Constants.thumbs[3]).fit().centerInside().into(img4);

            String txt4 = "<b>" + Constants.hl[4] + "</b> " + "<br>" + Constants.snippets[4] + "<br>";
            mTxtHeadline5.setText(Html.fromHtml(txt4));
            Picasso.with(getContext()).load(Constants.thumbs[4]).fit().centerInside().into(img5);

            String txt5 = "<b>" + Constants.hl[5] + "</b> " + "<br>" + Constants.snippets[5] + "<br>";
            mTxtHeadline6.setText(Html.fromHtml(txt5));
            Picasso.with(getContext()).load(Constants.thumbs[5]).fit().centerInside().into(img6);

            String txt6 = "<b>" + Constants.hl[6] + "</b> " + "<br>" + Constants.snippets[6] + "<br>";
            mTxtHeadline7.setText(Html.fromHtml(txt6));
            Picasso.with(getContext()).load(Constants.thumbs[6]).fit().centerInside().into(img7);

            String txt7 = "<b>" + Constants.hl[7] + "</b> " + "<br>" + Constants.snippets[7] + "<br>";
            mTxtHeadline8.setText(Html.fromHtml(txt7));
            Picasso.with(getContext()).load(Constants.thumbs[7]).fit().centerInside().into(img8);

        }catch(Exception e){
            Log.e("NEWS ERROR", e.toString());
        }
    }

}
