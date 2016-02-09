package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;



 /*Steps for OAuth
  * https://dev.twitter.com/oauth/application-only
  * */
public class TwitterActivity extends Activity{


    private TwitterLoginButton mTwitterLoginButton;
    private TextView mStatus;
    private TwitterSession mSession;
    private long mUserID;
    public static String mScreenName;
    public static String mAuthToken;
    public static String mAuthSecret;
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

                TwitterASyncTask.TWITTER_ACCESS_TOKEN = mAuthToken;
                TwitterASyncTask.TWITTER_ACCESS_SECRET = mAuthSecret;
                //new PostClass().execute();

                //finish();
                downloadTweets();
                /*try {
                    Thread.sleep(2000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                updateTwitter("https://api.twitter.com/1.1/statuses/user_timeline.json?count=5");*/
            }

            @Override
            public void failure(TwitterException exception) {
                mStatus.setText("Status: Login Failed");
                Log.d("TwitterKit", "Login with Twitter failure", exception);
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

     // Uses an AsyncTask to download a Twitter user's timeline
     private class DownloadTwitterTask extends AsyncTask<String, Void, String> {
         final static String CONSUMER_KEY = Constants.TWITTER_CONSUMER_KEY;
         final static String CONSUMER_SECRET = Constants.TWITTER_CONSUMER_SECRET;
         final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
         //final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";
         final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/home_timeline.json?count=1";

         @Override
         protected String doInBackground(String... screenNames) {
             String result = null;

             if (screenNames.length > 0) {
                 result = getTwitterStream(screenNames[0]);
                 //result = getTwitterStream();
             }
             return result;
         }

         // onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
         @Override
         protected void onPostExecute(String result) {
             try {
                 Twitter twits = jsonToTwitter(result);
                 Log.i("TWITTER OPE result", result.toString());
             } catch (Exception e) {
                 Log.i("TWITTER OPE result", "FAIL");
             }


             // lets write the results to the console as well
             /*for (Tweet tweet : twits) {
                 Log.i("TWITTER", tweet.getText());
             }*/

             // send the tweets to the adapter for rendering
             /*ArrayAdapter<Tweet> adapter = new ArrayAdapter<Tweet>(activity, android.R.layout.simple_list_item_1, twits);
             setListAdapter(adapter);*/
         }

         // converts a string of JSON data into a Twitter object
         private Twitter jsonToTwitter(String result) {
             Twitter twits = null;
             if (result != null && result.length() > 0) {
                 try {
                     //Gson gson = new Gson();
                     //twits = gson.fromJson(result, Twitter.class);
                     Log.i("TWITTER json to twitter", result.toString());
                 } catch (IllegalStateException ex) {
                     // just eat the exception
                 }
             }
             return twits;
         }

         // convert a JSON authentication object into an Authenticated object
         private Authenticated jsonToAuthenticated(String rawAuthorization) {
             Authenticated auth = null;
             if (rawAuthorization != null && rawAuthorization.length() > 0) {
                 try {
                     Gson gson = new Gson();
                     auth = gson.fromJson(rawAuthorization, Authenticated.class);
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
                     sb.append(reason);
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
                 Authenticated auth = jsonToAuthenticated(rawAuthorization);

                 // Applications should verify that the value associated with the
                 // token_type key of the returned object is bearer
                 if (auth != null && auth.token_type.equals("bearer")) {

                     // Step 3: Authenticate API requests with bearer token
                     //HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);
                     HttpGet httpGet = new HttpGet(TwitterStreamURL);
                     //Log.i("TWITTER URL USed", TwitterStreamURL);

                     // construct a normal HTTPS request and include an Authorization
                     // header with the value of Bearer <>
                     httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                     //Log.i("TWITTER auth token", auth.access_token.toString());
                     httpGet.setHeader("Content-Type", "application/json");
                     // update the results with the body of the response
                     results = getResponseBody(httpGet);
                     Log.i("TWITTER get result", results.toString());
                 }
             } catch (UnsupportedEncodingException ex) {
             } catch (IllegalStateException ex1) {
             }
             return results;
         }
     }

     // Get twitter feed from api
     private void updateFeed(final String query){
         new Thread(){
             public void run(){
                 final JSONObject json = FetchURL.getJSON(query);
                 if(json == null){
                     mHandler.post(new Runnable(){
                         public void run(){
                             Log.i("TWITTER", "TWITTER json error");
                         }
                     });
                 } else {
                     mHandler.post(new Runnable(){
                         public void run(){
                             Log.i("TWITTER ", json.toString());
                         }
                     });
                 }
             }
         }.start();
     }


    /*private class PostClass extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {

                URL url = new URL("https://api.twitter.com/oauth2/token");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "smartmirror");
                connection.setRequestProperty("grant_type", "client_credentials");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters ");
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                output.append(System.getProperty("line.separator") + "Response "
                        + System.getProperty("line.separator") + System.getProperty("line.separator")
                        + responseOutput.toString());

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Log.i("TWITTER", output.toString());

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

    }*/


}
