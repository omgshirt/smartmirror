package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {

    // fallback tokens, replaced once user signs in
    public static String TWITTER_ACCESS_TOKEN = "4202759960-FRC4u2oIMHECYgzsQJAtWG8TcHAsMWfF6cNigXG";
    public static String TWITTER_ACCESS_SECRET = "BbK7Ls2rwXrutUOnKsE5pZx8EajxRgUiMZO6P39edBZFZ";
    public static String accToken;


    @Override
    protected String doInBackground(String[] params) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        try {
            cb.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token");
            cb.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token");
            cb.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize");
            cb.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token");
            cb.setRestBaseURL("https://api.twitter.com/1.1/");
            cb.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
            cb.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
        } catch (Exception e) {
            Log.i("TWITTER", "Config not built");
        }


        AccessToken accessToken = new AccessToken(TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
        accToken = accessToken.toString();
        Twitter twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

        Paging paging;
        List<twitter4j.Status> statuses = null;

        try {
            paging = new Paging(5); // MAX 200 IN ONE CALL
            statuses = twitter.getHomeTimeline(paging);
        }catch (Exception e) {
            Log.i("TWITTER", "Paging failed");
        }

        try {
            for (twitter4j.Status status : statuses) {
                String rawJSON = TwitterObjectFactory.getRawJSON(status);
                //String fileName = "statuses/" + status.getId() + ".json";
                //Log.i("TWITTER", rawJSON.toString());

            }

        } catch (Exception e) {
            Log.i("TWITTER", "Statuses not found");
        }


        /*ConfigurationBuilder builder=new ConfigurationBuilder();
        builder.setUseSSL(true);
        builder.setApplicationOnlyAuthEnabled(true);

        // setup
        TwitterArrayList twitter = new TwitterFactory(builder.build()).getInstance();

        // exercise & verify
        twitter.setOAuthConsumer(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);

        try{
            OAuth2Token token = twitter.getOAuth2Token();
            twitter.getOAuth2Token();
        } catch (Exception e) {
            Log.i("TOKEN", "Could not get token");
        }*/


        /*try {
            ConfigurationBuilder cb = new ConfigurationBuilder();

                    cb.setDebugEnabled(true);
                    cb.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
                    cb.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
                    cb.setOAuthAccessToken(TWITTER_ACCESS_TOKEN);
                    cb.setOAuthAccessTokenSecret(TWITTER_ACCESS_SECRET);
                    cb.setJSONStoreEnabled(true);
                    //cb.setApplicationOnlyAuthEnabled(true);
                    //.setOAuthAccessToken(TwitterActivity.mAuthToken)
                    //.setOAuthAccessTokenSecret(TwitterActivity.mAuthToken);


            AccessToken accessToken = new AccessToken(TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            accToken = accessToken.toString();
            TwitterArrayList twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            // pulling tweets commented out for now
            *//*Paging paging = new Paging(5); // MAX 200 IN ONE CALL
            List<twitter4j.Status> statuses = twitter.getHomeTimeline(paging);

            try {

                int i = 0;
                for (twitter4j.Status status : statuses) {
                    String rawJSON = TwitterObjectFactory.getRawJSON(status);
                    //String fileName = "statuses/" + status.getId() + ".json";
                    //System.out.println(status.getUser().getName() + "\n" + status.getText());
                    TwitterFragment.mUser[i] = status.getUser().getName();
                    TwitterFragment.mStatus[i] = status.getText();
                    TwitterFragment.mUserAt[i] = status.getUser().getScreenName();
                    //System.out.println(Constants.mUser[i] + " @" + Constants.mUserAt[i] + "\n" + Constants.mStatus[i]);
                    TwitterFragment.mUrl[i] = Uri.parse(status.getUser().getProfileImageURLHttps());
                    i++;
                }


            } catch (Exception e) {
                Log.i("TWITTER JSON Parse ", "Didnt work");
            }*//*
        }catch (Exception e) {
            Log.i("ERR ", "Something's not right");

        }*/

        return "SUCCESS";


    }

    @Override
    protected void onPostExecute(String message) {
        TwitterFragment.mTwitterUser = TwitterActivity.mScreenName;
        String APICall = TwitterFragment.mTwitterPreAPI + TwitterFragment.mTwitterUser + TwitterFragment.mTwitterPostAPI;
        pullTweets(APICall);
    }

    protected void pullTweets(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    Log.i("TWITTER", "ERROR WITH TWITTER DATA");

                } else {
                    Log.i("TWEETS ", json.toString());
                }
            }
        }.start();
    }

}
