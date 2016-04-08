package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class TwitterFragment extends Fragment implements CacheManager.CacheListener{

    public static ListView twitterFeed;

    public static ArrayList<String> mUsers = new ArrayList<String>();
    public static ArrayList<String> mTweets = new ArrayList<String>();
    public static ArrayList<String> mUsersAt = new ArrayList<String>();
    public static ArrayList<Uri> mUri = new ArrayList<Uri>();

    public int twitterFeedPosition = 5;

    public static CacheManager mCacheManager = null;

    // time in seconds before twitter data is considered old and is discarded
    public static final int DATA_UPDATE_FREQUENCY = 61;
    public static final String TWITTER_CACHE = "twitter cache";


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));
        View view = inflater.inflate(R.layout.twitter_fragment, container, false);
        mCacheManager = CacheManager.getInstance();
        twitterFeed = (ListView)view.findViewById(R.id.list_twitter);

        return view;
    }



    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("TwitterArrayList ", "Got message:\"" + message +"\"");

            if (message.contains(Constants.SCROLL_DOWN) || message.contains(Constants.SCROLL_UP)) {
                VoiceScroll sl = new VoiceScroll();
                int numItemsInFeed = twitterFeed.getAdapter().getCount();
                if (message.contains(Constants.SCROLL_DOWN)) {
                    twitterFeedPosition = twitterFeedPosition + 5;
                    if (twitterFeedPosition >= numItemsInFeed)
                        twitterFeedPosition = numItemsInFeed;
                } else if (message.contains(Constants.SCROLL_UP)) {
                    twitterFeedPosition = twitterFeedPosition - 8;
                    if (twitterFeedPosition >= numItemsInFeed) {
                        twitterFeedPosition = twitterFeedPosition - 5;
                    }
                    else if (twitterFeedPosition <= 0) {
                        twitterFeedPosition = 0;
                    }
                }
                System.out.println("TWITTER FEED POSITION " + twitterFeedPosition);
                sl.scrollListView(message,twitterFeed, twitterFeedPosition);
                if (twitterFeedPosition == 0) twitterFeedPosition = 5;
            }
        }
    };

    public void renderTwitter() {
        ArrayList<CustomListViewObject> objects = new ArrayList<CustomListViewObject>();
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), objects, false);
        try {
            for(int i = 0; i < 10; i++){
                CustomListViewObject co = new CustomListViewObject(mUsers.get(i),mTweets.get(i),mUri.get(i),null);
                objects.add(co);
            }
            TwitterFragment.twitterFeed.setAdapter(customAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for any cached twitter data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.
        if (!mCacheManager.containsKey(TWITTER_CACHE)) {
                startTwitterUpdate();
            Log.i(Constants.TAG, "TwitterCache not found, creating cache..");
        } else {
            renderTwitter();
            if (mCacheManager.isExpired(TWITTER_CACHE)) {
                Log.i(Constants.TAG, "TwitterCache expired. Refreshing...");
                startTwitterUpdate();
            }
        }
    }

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
        mCacheManager.registerCacheListener(TWITTER_CACHE, this);
        Log.i("TWITTER CACHE", "register twitter");
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCacheManager.unRegisterCacheListener(TWITTER_CACHE, this);
        Log.i("TWITTER CACHE", "unregister twitter");
    }

    public void startTwitterUpdate() {
        new TwitterASyncTask().execute();
    }

    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(TWITTER_CACHE)) {
            mUri.clear();
            mUsers.clear();
            mUsersAt.clear();
            mTweets.clear();
            startTwitterUpdate();
        }
        Log.i("TWITTER CACHE", "updating expired cache" + cacheName);

    }

    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startTwitterUpdate() will refresh the views.
    }

}
