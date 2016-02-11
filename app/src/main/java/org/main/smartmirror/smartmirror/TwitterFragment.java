package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
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

    private ImageView mPP1;
    private ImageView mPP2;
    private ImageView mPP3;
    private ImageView mPP4;
    private ImageView mPP5;
    private ImageView mPP6;
    private ImageView mPP7;
    private ImageView mPP8;
    private ImageView mPP9;
    private ImageView mPP10;
    private ImageView mPP11;

    private Button mTwitterLogin;
    private Button mTwitterGet;

    public static String mTwitterPreAPI = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";
    public static String mTwitterPostAPI = "&count=2";
    public static String mTwitterUser;
    public static String mTwitterAPI;
    public static String mTwitterTokenURL = "https://api.twitter.com/oauth/access_token";
    public static String mTwitterAuthURL = "https://api.twitter.com/oauth/authenticate?oauth_token=";

    public static String mUser[] = new String[100];
    public static String mStatus[] = new String[100];
    public static String mUserAt[] = new String[100];
    public static Uri mUrl[] = new Uri[100]; // profile image url

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));

        View view = inflater.inflate(R.layout.twitter_fragment, container, false);

        /*// show initial tweet
        final ViewGroup parentView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        long tweetIds[] = {631879971628183552L, 503435417459249153L, 510908133917487104L, 473514864153870337L, 477788140900347904L};
        long currentTweet = tweetIds[mTweetNumber];
        *//*TweetUtils.loadTweet(currentTweet, new Callback<TwitterTweet>() {
            @Override
            public void success(Result<TwitterTweet> result) {
                //TweetView tweetView = new TweetView(getActivity(), result.data);
                mTweetView = new TweetView(getActivity(), result.data);
                parentView.addView(mTweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load TwitterTweet failure", exception);
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

        mTwitterLogin = (Button)view.findViewById(R.id.toLogin);
        mTwitterGet = (Button)view.findViewById(R.id.pullTweets);

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

        mPP1 = (ImageView)view.findViewById(R.id.pp1);
        mPP2 = (ImageView)view.findViewById(R.id.pp2);
        mPP3 = (ImageView)view.findViewById(R.id.pp3);
        mPP4 = (ImageView)view.findViewById(R.id.pp4);
        mPP5 = (ImageView)view.findViewById(R.id.pp5);
        mPP6 = (ImageView)view.findViewById(R.id.pp6);
        mPP7 = (ImageView)view.findViewById(R.id.pp7);
        mPP8 = (ImageView)view.findViewById(R.id.pp8);
        mPP9 = (ImageView)view.findViewById(R.id.pp9);
        mPP10 = (ImageView)view.findViewById(R.id.pp10);
        mPP11 = (ImageView)view.findViewById(R.id.pp11);

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

        mTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLogin();
            }
        });
        mTwitterGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*TwitterSession session = TwitterArrayList.getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                TwitterActivity.mAuthToken = authToken.token;
                TwitterActivity.mAuthSecret = authToken.secret;*/

                mTwitterAPI = mTwitterPreAPI+TwitterActivity.mScreenName+mTwitterPostAPI;
                pullTweets(mTwitterAPI);
                //twitterAsync();

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
            Log.d("TwitterArrayList ", "Got message:\"" + message +"\"");
            switch (message) {
                case Constants.mGet:
                    //twitterAsync();
                    break;
                case Constants.mRefresh:
                    //twitterAsync();
                    Toast.makeText(getActivity(),"TwitterArrayList Feed Refreshed",Toast.LENGTH_LONG).show();
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
        *//*TweetUtils.loadTweet(currentTweet, new Callback<TwitterTweet>() {
            @Override
            public void success(Result<TwitterTweet> result) {
                //tweetView = new TweetView(getActivity(), result.data);
                //parentView.addView(tweetView);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load TwitterTweet failure", exception);
            }
        });*//*
    }*/


    private void pullTweets(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.twitter_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run(){
                            Log.i("TWEETS ", json.toString());
                            //renderTweets(json);
                        }
                    });
                }
            }
        }.start();
    }

    //to twitter login activity
    public void twitterLogin() {
        Intent intent = new Intent(getContext(), TwitterActivity.class);
        startActivity(intent);
    }


    public void twitterAsync() {
        //new TwitterASyncTask().execute();

        /*try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        *//* do this once you build txtListTweets
        for (int i=0; i <= 10; i++) {
            String txt = "<b>" + Constants.mUser[i] + "</b> " + "<br>" + Constants.mStatus[i] + "<br>";
            txtListTweets.get(i).setText(Html.fromHtml(txt));
        }
        *//*

        String txt0 = "<b>" + mUser[0] + "</b> " + "<br>" + mStatus[0] + "<br>";
        mStatus1.setText(Html.fromHtml(txt0));
        Picasso.with(getContext()).load(mUrl[0]).fit().centerInside().into(mPP1);

        String txt1 = "<b>" + mUser[1] + "</b> " + "<br>" + mStatus[1] + "<br>";
        mStatus2.setText(Html.fromHtml(txt1));
        Picasso.with(getContext()).load(mUrl[1]).fit().centerInside().into(mPP2);

        String txt2 = "<b>" + mUser[2] + "</b> " + "<br>" + mStatus[2] + "<br>";
        mStatus3.setText(Html.fromHtml(txt2));
        Picasso.with(getContext()).load(mUrl[2]).fit().centerInside().into(mPP3);

        String txt3 = "<b>" + mUser[3] + "</b> " + "<br>" + mStatus[3] + "<br>";
        mStatus4.setText(Html.fromHtml(txt3));
        Picasso.with(getContext()).load(mUrl[3]).fit().centerInside().into(mPP4);

        String txt4 = "<b>" + mUser[4] + "</b> " + "<br>" + mStatus[4] + "<br>";
        mStatus5.setText(Html.fromHtml(txt4));
        Picasso.with(getContext()).load(mUrl[4]).fit().centerInside().into(mPP5);

        String txt5 = "<b>" + mUser[5] + "</b> " + "<br>" + mStatus[5] + "<br>";
        mStatus6.setText(Html.fromHtml(txt5));
        Picasso.with(getContext()).load(mUrl[5]).fit().centerInside().into(mPP6);

        String txt6 = "<b>" + mUser[6] + "</b> " + "<br>" + mStatus[6] + "<br>";
        mStatus7.setText(Html.fromHtml(txt6));
        Picasso.with(getContext()).load(mUrl[6]).fit().centerInside().into(mPP7);

        String txt7 = "<b>" + mUser[7] + "</b> " + "<br>" + mStatus[7] + "<br>";
        mStatus8.setText(Html.fromHtml(txt7));
        Picasso.with(getContext()).load(mUrl[7]).fit().centerInside().into(mPP8);

        String txt8 = "<b>" + mUser[8] + "</b> " + "<br>" + mStatus[8] + "<br>";
        mStatus9.setText(Html.fromHtml(txt8));
        Picasso.with(getContext()).load(mUrl[8]).fit().centerInside().into(mPP9);

        String txt9 = "<b>" + mUser[9] + "</b> " + "<br>" + mStatus[9] + "<br>";
        mStatus10.setText(Html.fromHtml(txt9));
        Picasso.with(getContext()).load(mUrl[9]).fit().centerInside().into(mPP10);

        String txt10 = "<b>" + mUser[10] + "</b> " + "<br>" + mStatus[10] + "<br>";
        mStatus11.setText(Html.fromHtml(txt10));
        Picasso.with(getContext()).load(mUrl[10]).fit().centerInside().into(mPP11);*/
    }

}
