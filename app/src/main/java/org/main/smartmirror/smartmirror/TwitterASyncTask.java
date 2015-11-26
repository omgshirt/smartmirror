package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

//android.os.AsyncTask<Params, Progress, Result>
public class TwitterASyncTask extends AsyncTask<String, Void, String> {
    //List<Status> statuses = new ArrayList<Status>();
    //ResponseList<Status> statuses;
    String statusJson = "";
    JSONObject JSON_complete = null;
    JSONObject JSON_user = null;
    String status = null;
    static String s = "";


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
            List<twitter4j.Status> statuses = twitter.getHomeTimeline(paging);

            //Paging paging = new Paging(1, 40);
            //List<twitter4j.Status> statusList = twitter.getUserTimeline("nexiidroid", paging);


            StringBuilder sb = new StringBuilder();


            try {
                //Status To JSON String
                statusJson = TwitterObjectFactory.getRawJSON(statuses);
                //System.out.println("StatusJson " + statusJson);

                //JSON String to JSONObject
                JSON_complete = new JSONObject(statusJson);
                System.out.println("JSON complete " + JSON_complete.toString());
                Log.i("JSON Complete ", JSON_complete.toString());

                JSONArray tweetArray = JSON_complete.getJSONArray("results");
                JSONObject tweetObject = tweetArray.getJSONObject(1);


/*                System.out.println("Array " + tweetArray);
                System.out.println("Object " + tweetObject);
                sb.append(tweetObject.getString("from_user")+": ");
                sb.append(tweetObject.get("text")+"\n\n");

                System.out.println(sb);
                Log.i("TWEET? ", sb.toString());*/

                /*//We get another JSONObject
                JSON_user = JSON_complete.getJSONObject("user");
                System.out.println("JSON user " + JSON_user.toString());
                Log.i("USER ", JSON_user.toString());

                //We get a field in the second JSONObject
                status = JSON_user.getString("status");

                System.out.println(JSON_user + " " + status.toString());
                Log.i("TWITTER JSON Parse ", JSON_user + " " + status);*/

                /*JSONArray JATweets = new JSONArray(statusJson);
                JSONObject JOTweets = JATweets.getJSONObject(1);
                //System.out.println(JOTweets);
                source = JOTweets.getJSONArray("source");
                System.out.println(source);*/


/*                for (int i = 0; i < JATweets.length(); i++) {
                    JSONObject JOTweets = JATweets.getJSONObject(i);
                    System.out.println(JOTweets);
                    //Log.i("TWEETS", JOTweets.toString());
                }*/


            } catch (Exception e) {
                Log.i("TWITTER JSON Parse ", "Didnt work");
            }
        }catch (Exception e) {
            Log.i("Something's not right", "wtf");

        }


        System.out.println("SUCCESS " + JSON_user + " " + status);
        Log.i("SUCCESS ", JSON_user + " " + status);
        return "SUCCESS";


    }

    @Override
    protected void onPostExecute(String message) {
        Log.i("SUCCESS", " I think");

    }
}
