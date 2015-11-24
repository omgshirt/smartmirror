package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.*;

import io.fabric.sdk.android.Fabric;


public class TwitterAct extends Activity{

    private TwitterLoginButton mTwitterLoginButton;
    private TextView mStatus;
    private TwitterSession mSession;
    private long mUserID;
    private String mScreenName;
    private static String mAuthToken;
    private static String mAuthSecret;


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
                long tweetID = 631879971628183552L;
                //showTweet(tweetID);
                mAuthToken = result.data.getAuthToken().token;
                mAuthSecret = result.data.getAuthToken().secret;
                Log.i("auth token ", mAuthToken);
                Log.i("auth token ", mAuthSecret);
                Constants.TWITTER_ACCESS_TOKEN = mAuthToken;
                Constants.TWITTER_ACCESS_SECRET = mAuthSecret;
                finish();
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

}
