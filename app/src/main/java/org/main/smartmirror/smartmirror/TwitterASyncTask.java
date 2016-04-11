package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {

    // fallback tokens, replaced once user signs in
    public static String TWITTER_ACCESS_TOKEN = "4313842942-zRoKUcEECkUZoWLfEWnomOjzIaXaJJeIIRuT7Nh";
    public static String TWITTER_ACCESS_SECRET = "uwwdTUEs9gRQwnvDoeEEquQBkoA9KTdK3kdsgwKm1PHCY";
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


            accessToken = new AccessToken(TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            accToken = accessToken.toString();
            twitter = new TwitterFactory(cb.build()).getInstance(accessToken);

            paging = new Paging(5); // MAX 200 IN ONE CALL
            statuses = twitter.getHomeTimeline(paging);
            updateTwitterCache(statuses);

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
                Log.i("TWITTER Parse ", e.toString());
            }

        }catch (Exception e) {
            Log.i("ERR ", e.toString());

        }

        return "SUCCESS";


    }

    @Override
    protected void onPostExecute(String message) {

        ArrayList<CustomListViewObject> objects = new ArrayList<CustomListViewObject>();
        try {
            for(int i = 0; i < statuses.size(); i++){
                CustomListViewObject co = new CustomListViewObject(TwitterFragment.mUsers.get(i),TwitterFragment.mTweets.get(i),TwitterFragment.mUri.get(i), null);
                objects.add(co);
            }
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.getContextForApplication(), objects, false);
            TwitterFragment.twitterFeed.setAdapter(customAdapter);
        } catch (Exception e) {}


    }

    public void updateTwitterCache(List<twitter4j.Status> data) {
        // Update the TWITTER_CACHE stored in cacheManager or create new if it doesn't exist.
        TwitterFragment.mCacheManager.addCache(TwitterFragment.TWITTER_CACHE, data, TwitterFragment.DATA_UPDATE_FREQUENCY);
    }

}
