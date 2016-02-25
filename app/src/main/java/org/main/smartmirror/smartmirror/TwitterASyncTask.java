package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
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
    private AccessToken accessToken;
    Twitter twitter;
    List<twitter4j.Status> statuses;
    Paging paging;

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


            accessToken = new AccessToken(TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            accToken = accessToken.toString();
            twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            // pulling tweets commented out for now
            paging = new Paging(5); // MAX 200 IN ONE CALL
            statuses = twitter.getHomeTimeline(paging);

            try {

                int i = 0;
                for (twitter4j.Status status : statuses) {
                    //String rawJSON = TwitterObjectFactory.getRawJSON(status);
                    //String fileName = "statuses/" + status.getId() + ".json";

                    TwitterFragment.mUsers.add(i,status.getUser().getName());
                    TwitterFragment.mTweets.add(i,status.getText());
                    TwitterFragment.mUsersAt.add(i,status.getUser().getScreenName());
                    TwitterFragment.mUri.add(i, Uri.parse(status.getUser().getProfileImageURLHttps()));

                    i++;
                }


            } catch (Exception e) {
                Log.i("TWITTER Parse ", "Didnt work");
            }

        }catch (Exception e) {
            Log.i("ERR ", "Something's not right, max call limit reached");

        }

        return "SUCCESS";


    }

    @Override
    protected void onPostExecute(String message) {

        ArrayList<CustomObject> objects = new ArrayList<CustomObject>();
        try {
            for(int i = 0; i < statuses.size(); i++){
                CustomObject co = new CustomObject(TwitterFragment.mUsers.get(i),TwitterFragment.mTweets.get(i),TwitterFragment.mUri.get(i));
                objects.add(co);
            }
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.getContextForApplication(), objects);
            TwitterFragment.twitterFeed.setAdapter(customAdapter);
        } catch (Exception e) {}


    }

}
