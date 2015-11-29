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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;


import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

public class TwitterFragment extends Fragment {

    int mTweetNumber = 0;
    TweetView mTweetView;
    Handler mHandler = new Handler();

    private TextView mStatus1;
    private TextView mStatus2;
    private TextView mStatus3;
    private TextView mStatus4;
    private TextView mStatus5;
    private TextView mStatus6;
    private TextView mStatus7;
    private TextView mStatus8;
    private TextView mStatus9;
    private TextView mStatus10;
    private TextView mStatus11;

    private Button mTwitterLogin;
    private Button mTwitterButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));

        View view = inflater.inflate(R.layout.twitter_fragment, container, false);

        /*// show initial tweet
        final ViewGroup parentView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        long tweetIds[] = {631879971628183552L, 503435417459249153L, 510908133917487104L, 473514864153870337L, 477788140900347904L};
        long currentTweet = tweetIds[mTweetNumber];
        *//*TweetUtils.loadTweet(currentTweet, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //TweetView tweetView = new TweetView(getActivity(), result.data);
                mTweetView = new TweetView(getActivity(), result.data);
                parentView.addView(mTweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });*/

        //mTwitterButton = (Button)view.findViewById(R.id.btn_twitter);
        //mTwitterLogin = (Button)view.findViewById(R.id.btn_login);
        /*mTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterAsync();
            }
        });*/

        /*mTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLogin();
            }
        });*/

        mStatus1 = (TextView)view.findViewById(R.id.status1);
        mStatus2 = (TextView)view.findViewById(R.id.status2);
        mStatus3 = (TextView)view.findViewById(R.id.status3);
        mStatus4 = (TextView)view.findViewById(R.id.status4);
        mStatus5 = (TextView)view.findViewById(R.id.status5);
        mStatus6 = (TextView)view.findViewById(R.id.status6);
        mStatus7 = (TextView)view.findViewById(R.id.status7);
        mStatus8 = (TextView)view.findViewById(R.id.status8);
        mStatus9 = (TextView)view.findViewById(R.id.status9);
        mStatus10 = (TextView)view.findViewById(R.id.status10);
        mStatus11 = (TextView)view.findViewById(R.id.status11);

        mStatus1.setText("");
        mStatus2.setText("");
        mStatus3.setText("");
        mStatus4.setText("");
        mStatus5.setText("");
        mStatus6.setText("");
        mStatus7.setText("");
        mStatus8.setText("");
        mStatus9.setText("");
        mStatus10.setText("");
        mStatus11.setText("");

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
                case Constants.mGet:
                    twitterAsync();
                    break;
                case Constants.mRefresh:
                    twitterAsync();
                    break;
                case Constants.mLogin:
                    twitterLogin();
                    break;

                /*case MainActivity.mNEXTTWEET:
                    mTweetNumber++;
                    mTweetView.removeAllViews();
                    if(mTweetNumber > 4)
                        mTweetNumber = 0;
                    showTweets(mTweetNumber);
                    break;*/
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

    /*public void showTweets(int tw) {

        final ViewGroup parentView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        long tweetIds[] = {631879971628183552L, 503435417459249153L, 510908133917487104L, 473514864153870337L, 477788140900347904L};
        long currentTweet = tweetIds[tw];
        *//*TweetUtils.loadTweet(currentTweet, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //tweetView = new TweetView(getActivity(), result.data);
                //parentView.addView(tweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });*//*
    }*/

    //to twitter login activity
    public void twitterLogin() {
        Intent intent = new Intent(getContext(), TwitterAct.class);
        startActivity(intent);
    }

    public void twitterAsync() {
        new TwitterASyncTask().execute();

        try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        String txt0 = "<b>" + Constants.mUser[0] + "</b> " + "<br>" + Constants.mStatus[0] + "<br>";
        mStatus1.setText(Html.fromHtml(txt0));

        String txt1 = "<b>" + Constants.mUser[1] + "</b> " + "<br>" + Constants.mStatus[1] + "<br>";
        mStatus2.setText(Html.fromHtml(txt1));

        String txt2 = "<b>" + Constants.mUser[2] + "</b> " + "<br>" + Constants.mStatus[2] + "<br>";
        mStatus3.setText(Html.fromHtml(txt2));

        String txt3 = "<b>" + Constants.mUser[3] + "</b> " + "<br>" + Constants.mStatus[3] + "<br>";
        mStatus4.setText(Html.fromHtml(txt3));

        String txt4 = "<b>" + Constants.mUser[4] + "</b> " + "<br>" + Constants.mStatus[4] + "<br>";
        mStatus5.setText(Html.fromHtml(txt4));

        String txt5 = "<b>" + Constants.mUser[5] + "</b> " + "<br>" + Constants.mStatus[5] + "<br>";
        mStatus6.setText(Html.fromHtml(txt5));

        String txt6 = "<b>" + Constants.mUser[6] + "</b> " + "<br>" + Constants.mStatus[6] + "<br>";
        mStatus7.setText(Html.fromHtml(txt6));

        String txt7 = "<b>" + Constants.mUser[7] + "</b> " + "<br>" + Constants.mStatus[7] + "<br>";
        mStatus8.setText(Html.fromHtml(txt7));

        String txt8 = "<b>" + Constants.mUser[8] + "</b> " + "<br>" + Constants.mStatus[8] + "<br>";
        mStatus9.setText(Html.fromHtml(txt8));

        String txt9 = "<b>" + Constants.mUser[9] + "</b> " + "<br>" + Constants.mStatus[9] + "<br>";
        mStatus10.setText(Html.fromHtml(txt9));

        String txt10 = "<b>" + Constants.mUser[10] + "</b> " + "<br>" + Constants.mStatus[10] + "<br>";
        mStatus11.setText(Html.fromHtml(txt10));
    }

}
