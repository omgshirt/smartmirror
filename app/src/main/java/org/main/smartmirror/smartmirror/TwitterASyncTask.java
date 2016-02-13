package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;
import java.util.logging.Handler;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;


//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {

    // fallback tokens, replaced once user signs in
    public static String TWITTER_ACCESS_TOKEN = "4202759960-FRC4u2oIMHECYgzsQJAtWG8TcHAsMWfF6cNigXG";
    public static String TWITTER_ACCESS_SECRET = "BbK7Ls2rwXrutUOnKsE5pZx8EajxRgUiMZO6P39edBZFZ";
    public static String accToken;


    @Override
    protected String doInBackground(String[] params) {

        try {
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
            Twitter twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            // pulling tweets commented out for now
            Paging paging = new Paging(5); // MAX 200 IN ONE CALL
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
                    System.out.println(TwitterFragment.mUser[i] + " @" + TwitterFragment.mUserAt[i] + "\n" + TwitterFragment.mStatus[i]);
                    TwitterFragment.mUrl[i] = Uri.parse(status.getUser().getProfileImageURLHttps());
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
