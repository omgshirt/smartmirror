package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.*;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetUi;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit.http.GET;
import retrofit.http.Query;

public class TwitterAct extends Activity{
    private TwitterLoginButton loginButton;
    private static final String mTWITTER_KEY = "mQ51h9ZbAz9Xk2AZtsUBJAGlx";
    private static final String mTWITTER_SECRET = "uSRCxg6AqE9DyIiuKjVD2ZzKC7CsGmuUcEljx2yafBwYHW74Rt";
    TwitterSession mSession;
    long mUserID;
    Handler mHandler = new Handler();
    String mTwitterQuery = "https://api.twitter.com/1.1/statuses/show/210462857140252672.json";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(mTWITTER_KEY, mTWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.twitter_login_fragment);
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                mSession = result.data;
                mUserID = mSession.getUserId();
                String msg = "@" + mSession.getUserName() + " logged in! (#" + mUserID + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                long tweetID = 631879971628183552L;
                showTweet(tweetID);
                mTwitterQuery="https://api.twitter.com/1.1/statuses/";
                String tUser = "https://api.twitter.com/1.1/statuses/user_timeline.json";
                updateFeed(tUser);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

    }



    public void showTweet(long tweetId) {
        final ViewGroup parentView = (ViewGroup) getWindow().getDecorView().getRootView();
        //long tweetId = 631879971628183552L;
        TweetUtils.loadTweet(tweetId, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                TweetView tweetView = new TweetView(TwitterAct.this, result.data);
                parentView.addView(tweetView);

            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);

    }

    private void updateFeed(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(TwitterAct.this,
                                    TwitterAct.this.getString(R.string.twitter_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            renderTweets(json);
                        }
                    });
                }
            }
        }.start();

    }

    private void renderTweets(JSONObject json) {
        try {
                Log.i("Twitter_API", json.toString());
        }catch(Exception e){
            Log.e("SPORTS ERROR", e.toString());
        }
    }
}
