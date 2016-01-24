package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {


    @Override
    protected String doInBackground(String[] params) {


        try {
            ConfigurationBuilder cb = new ConfigurationBuilder();

                    cb.setDebugEnabled(true);
                    cb.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
                    cb.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
                    cb.setOAuthAccessToken(Constants.TWITTER_ACCESS_TOKEN);
                    cb.setOAuthAccessTokenSecret(Constants.TWITTER_ACCESS_SECRET);
                    cb.setJSONStoreEnabled(true);
                    //.setOAuthAccessToken(TwitterActivity.mAuthToken)
                    //.setOAuthAccessTokenSecret(TwitterActivity.mAuthToken);


            AccessToken accessToken = new AccessToken(Constants.TWITTER_ACCESS_TOKEN, Constants.TWITTER_ACCESS_SECRET);
            Twitter twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            Paging paging = new Paging(5); // MAX 200 IN ONE CALL
            List<twitter4j.Status> statuses = twitter.getHomeTimeline(paging);

            try {

                int i = 0;
                for (twitter4j.Status status : statuses) {
                    String rawJSON = TwitterObjectFactory.getRawJSON(status);
                    //String fileName = "statuses/" + status.getId() + ".json";
                    //System.out.println(status.getUser().getName() + "\n" + status.getText());
                    Constants.mUser[i] = status.getUser().getName();
                    Constants.mStatus[i] = status.getText();
                    Constants.mUserAt[i] = status.getUser().getScreenName();
                    //System.out.println(Constants.mUser[i] + " @" + Constants.mUserAt[i] + "\n" + Constants.mStatus[i]);
                    //Constants.mUrl[i] = Uri.parse(status.getUser().getProfileImageURL());
                    Constants.mUrl[i] = Uri.parse(status.getUser().getProfileImageURLHttps());

                    i++;
                }


            } catch (Exception e) {
                Log.i("TWITTER JSON Parse ", "Didnt work");
            }
        }catch (Exception e) {
            Log.i("ERR ", "Something's not right");

        }

        return "SUCCESS";


    }

    @Override
    protected void onPostExecute(String message) {

    }
}
