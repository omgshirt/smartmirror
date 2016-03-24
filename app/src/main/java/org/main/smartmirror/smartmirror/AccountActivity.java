package org.main.smartmirror.smartmirror;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.api.services.gmail.GmailScopes;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.IOException;
import java.util.List;


import io.fabric.sdk.android.Fabric;

/**
 * Activity that handles the Account Credentials and Work address
 */
public class AccountActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private long mUserID;
    private GoogleApiClient mGoogleApiClient;
    private Preferences mPreference;
    private TwitterLoginButton mTwitterLoginButton;
    private TwitterSession mSession;

    private String mScreenName;
    private String mAuthToken;
    private String mAuthSecret;

    private TextView edtFacebookUsername;
    private TextView edtFacebookPassword;

    private TextView txtGoogleAccountName;
    private EditText edtWorkAddress;

    public static final int GOOGLE_REQUEST = 1;
    public static final int REQUEST_PERMISSIONS = 2;
    public static final int TWITTER_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApis();
        setContentView(R.layout.account_activity);
        mPreference = Preferences.getInstance(this);

        edtFacebookUsername = (EditText) findViewById(R.id.facebook_username);
        edtFacebookPassword  = (EditText) findViewById(R.id.facebook_password);

        if (mPreference.isLoggedInToFacebook()) {
            // TODO: get values from preferences and set edtFacebookUsername / edtFacebookPassword
        }

        txtGoogleAccountName = (TextView) findViewById(R.id.google_account_name);
        edtWorkAddress = (EditText) findViewById(R.id.work_location);

        if (mPreference.isLoggedInToGmail()) {
            txtGoogleAccountName.setText(mPreference.getGmailAccount());
        }
        edtWorkAddress.setText(mPreference.getWorkLocation());

        setUpTwitterButton();
        setUpGoogleButton();

    }

    /**
     * Initializes the APIs we want to use
     */
    private void initializeApis() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Constants.PICASA), new Scope(GmailScopes.GMAIL_LABELS),
                new Scope(GmailScopes.GMAIL_READONLY),
                new Scope(GmailScopes.MAIL_GOOGLE_COM),
                new Scope(GmailScopes.GMAIL_MODIFY),
                new Scope(GmailScopes.GMAIL_INSERT))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig));
    }


    /**
     * Sets up the functionality for the twitter button in the view.
     */
    private void setUpTwitterButton() {
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String output = "Status: " +
                        "Your login was successful " +
                        result.data.getUserName() +
                        "\nAuth Token Received: " +
                        result.data.getAuthToken().token;

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
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with TwitterArrayList failure", exception);
            }
        });
    }

    /**
     * Handles the Google Button necessities
     */
    private void setUpGoogleButton() {
        SignInButton googleSignInBtn = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInToGoogle();
            }
        });
    }

    /**
     * Handles the signing out of Google
     */
    private void signOutOfGoogle() {
        mGoogleApiClient.connect();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(Constants.TAG, "Revoked: " + status);
                        mPreference.setGmailAccount("");
                        txtGoogleAccountName.setText(getResources().getString(R.string.no_google_account));
                    }
                });
//        HttpsURLConnection connection = null;
//        URL url = null;
//        try {
//            url = new URL("https://accounts.google.com/o/oauth2/revoke?token=" + mPreference.getTokenId());
//            connection = (HttpsURLConnection) url.openConnection();
//            connection.connect();
//            if(connection.getResponseCode() == 200){
//                Log.i(Constants.TAG, "Successfully Logged Out");
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            connection.disconnect();
//        }
    }

    // --------------------------------Helpers------------------------------------------------- //

    /**
     * Shows the dialog that prompts the user for the number of permissions
     *
     * @param message    The message we want to display
     * @param okListener OnClickListener instance
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AccountActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * We listen here for the result from Twitter
     * and pass it on to TwitterLoginBUtton
     *
     * @param requestCode the request
     * @param resultCode  the result
     * @param data        data that we received
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if (requestCode == TWITTER_REQUEST) {
            mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Sign into Google
     */
    private void signInToGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_REQUEST);
    }

    /**
     * Handles the Submit button in account_activity
     * layout
     *
     * @param view current view
     */
    public void handleButtonPress(View view) {
        Log.i(Constants.TAG, "Submit Button");
        /*EditText edtFacebookUsername = (EditText) findViewById(R.id.facebook_username);
        EditText edtFacebookPassword = (EditText) findViewById(R.id.facebook_password);
        if (edtFacebookPassword.getText().toString().equals("") && edtFacebookUsername.getText().toString().equals("")) {
            AESHelper.encryptMsg(facebookUsername.getText().toString() + "::" + facebookPassword.getText().toString(), mPreference.getSecret());
            edFacebookPassword = null;
            edtFacebookUsername = null;
        }*/
        // since by default the work lat and long is set to -1 we are OK
        // to not have an else case here
        if ( !(edtWorkAddress.getText().toString().isEmpty()) ) {
            mPreference.setWorkLocation(edtWorkAddress.getText().toString());

            String strAddress = edtWorkAddress.getText().toString().replace(' ', '+');
            convertAddressToLatLong(strAddress);
        }
        mPreference.setFirstTimeRun(false);
        finish();
    }

    /**
     * Converts the given address to latitude and longitude
     *
     * @param addressInput the given address
     */
    private void convertAddressToLatLong(String addressInput) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(addressInput, 5);
            if (addressList != null) {
                // interested in only the first result
                mPreference.setWorkLatitude(addressList.get(0).getLatitude());
                mPreference.setWorkLongitude(addressList.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is where we handle the result from Google
     *
     * @param result the result from the sign in
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct != null) {
                mPreference.setGmailAccount(acct.getEmail());
                txtGoogleAccountName.setText(acct.getEmail());
                String[] names = acct.getDisplayName().split("\\s");
                if (names.length > 0) {
                    mPreference.setUserFirstName(names[0]);
                    mPreference.setUserLastName(names[names.length - 1]);
                }
            } else {
                Toast.makeText(this, "No account found!!", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(Constants.TAG, "Google sign in failed");
        }
    }

    // -------------------------------Callbacks------------------------------------------------- //

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(Constants.TAG, connectionResult.toString());
    }
}
