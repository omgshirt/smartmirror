package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;


import io.fabric.sdk.android.Fabric;

public class TwitterFragment extends Fragment {

    int mTweetNumber = 0;
    TweetView tweetView;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));

        View view = inflater.inflate(R.layout.twitter_fragment, container, false);

        // show initial tweet
        final ViewGroup parentView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        long tweetIds[] = {631879971628183552L, 503435417459249153L, 510908133917487104L, 473514864153870337L, 477788140900347904L};
        long currentTweet = tweetIds[mTweetNumber];
        TweetUtils.loadTweet(currentTweet, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //TweetView tweetView = new TweetView(getActivity(), result.data);
                tweetView = new TweetView(getActivity(), result.data);
                parentView.addView(tweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });



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
            Log.d("Twitter ", "Got message:\"" + message +"\"");
            switch (message) {
                case MainActivity.mNEXTTWEET:
                    mTweetNumber++;
                    tweetView.removeAllViews();
                    if(mTweetNumber > 4)
                        mTweetNumber = 0;
                    showTweets(mTweetNumber);
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

    public void showTweets(int tw) {

        final ViewGroup parentView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        long tweetIds[] = {631879971628183552L, 503435417459249153L, 510908133917487104L, 473514864153870337L, 477788140900347904L};
        long currentTweet = tweetIds[tw];
        TweetUtils.loadTweet(currentTweet, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                tweetView = new TweetView(getActivity(), result.data);
                parentView.addView(tweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });
    }


}
