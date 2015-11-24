package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {
    //List<Status> statuses = new ArrayList<Status>();
    //ResponseList<Status> statuses;


    @Override
    protected String doInBackground(String[] params) {

        try {
            ConfigurationBuilder cb = new ConfigurationBuilder();

            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY)
                    .setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET)
                    .setOAuthAccessToken(Constants.TWITTER_ACCESS_TOKEN)
                    .setOAuthAccessTokenSecret(Constants.TWITTER_ACCESS_SECRET);
                    cb.setJSONStoreEnabled(true);
                    //.setOAuthAccessToken(TwitterAct.mAuthToken)
                    //.setOAuthAccessTokenSecret(TwitterAct.mAuthToken);


            AccessToken accessToken = new AccessToken(Constants.TWITTER_ACCESS_TOKEN, Constants.TWITTER_ACCESS_SECRET);
            Twitter twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            Paging paging = new Paging(1); // MAX 200 IN ONE CALL
            List statuses = twitter.getHomeTimeline(paging);

            try {
                String strInitialDataSet = DataObjectFactory.getRawJSON(statuses);
                JSONArray JATweets = new JSONArray(strInitialDataSet);

                for (int i = 0; i < JATweets.length(); i++) {
                    JSONObject JOTweets = JATweets.getJSONObject(i);
                    System.out.println(JOTweets);
                    //Log.i("TWEETS", JOTweets.toString());
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
        }catch (Exception e) {

        }

        return "SUCCESS";
    }

    @Override
    protected void onPostExecute(String message) {
        Log.i("SUCCESS", " I think");
    }
}
