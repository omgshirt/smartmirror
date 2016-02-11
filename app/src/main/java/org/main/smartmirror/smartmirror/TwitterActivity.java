package org.main.smartmirror.smartmirror;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;



 /*Steps for OAuth
  * https://dev.twitter.com/oauth/application-only
  * */
public class TwitterActivity extends ListActivity{


    private TwitterLoginButton mTwitterLoginButton;
    private TextView mStatus;
    private TwitterSession mSession;
    private long mUserID;
    public static String mScreenName;
    public static String mAuthToken;
    public static String mAuthSecret;
    private ListView userTL;
     Handler mHandler;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new com.twitter.sdk.android.Twitter(authConfig));

        setContentView(R.layout.twitter_login_fragment);
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mStatus = (TextView)findViewById(R.id.status);
        mStatus.setText("Status: Ready");

        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
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
                mAuthToken = result.data.getAuthToken().token;
                mAuthSecret = result.data.getAuthToken().secret;

                // wont be needed if streaming api works
                //TwitterASyncTask.TWITTER_ACCESS_TOKEN = mAuthToken;
                //TwitterASyncTask.TWITTER_ACCESS_SECRET = mAuthSecret;

                //finish();
                downloadTweets();

                // this displays user timeline in twitter app like feed, need home_timeline
                /*UserTimeline userTimeline = new UserTimeline.Builder().screenName(mScreenName).build();
                Log.i("TWITTER USER TL ",userTimeline.toString() );

                final UserTimeline userTL = new UserTimeline.Builder()
                        .screenName(mScreenName)
                        .build();
                final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(TwitterActivity.this)
                        .setTimeline(userTL)
                        .build();
                setListAdapter(adapter);*/
            }

            @Override
            public void failure(TwitterException exception) {
                mStatus.setText("Status: Login Failed");
                Log.d("TwitterKit", "Login with TwitterArrayList failure", exception);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }



     // download twitter timeline after first checking to see if there is a network connection
     public void downloadTweets() {
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

         if (networkInfo != null && networkInfo.isConnected()) {
             new DownloadTwitterTask().execute(mScreenName);
             //new DownloadTwitterTask().execute();
         } else {
             Log.v("TWITTER", "No network connection available.");
         }
     }

     // Uses an AsyncTask to download a TwitterArrayList user's timeline
     private class DownloadTwitterTask extends AsyncTask<String, Void, String> {
         final static String CONSUMER_KEY = Constants.TWITTER_CONSUMER_KEY;
         final static String CONSUMER_SECRET = Constants.TWITTER_CONSUMER_SECRET;
         final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
         //final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name="; // this works
         final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/home_timeline.json?count=1"; // this gives error code 403 Forbidden,
                                                                                                                    //    possibly pull limit reached?
                                                                                                                    //    emailing twitter support

         @Override
         protected String doInBackground(String... screenNames) {
             String result = null;

             if (screenNames.length > 0) {
                 result = getTwitterStream(mScreenName);
                 //result = getTwitterStream();
             }
             return result;
         }


         // convert a JSON authentication object into an TwitterAuthenticated object
         private TwitterAuthenticated jsonToAuthenticated(String rawAuthorization) {
             TwitterAuthenticated auth = null;
             if (rawAuthorization != null && rawAuthorization.length() > 0) {
                 try {
                     Gson gson = new Gson();
                     auth = gson.fromJson(rawAuthorization, TwitterAuthenticated.class);
                 } catch (IllegalStateException ex) {
                     // just eat the exception
                 }
             }
             return auth;
         }

         private String getResponseBody(HttpRequestBase request) {
             StringBuilder sb = new StringBuilder();
             try {

                 DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                 HttpResponse response = httpClient.execute(request);
                 int statusCode = response.getStatusLine().getStatusCode();
                 String reason = response.getStatusLine().getReasonPhrase();

                 if (statusCode == 200) {

                     HttpEntity entity = response.getEntity();
                     InputStream inputStream = entity.getContent();

                     BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                     String line = null;
                     while ((line = bReader.readLine()) != null) {
                         sb.append(line);
                     }
                 } else {
                     sb.append(statusCode + " " + reason);
                 }
             } catch (UnsupportedEncodingException ex) {
             } catch (ClientProtocolException ex1) {
             } catch (IOException ex2) {
             }
             return sb.toString();
         }

         private String getTwitterStream(String screenName) {
         //private String getTwitterStream() {
             String results = null;

             // Step 1: Encode consumer key and secret
             try {
                 // URL encode the consumer key and secret
                 String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
                 String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

                 // Concatenate the encoded consumer key, a colon character, and the
                 // encoded consumer secret
                 String combined = urlApiKey + ":" + urlApiSecret;

                 // Base64 encode the string
                 String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

                 // Step 2: Obtain a bearer token
                 HttpPost httpPost = new HttpPost(TwitterTokenURL);
                 httpPost.setHeader("Authorization", "Basic " + base64Encoded);
                 httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                 httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
                 String rawAuthorization = getResponseBody(httpPost);
                 TwitterAuthenticated auth = jsonToAuthenticated(rawAuthorization);

                 // Applications should verify that the value associated with the
                 // token_type key of the returned object is bearer
                 if (auth != null && auth.token_type.equals("bearer")) {

                     // Step 3: Authenticate API requests with bearer token
                     //HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);
                     HttpGet httpGet = new HttpGet(TwitterStreamURL);

                     // construct a normal HTTPS request and include an Authorization
                     // header with the value of Bearer <>
                     httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                     httpGet.setHeader("Content-Type", "application/json");

                     // update the results with the body of the response
                     results = getResponseBody(httpGet);
                     Log.i("TWITTER result", results.toString());
                 }
             } catch (UnsupportedEncodingException ex) {
             } catch (IllegalStateException ex1) {
             }

             return results;
         }
     }


}
