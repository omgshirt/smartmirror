package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import io.fabric.sdk.android.Fabric;

/*API Console
   * https://dev.twitter.com/zh-hant/rest/tools/console
* */

/* TWITTER Login details
   * username: smartmirrortesting@gmail.com
   * password: smartmirrort
* */

public class TwitterActivity extends Activity{


    private TwitterLoginButton mTwitterLoginButton;
    private TextView mStatus;
    private TwitterSession mSession;
    private long mUserID;
    public static String mScreenName;
    public static String mAuthToken;
    public static String mAuthSecret;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig));

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
                finish();
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

}
