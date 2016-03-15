package org.main.smartmirror.smartmirror;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final int GOOGLE_REQUEST = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private static final int TWITTER_REQUEST = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApis();
        setContentView(R.layout.account_activity);
        mPreference = Preferences.getInstance(this);
        askForPermissions();
        setUpTwitterButton();
        setUpGoogleButton();
        if (mPreference.getFirstTimeRun()) {
            if (mPreference.getGmailLoginStatus()) {
                // sign out of google
                // signOutOfGoogle();
            }
            // generate the keys
            // createNewKeys();
        } else {
            // we don't care if the values are empty
            // each fragment should handle this
            startMain();
        }
    }

    /**
     * Initializes the APIs we want to use
     */
    private void initializeApis() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Constants.PICASA), new Scope(Scopes.PLUS_LOGIN))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig));
    }

    /**
     * Checks all the permissions!
     */
    private void askForPermissions() {
        getWriteSettingsPermission();
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        // Here, this is the current activity
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Access Coarse Location");
        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add("Camera");
        if (!addPermission(permissionsList, Manifest.permission.GET_ACCOUNTS))
            permissionsNeeded.add("Get Accounts");
        if (!addPermission(permissionsList, Manifest.permission.READ_CALENDAR))
            permissionsNeeded.add("Read Calendar");
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Record Audio");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(AccountActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_PERMISSIONS);
                            }
                        });
            }
        }
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
    private void signOutOffGoogle() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(Constants.TAG, "Revoked: " + status);
                    }
                });
    }

    /**
     * Starts Main Activity
     */
    private void startMain() {
        mPreference.setFirstTimeRun(false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // --------------------------------Helpers------------------------------------------------- //

    /**
     * Shows the dialog that prompts the user for the number of permissions
     *
     * @param message    The message we want to display
     * @param okListener
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
     * Receives a list of permissions and the permission
     * wanted. It then checks to see if the permission is not
     * granted and prompts the user to grant the permission
     *
     * @param permissionsList the permission list that we want
     * @param permission      the given permission we need to check against
     * @return the boolean value
     */
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
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
        }
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Gets the permission for WRITE_SETTINGS for Android M
     */
    private void getWriteSettingsPermission() {
        // check for permission to write system settings on API 23 and greater.
        // Leaving this in case we need the WRITE_SETTINGS permission later on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, REQUEST_PERMISSIONS);
            }
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
     * Handles the button press in the layout
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
        EditText workAddress = (EditText) findViewById(R.id.work_location);
        // since by default the work lat and long is set to -1 we are OK
        // to not have an else case here
        if (!(workAddress.getText().toString().equals(""))) {
            String strAddress = workAddress.getText().toString().replace(' ', '+');
            convertAddressToLatLong(strAddress);
        }
        startMain();
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
                mPreference.setGmailAccount(acct.getEmail().toString());
                // mPreference.setUserName(acct.getDisplayName());
            }
        } else {
            // not logged in!
        }
    }

    // -------------------------------Callbacks------------------------------------------------- //

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CALENDAR, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for permissions
                if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                } else {
                    // Permission Denied
                    Toast.makeText(AccountActivity.this, "Some Permission is Denied", Toast.LENGTH_LONG).show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(Constants.TAG, connectionResult.toString());
    }
}
