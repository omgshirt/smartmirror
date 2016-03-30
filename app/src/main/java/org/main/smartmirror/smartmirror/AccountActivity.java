package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    private SignInButton sbtnGoogleSignInButton;
    private Preferences mPreference;
    private TwitterLoginButton mTwitterLoginButton;
    private TwitterSession mSession;

    private String mScreenName;
    private String mAuthToken;
    private String mAuthSecret;

    private TextView txtGoogleAccountName;
    private TextView txtFacebookAccountName;
    private TextView txtTwitterAccountName;
    private EditText edtWorkAddress;
    private ViewGroup btnSignOutButton;

    public static final int GOOGLE_REQUEST = 1;
    public static final int REQUEST_PERMISSIONS = 2;
    public static final int TWITTER_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApis();
        setContentView(R.layout.account_activity);
        mPreference = Preferences.getInstance(this);

        btnSignOutButton = (ViewGroup) findViewById(R.id.google_sign_out_button);
        edtWorkAddress = (EditText) findViewById(R.id.work_location);
        sbtnGoogleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        txtGoogleAccountName = (TextView) findViewById(R.id.google_account_name);
        txtFacebookAccountName = (TextView) findViewById(R.id.facebook_account_name);
        txtTwitterAccountName = (TextView) findViewById(R.id.twitter_account_name);

        txtGoogleAccountName.setText(mPreference.getGmailAccount());
        txtFacebookAccountName.setText(mPreference.getFacebookAccount());
        txtTwitterAccountName.setText(mPreference.getTwitterAccount());
        edtWorkAddress.setText(mPreference.getWorkLocation());

        setUpTwitterButton();
        setUpSignOutButton();
        setUpGoogleButton();
        if (!mPreference.getGmailAccount().isEmpty()) {
            hideSignInShowSignOutButton();
        } else {
            hideSignOutShowSignInButton();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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
                mPreference.setTwitterAccount(mScreenName);
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
     * Sets up the Google Button with its listener
     */
    private void setUpGoogleButton() {
        sbtnGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInToGoogle();
            }
        });
    }

    /**
     * Sets up the Sign Out button with its listener
     */
    private void setUpSignOutButton() {
        btnSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutOfGoogle();
            }
        });
    }

    /**
     * Sign in to Google
     */
    private void signInToGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_REQUEST);
    }

    /**
     * Handles the signing out of Google
     */
    private void signOutOfGoogle() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(Constants.TAG, "Revoked: " + status);
                        resetGoogleAccountValues();
                        hideSignOutShowSignInButton();
                    }
                });
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(Constants.TAG, "Signed out: " + status);
                    }
                });
    }

    // --------------------------------Helpers------------------------------------------------- //

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
     * Handles the Submit button press in account_activity
     * layout
     *
     * @param view current view
     */
    public void submitButtonPress(View view) {
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
        if (!(edtWorkAddress.getText().toString().isEmpty())) {
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
            if (acct != null) {
                mPreference.setGmailAccount(acct.getEmail());
                mPreference.setUserId(acct.getId());
                txtGoogleAccountName.setText(acct.getEmail());
                hideSignInShowSignOutButton();
                String[] names = acct.getDisplayName().split("\\s");
                if (names.length > 0) {
                    mPreference.setUserFirstName(names[0]);
                    mPreference.setUserLastName(names[names.length - 1]);
                }
            } else {
                Toast.makeText(this, "No account found!!", Toast.LENGTH_LONG).show();
            }
        } else {
            // not logged in!
        }
    }

    private void resetGoogleAccountValues() {
        mPreference.setGmailAccount("");
        mPreference.setUserId("");
        txtGoogleAccountName.setText(getResources().getString(R.string.no_google_account));
    }

    private void hideSignInShowSignOutButton() {
        sbtnGoogleSignInButton.setVisibility(View.GONE);
        btnSignOutButton.setVisibility(View.VISIBLE);
    }

    private void hideSignOutShowSignInButton() {
        sbtnGoogleSignInButton.setVisibility(View.VISIBLE);
        btnSignOutButton.setVisibility(View.GONE);
    }

    // -------------------------------Callbacks------------------------------------------------- //

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Log.i(Constants.TAG, connectionResult.toString());
        }
    }
}
