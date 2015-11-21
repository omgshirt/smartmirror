package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String[] params) {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(Constants.TWITTER_ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(Constants.TWITTER_ACCESS_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter4j.Twitter twitter = tf.getInstance();

        try {
            //twitter4j.Status status = twitter.updateStatus("test");
            try {
                String user = twitter.getScreenName();
                ResponseList statuses = twitter.getHomeTimeline();
                Log.i("user ", user);
                Log.i("timeline ", statuses.toString());
                //for (Status status : statuses) {
                    //Log.i("STATUS ","@" + status.getUser().getScreenName() + " - " + status.getText());
                //}
            }catch (Exception e) {

            }
            Log.i("STATUS ", "SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("STATUS ", "FAIL");
        }
        return "SUCCESS";
    }

    @Override
    protected void onPostExecute(String message) {
        Log.i("SUCCESS", " I think");
    }
}
