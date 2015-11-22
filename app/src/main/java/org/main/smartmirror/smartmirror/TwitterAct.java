package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.twitter.sdk.android.Twitter;
//import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.*;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import retrofit.http.GET;
import retrofit.http.Query;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.util.function.Consumer;



public class TwitterAct extends Activity{

    private TwitterLoginButton loginButton;
    private TextView mStatus;

    TwitterSession mSession;
    long mUserID;
    String mScreenName;
    Handler mHandler = new Handler();

    public static String mAuthToken;
    public static String mAuthSecret;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new com.twitter.sdk.android.Twitter(authConfig));

        setContentView(R.layout.twitter_login_fragment);
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mStatus = (TextView)findViewById(R.id.status);
        mStatus.setText("Status: Ready");

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String output = "Status: " +
                        "Your login was successful " +
                        result.data.getUserName() +
                        "\nAuth Token Received: " +
                        result.data.getAuthToken().token;

                mStatus.setText(output);
                mSession = result.data;
                mUserID = mSession.getUserId();
                mScreenName = mSession.getUserName();
                String msg = "@" + mSession.getUserName() + " logged in! (#" + mUserID + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                Long id = mSession.getId();
                Log.i("ID: ", id.toString());
                long tweetID = 631879971628183552L;
                //showTweet(tweetID);
                mAuthToken = result.data.getAuthToken().token;
                mAuthSecret = result.data.getAuthToken().secret;
                Log.i("auth token ", mAuthToken);
                Log.i("auth token ", mAuthSecret);
                Constants.TWITTER_ACCESS_TOKEN = mAuthToken;
                Constants.TWITTER_ACCESS_SECRET = mAuthSecret;

                /*ConfigurationBuilder cb = new ConfigurationBuilder();

                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY)
                        .setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET)
                        .setOAuthAccessToken(Constants.TWITTER_ACCESS_TOKEN)
                        .setOAuthAccessTokenSecret(Constants.TWITTER_ACCESS_SECRET);

                TwitterFactory tf = new TwitterFactory(cb.build());
                Twitter twitter = tf.getInstance();
                try {
                    Status status = twitter.updateStatus("test");
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                /*String url = "https://api.twitter.com/1.1/statuses/home_timeline.json";
                String tUser = url + mScreenName;
                Log.i("url being passed ", tUser);
                updateFeed(url);*/


                //tweets(mScreenName);

                /*ConfigurationBuilder cb = new ConfigurationBuilder();

                cb.setOAuthConsumerKey(mTWITTER_KEY);
                cb.setOAuthConsumerSecret(mTWITTER_SECRET);
                cb.setOAuthAccessToken(mACCESSTOKEN);
                cb.setOAuthAccessTokenSecret(mACCESSSECRET);*/
                //Twitter twitter = new TwitterFactory(cb.build()).getInstance();

               /* TwitterFactory factory = new TwitterFactory();
                Twitter twitter = factory.getInstance();
                AccessToken accestoken = new AccessToken(mACCESSTOKEN, mACCESSSECRET);
                twitter.setOAuthAccessToken(accestoken);
                try {
                    List<Status> statuses;
                    String user;
                    try {
                        user = twitter.getScreenName();
                        statuses = twitter.getUserTimeline();
                        Log.i("user ", user);
                        for (Status status : statuses) {
                            Log.i("STATUS ","@" + status.getUser().getScreenName() + " - " + status.getText());
                        }
                    }catch (Exception e) {

                    }
                } catch (TwitterException te) {
                    te.printStackTrace();
                    //System.out.println("Failed to get timeline: " + te.getMessage());
                    //System.exit(-1);

                }*/


                /*ConfigurationBuilder cb = new ConfigurationBuilder();

                cb.setOAuthConsumerKey(mTWITTER_KEY);
                cb.setOAuthConsumerSecret(mTWITTER_SECRET);
                cb.setOAuthAccessToken(result.data.getAuthToken().token);
                cb.setOAuthAccessTokenSecret(result.data.getAuthToken().secret);*/

                /*java.util.List statuses = null;
                Twitter twitter = new TwitterFactory(cb.build()).getInstance();
                String userName = result.data.getUserName();
                String twArray;

                try {
                    statuses = twitter.getUserTimeline(userName);
                    Status status = (Status)statuses.get(1);
                    twArray = status.getUser().getName() + ": " + status.getText();
                    Log.i("TWITTER ", twArray);
                }
                catch(Exception e) {
                    Log.i("TWITTER ", " WTF");
                }*/



               /* ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(mCONSUMER_KEY)
                        .setOAuthConsumerSecret(
                                mCONSUMER_SECRET)
                        .setOAuthAccessToken(
                                mSession.getAuthToken().token)
                        .setOAuthAccessTokenSecret(
                                mSession.getAuthToken().secret);
                TwitterFactory tf = new TwitterFactory(cb.build());
                twitter = tf.getInstance();
                long user;
                user = mUserID;
                getUserTimeLine(mUserID);*/
                /*try{
                    ResponseList<Status> statuses = twitter.getUserTimeline(mUserID);
                    Log.i("twitter??? ", statuses.toString());

                }catch(Exception e) {
                    Log.i("time line " , " ERROR !!!");
                }*/
                /*for (Status status : statuses) {
                    //System.out.println(status.getText());
                    Log.i("time line " , status.getText());
                }*/
                /*try {
                    List<Status> statuses;

                    statuses = twitter.getUserTimeline(user);
                    Log.i("Status Count", statuses.size() + " Feeds");
                    //String twt = twitter.getUserTimeline(user).toString();
                    //Log.i("Tweets: ", twt);

                } catch (Exception e) {
                    Log.i("twit ", " exception");
                }*/

                /*ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(mCONSUMER_KEY);
                builder.setOAuthConsumerSecret(mCONSUMER_SECRET);
                builder.setOAuthAccessToken(mTWITTER_KEY);
                builder.setOAuthAccessTokenSecret(mTWITTER_SECRET);
                Configuration configuration = builder.build();
                TwitterFactory factory = new TwitterFactory(configuration);
                Twitter twitter = factory.getInstance();
                Paging page = new Paging();
                page.setCount(100);
                List<Status> statuses = new ArrayList<Status>();
                try{
                    statuses = twitter.getUserTimeline(mSession.getUserName(), page);
                    for (Status status : statuses) {
                        System.out.println("title is : " + status.getText());
                    }
                }catch(Exception e) {
                    Log.i("TWITTER ", "ERROR");
                }

                getUserTimeLine(mUserID);*/


            }

            @Override
            public void failure(TwitterException exception) {
                mStatus.setText("Status: Login Failed");
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });



    }

    /*public void getTwitter(View v) {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(Constants.TWITTER_ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(Constants.TWITTER_ACCESS_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        try {
            Status status = twitter.updateStatus("test");
            Log.i("STATUS ","SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("STATUS ", "FAIL");
        }
    }*/
 /*   void getUserTimeLine(long userID) {
        try{
            ResponseList<Status> statuses = twitter.getUserTimeline(userID);
            for (Status status : statuses) {
                //System.out.println(status.getText());
                Log.i("time line " , status.getText());
            }
        }catch(Exception e) {
            Log.i("time line " , " ERROR !!!");
        }

    }*/


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
                                    "TWITTER ERROR!@#$@!",
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
            Log.e("JSON ERROR", e.toString());
        }
    }

    /*public void tweets(String user) {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setOAuthConsumerKey(mTWITTER_KEY);
        cb.setOAuthConsumerSecret(mTWITTER_SECRET);
        cb.setOAuthAccessToken(mACCESSTOKEN);
        cb.setOAuthAccessTokenSecret(mACCESSSECRET);

        //Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        //Configuration configuration = cb.build();

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        //AccessToken accesstoken = new AccessToken(twitter.getConfiguration().getOAuthConsumerKey(), twitter.getConfiguration().getOAuthConsumerSecret());
        AccessToken accesstoken = new AccessToken(mTWITTER_KEY,mTWITTER_SECRET);
        twitter.setOAuthAccessToken(accesstoken);
        Log.i("Auth Token ",accesstoken.toString());
        try {
            List<Status> statuses;
            //String user;
            try {
                //user = twitter.getScreenName();
                //statuses = twitter.getUserTimeline();
                Log.i("user ", user);
                //for (Status status : statuses) {
                //    Log.i("STATUS ","@" + status.getUser().getScreenName() + " - " + status.getText().toString());
               // }
            }catch (Exception e) {
                Log.i("STATUS ", " FAIL");
            }
            //updateFeed(tUser);
        } catch (TwitterException te) {
            te.printStackTrace();
            //System.out.println("Failed to get timeline: " + te.getMessage());
            //System.exit(-1);

        }
    }*/

    //to main activity
    public void toMain(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
