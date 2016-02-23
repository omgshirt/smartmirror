package org.main.smartmirror.smartmirror;

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
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class NewsFragment extends Fragment implements CacheManager.CacheListener{

    // the guardian api
    public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "all&order-by=newest&q=world&api-key=";
    public static String mDefNewsSection = "world";
    //public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&q=";
    public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&order-by=newest&q=";
    public static String mPostURL = "&api-key=";
    public static String mGuardURL = mPreURL + mDefNewsSection + mPostURL;
    public static String mNewsSection;

    // time in minutes before news data is considered old and is discarded
    private final int DATA_UPDATE_FREQUENCY = 1;

    // I've updated NewsFragment to show the DataManager class. Create items as required.

    public static final String WORLD_CACHE = "world";
    public static final String SPORTS_CACHE = "sports";
    public static final String TECH_CACHE = "tech";
    public static final String BUSINESS_CACHE = "business";
    public static final String SCIENCE_CACHE = "science";
    public static final String MEDIA_CACHE = "media";
    public static final String TRAVEL_CACHE = "travel";

    public static final String[] NEWS_DESKS = {
            "business",
            "media",
            "science",
            "sports",
            "tech",
            "travel",
            "world" };


    private CacheManager mCacheManager = null;

    private TextView mTxtHeadline1;
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

    public static String mArticleFullBody = "";
    public static int numItems = 10;
    public static String article[] = new String[numItems];
    public static String hl[] = new String[numItems];
    public static String snippets[] = new String[numItems];
    public static String thumbs[] = new String[numItems];
    public static String thumbnail = "";
    public static String body = "";
    public static String trailText = "";
    public static String webTitle = "";
    public static String mHeadline = "";

    ScrollView mScrollView;

    Handler mHandler = new Handler();
    ArticleSelectedListener articleSelectedListener;

    public interface ArticleSelectedListener {
        void onArticleSelected(String title, String body);
    }

    public NewsFragment() {}

    public static NewsFragment newInstance(String section) {
        Bundle args = new Bundle();
        args.putString("arrI", section);
        NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    static String mNewURL;
    static String mGuardAPIKey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        mCacheManager = CacheManager.getInstance();
        //
        // Initialize Items
        // mNewsSection = "world";

        mTxtHeadline1 = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);
        mTxtHeadline6 = (TextView)view.findViewById(R.id.headline6);
        mTxtHeadline7 = (TextView)view.findViewById(R.id.headline7);
        mTxtHeadline8 = (TextView)view.findViewById(R.id.headline8);

        txtNewsDesk = (TextView)view.findViewById(R.id.news_desk_title);

        img1 = (ImageView)view.findViewById(R.id.img1);
        img2 = (ImageView)view.findViewById(R.id.img2);
        img3 = (ImageView)view.findViewById(R.id.img3);
        img4 = (ImageView)view.findViewById(R.id.img4);
        img5 = (ImageView)view.findViewById(R.id.img5);
        img6 = (ImageView)view.findViewById(R.id.img6);
        img7 = (ImageView)view.findViewById(R.id.img7);
        img8 = (ImageView)view.findViewById(R.id.img8);

        mScrollView = (ScrollView)view.findViewById(R.id.scrollView2);

        clearLayout();

        mNewsSection = getArguments().getString("arrI");
        //mGuardURL = mGuardURL + mGuardAPIKey;
        mGuardAPIKey = getString(R.string.guardian_api_key); // the guardian api key

        //updateNews(mGuardURL);

        txtNewsDesk.setText(mNewsSection.toUpperCase());

        // set onClickListener
        mTxtHeadline1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(0);
            }
        });
        mTxtHeadline2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(1);
            }
        });
        mTxtHeadline3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(2);
            }
        });
        mTxtHeadline4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(3);
            }
        });
        mTxtHeadline5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(4);
            }
        });
        mTxtHeadline6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(5);
            }
        });
        mTxtHeadline7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(6);
            }
        });
        mTxtHeadline8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(7);
            }
        });

        return view;
    }

    public void toNewsBodyFragment(int x){
        mArticleFullBody = article[x];
        mHeadline = hl[x];
        articleSelectedListener.onArticleSelected(mHeadline, mArticleFullBody);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            articleSelectedListener = (ArticleSelectedListener)context;
        } catch (ClassCastException cce){
            cce.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        articleSelectedListener = null;
    }

    public void startNewsUpdate(){
        mGuardURL = mPreURL + mNewsSection + mPostURL + mGuardAPIKey;
        updateNews(mGuardURL);
    }

    private void clearLayout() {
        mTxtHeadline1.setText("");
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
                case Constants.ONE:
                case Constants.FIRST:
                    toNewsBodyFragment(0);
                    break;
                case Constants.TWO:
                case Constants.SECOND:
                    toNewsBodyFragment(1);
                    break;
                case Constants.THREE:
                case Constants.THIRD:
                    toNewsBodyFragment(2);
                    break;
                case Constants.FOUR:
                case Constants.FOURTH:
                    toNewsBodyFragment(3);
                    break;
                case Constants.FIVE:
                case Constants.FIFTH:
                    toNewsBodyFragment(4);
                    break;
                case Constants.SIX:
                case Constants.SIXTH:
                    toNewsBodyFragment(5);
                    break;
                case Constants.SEVEN:
                case Constants.SEVENTH:
                    toNewsBodyFragment(6);
                    break;
                case Constants.EIGHT:
                case Constants.EIGHTH:
                    toNewsBodyFragment(7);
                    break;
                default:
                    txtNewsDesk.setText(mNewsSection.toUpperCase());

            }
            /*if(message.contains(Constants.SCROLL_DOWN))
                mScrollView.scrollBy(0, -(0-mScrollView.getHeight()));
            else if(!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
                mScrollView.scrollBy(0, 0-mScrollView.getHeight());*/
            if (message.contains(Constants.SCROLL_DOWN) || message.contains(Constants.SCROLL_UP)) {
                VoiceScroll sl = new VoiceScroll();
                sl.voiceScrollView(message,mScrollView);
            }

        }
    };


    @Override
    public void onStart() {
        super.onStart();

        // Check for any cached news data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.

        // TODO: 2/3/2016 add switch case so only current news section is checked/updated
        // TODO: 2/3/2016 don't update when null/not prev visited


        for (String name : NEWS_DESKS) {
            String cacheName = name;
            if (!mCacheManager.containsKey(cacheName)) {
                Log.i(Constants.TAG, cacheName + " does not exist, creating");
                startNewsUpdate();
            } else if (mCacheManager.isExpired(name)){
                Log.i(Constants.TAG, cacheName + " expired. Refreshing...");
                startNewsUpdate();
                renderNews((JSONObject) mCacheManager.get(cacheName));
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

        for (String name : NEWS_DESKS) {
            String cacheName = name;
            if (mNewsSection.equals(cacheName)) {
                mCacheManager.registerCacheListener(cacheName, this);
                Log.i("NEWS CACHE", "register " + cacheName);
            }
        }

    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

        // TODO UNREGISTER ON PAUSE

        for (String name : NEWS_DESKS) {
            String cacheName = name;
            if (mNewsSection.equals(cacheName)) {
                mCacheManager.unRegisterCacheListener(cacheName, this);
                Log.i("NEWS CACHE", "unregister " + cacheName);
            }
        }

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
                            //Log.i("NEWS ", json.toString());
                            renderNews(json);
                        }
                    });
                }
            }
        }.start();
    }
    private void updateNewsCache(JSONObject data){

        for (String name : NEWS_DESKS) {
            String cacheName = name;
            if (mNewsSection.equals(cacheName)) {
                mCacheManager.addCache(cacheName, data, DATA_UPDATE_FREQUENCY);
                Log.i("NEWS CACHE", "updating " + cacheName);
            }
        }

    }

    private void renderNews(JSONObject json){
        try {
            //Log.i("NEWS JSON", json.toString());
            JSONObject response = null;
            JSONObject results = null;
            JSONObject fields = null;

            int i = 0;

            while (i < numItems) {
                response = json.getJSONObject("response");
                results = response.getJSONArray("results").getJSONObject(i);
                webTitle = results.getString("webTitle");
                hl[i] = webTitle;
                fields = results.getJSONObject("fields");
                body = fields.getString("body");
                article[i] = body;
                trailText = fields.getString("trailText");
                snippets[i] = trailText;
                try {
                    thumbnail = fields.getString("thumbnail");
                    thumbs[i] = thumbnail;
                }
                catch (Exception e) {
                    thumbs[i] = "@drawable/guardian";
                }
                i++;
            }


            String txt0 = "<b>" + hl[0] + "</b> " + "<br>" + snippets[0] + "<br>";
            mTxtHeadline1.setText(Html.fromHtml(txt0));
            Picasso.with(getContext()).load(thumbs[0]).fit().centerInside().into(img1);

            String txt1 = "<b>" + hl[1] + "</b> " + "<br>" + snippets[1] + "<br>";
            mTxtHeadline2.setText(Html.fromHtml(txt1));
            Picasso.with(getContext()).load(thumbs[1]).fit().centerInside().into(img2);

            String txt2 = "<b>" + hl[2] + "</b> " + "<br>" + snippets[2] + "<br>";
            mTxtHeadline3.setText(Html.fromHtml(txt2));
            Picasso.with(getContext()).load(thumbs[2]).fit().centerInside().into(img3);

            String txt3 = "<b>" + hl[3] + "</b> " + "<br>" + snippets[3] + "<br>";
            mTxtHeadline4.setText(Html.fromHtml(txt3));
            Picasso.with(getContext()).load(thumbs[3]).fit().centerInside().into(img4);

            String txt4 = "<b>" + hl[4] + "</b> " + "<br>" + snippets[4] + "<br>";
            mTxtHeadline5.setText(Html.fromHtml(txt4));
            Picasso.with(getContext()).load(thumbs[4]).fit().centerInside().into(img5);

            String txt5 = "<b>" + hl[5] + "</b> " + "<br>" + snippets[5] + "<br>";
            mTxtHeadline6.setText(Html.fromHtml(txt5));
            Picasso.with(getContext()).load(thumbs[5]).fit().centerInside().into(img6);

            String txt6 = "<b>" + hl[6] + "</b> " + "<br>" + snippets[6] + "<br>";
            mTxtHeadline7.setText(Html.fromHtml(txt6));
            Picasso.with(getContext()).load(thumbs[6]).fit().centerInside().into(img7);

            String txt7 = "<b>" + hl[7] + "</b> " + "<br>" + snippets[7] + "<br>";
            mTxtHeadline8.setText(Html.fromHtml(txt7));
            Picasso.with(getContext()).load(thumbs[7]).fit().centerInside().into(img8);

        }catch(Exception e){
            Log.e("NEWS ERROR", e.toString());
        }
    }

    /** Callback from CacheManager */
    @Override
    public void onCacheExpired(String cacheName) {

        for (String name : NEWS_DESKS) {
            cacheName = name;
            if (mNewsSection.equals(cacheName)) {
                startNewsUpdate();
                Log.i("NEWS CACHE", "updating expired cache" + cacheName);
            }
        }

    }

    /** Callback from CacheManager */
    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startWeatherUpdate() will refresh the views.
    }

}
