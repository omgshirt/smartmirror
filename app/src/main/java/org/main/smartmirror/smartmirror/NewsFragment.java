package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class NewsFragment extends Fragment implements CacheManager.CacheListener {

    // the guardian api
    public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "all&order-by=newest&q=world&api-key=";
    public static String mDefNewsSection = "world";
    //public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&q=";
    public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&order-by=newest&q=";
    public static String mPostURL = "&api-key=";
    public static String mGuardURL = mPreURL + mDefNewsSection + mPostURL;
    public static String mNewsSection;

    // time in seconds before news data is considered old and is discarded
    private final int DATA_UPDATE_FREQUENCY = 1000;

    // I've updated NewsFragment to show the DataManager class. Create items as required.

    private CacheManager mCacheManager = null;

    private TextView txtNewsDesk;

    //public static String mArticleFullBody = "";
    public int numArticles = 10;
    public String article[] = new String[numArticles];
    public String hl[] = new String[numArticles];
    public String snippets[] = new String[numArticles];
    public String thumbs[] = new String[numArticles];
    public static String thumbnail = "";
    public static String body = "";
    public static String trailText = "";
    public static String webTitle = "";
    //public static String mHeadline = "";

    ListView newsFeed;
    public ArrayList<String> mHeadline = new ArrayList<String>();
    public ArrayList<String> mSnippet = new ArrayList<String>();
    public ArrayList<Uri> mImageURI = new ArrayList<Uri>();
    public ArrayList<String> mFullArticle = new ArrayList<String>();

    Handler mHandler = new Handler();
    private ArticleSelectedListener articleSelectedListener;

    public interface ArticleSelectedListener {
        void onArticleSelected(String title, String body);
    }

    public NewsFragment() {
    }

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

        // Initialize Items
        // mNewsSection = "world";
        txtNewsDesk = (TextView) view.findViewById(R.id.news_desk_title);
        newsFeed = (ListView) view.findViewById(R.id.list_news);

        //clearLayout();

        mNewsSection = getArguments().getString("arrI"); // change arrI
        //mGuardURL = mGuardURL + mGuardAPIKey;
        mGuardAPIKey = getString(R.string.guardian_api_key); // the guardian api key

        //updateNews(mGuardURL);

        txtNewsDesk.setText(mNewsSection.toUpperCase());
        startNewsUpdate();

        return view;
    }

    public void toNewsBodyFragment(int x) {
        //mArticleFullBody = article[x];
        //mHeadline = hl[x];
        //articleSelectedListener.onArticleSelected(hl[x], article[x]);
        articleSelectedListener.onArticleSelected(mHeadline.get(x), mFullArticle.get(x));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            articleSelectedListener = (ArticleSelectedListener) context;
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        articleSelectedListener = null;
    }

    public void startNewsUpdate() {
        Log.i(Constants.TAG, "starting news update");
        mGuardURL = mPreURL + mNewsSection + mPostURL + mGuardAPIKey;
        updateNews(mGuardURL);
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
            if (message.contains(Constants.SCROLL_DOWN) || message.contains(Constants.SCROLL_UP)) {
                int position = 0;
                if (message.contains(Constants.SCROLL_DOWN)) {
                    position = position + 5;
                } else if (message.contains(Constants.SCROLL_UP)) {
                    position = position - 5;
                    if (position < 0) position = 0;
                }
                VoiceScroll sl = new VoiceScroll();
                sl.voiceListView(message,newsFeed, position);
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

        if (!mCacheManager.containsKey(mNewsSection)) {
            Log.i(Constants.TAG, mNewsSection + " does not exist, creating");
            startNewsUpdate();
        } else {
            renderNews((JSONObject) mCacheManager.get(mNewsSection));
            if (mCacheManager.isExpired(mNewsSection)) {
                Log.i(Constants.TAG, mNewsSection + " expired. Refreshing...");
                startNewsUpdate();
            }
        }
    }

    /**
     * When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     * We're interested in the 'inputAction' intent, which carries any inputs send to
     * MainActivity from voice recognition, the remote control, etc.
     */
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));

        for (String name : Constants.NEWS_DESKS) {
            if (name.equals(mNewsSection)) {
                mCacheManager.registerCacheListener(name, this);
                Log.i("NEWS CACHE", "register " + name);
            }
        }

    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCacheManager.unRegisterCacheListener(mNewsSection, this);
        Log.i("NEWS CACHE", "unregister " + mNewsSection);
    }

    // Get news headlines from api and display
    private void updateNews(final String query) {
        new Thread() {
            public void run() {
                final JSONObject json = FetchURL.getJSON(query);
                if (json == null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            ((MainActivity) getActivity()).showToast(getString(R.string.news_err),
                                    Gravity.CENTER, Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                updateNewsCache(json);
                                //Log.i("NEWS ", json.toString());
                                renderNews(json);
                            } catch (Exception e) {Log.i("render news", e.toString());}

                        }
                    });
                }
            }
        }.start();
    }

    private void updateNewsCache(JSONObject data) {

        for (String name : Constants.NEWS_DESKS) {
            if (name.equals(mNewsSection)) {
                mCacheManager.addCache(name, data, DATA_UPDATE_FREQUENCY);
                Log.i("NEWS CACHE", "updating " + name);
            }
        }

    }

    private void renderNews(JSONObject json) {
        try {
            //Log.i("NEWS JSON", json.toString());
            JSONObject response = null;
            JSONObject results = null;
            JSONObject fields = null;

            int i = 0;

            while (i < numArticles) {
                response = json.getJSONObject("response");
                results = response.getJSONArray("results").getJSONObject(i);
                webTitle = results.getString("webTitle");
                //hl[i] = webTitle;
                mHeadline.add(webTitle);
                fields = results.getJSONObject("fields");
                body = fields.getString("body");
                //article[i] = body;
                mFullArticle.add(body);
                trailText = fields.getString("trailText");
                //snippets[i] = trailText;
                mSnippet.add(trailText);
                try {
                    thumbnail = fields.getString("thumbnail");
                    //thumbs[i] = thumbnail;
                    mImageURI.add(Uri.parse(thumbnail));
                } catch (Exception e) {
                    thumbs[i] = "@drawable/guardian";
                }
                i++;
            }


        } catch (Exception e) {
            Log.e("NEWS ERROR", e.toString());
        }

        ArrayList<CustomListViewObject> objects = new ArrayList<CustomListViewObject>();
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), objects);
        try {
            for(int j = 0; j < numArticles; j++){
                CustomListViewObject co = new CustomListViewObject(mHeadline.get(j),mSnippet.get(j),mImageURI.get(j));
                objects.add(co);
                customAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {Log.i("NEWS", e.toString());}
        newsFeed.setAdapter(customAdapter);
    }

    /**
     * Callback from CacheManager
     */
    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(mNewsSection)) {
            startNewsUpdate();
            Log.i("NEWS CACHE", "updating expired cache" + cacheName);
        }

    }

    /**
     * Callback from CacheManager
     */
    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startNewsUpdate() will refresh the views.
    }

}
