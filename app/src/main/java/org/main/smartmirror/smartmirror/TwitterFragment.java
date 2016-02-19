package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.ClipData;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

import io.fabric.sdk.android.Fabric;

public class TwitterFragment extends Fragment {

    Handler mHandler = new Handler();

    Handler mTimerHandler = new Handler();
    int mDelay = 61000; //milliseconds

    private Button mTwitterLogin;
    private Button mTwitterGet;

    public static String mUser[] = new String[100];
    public static String mStatus[] = new String[100];
    public static String mUserAt[] = new String[100];
    public static Uri mUrl[] = new Uri[100]; // profile image url

    public static ListView twitterFeed;

    public static ArrayList<String> mUsers = new ArrayList<String>();
    public static ArrayList<String> mTweets = new ArrayList<String>();
    public static ArrayList<String> mUsersAt = new ArrayList<String>();
    public static ArrayList<Uri> mUri = new ArrayList<Uri>();


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));

        View view = inflater.inflate(R.layout.twitter_fragment, container, false);

        mTwitterLogin = (Button)view.findViewById(R.id.toLogin);
        mTwitterGet = (Button)view.findViewById(R.id.pullTweets);


        twitterFeed = (ListView)view.findViewById(R.id.list_twitter);

        twitterAsync();

        mTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLogin();
            }
        });
        mTwitterGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterAsync();

            }
        });



        mTimerHandler.postDelayed(new Runnable(){
            public void run(){
                System.out.println("TIMER EXPIRED UPDATING TWITTER");
                twitterAsync();
                mTimerHandler.postDelayed(this, mDelay);
            }
        }, mDelay);

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
                    twitterAsync();
                    break;
                case Constants.mRefresh:
                    twitterAsync();
                    Toast.makeText(getActivity(),"TwitterArrayList Feed Refreshed",Toast.LENGTH_LONG).show();
                    break;
                case Constants.mLogin:
                    twitterLogin();
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
        new TwitterASyncTask().execute();
    }

}
