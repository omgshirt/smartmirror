package org.main.smartmirror.smartmirror;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that handles the Account Credentials and Work address
 * TODO Convert to Fragment
 */
public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Preferences mPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        mPreference = Preferences.getInstance(this);
        Log.i("mWorkLat", "" + mPreference.getWorkLatitude());
        Log.i("mWorkLong", "" + mPreference.getWorkLongitude());
        findGoogleAccounts();
        if (!mPreference.getFirstTimeRun()) {
            if (mPreference.getWorkLatitude() != 0.0 || mPreference.getWorkLongitude() != 0.0) {
                startMain();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPreference.setUserAccountName(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Finds the google accounts that are tied to the device
     */
    private void findGoogleAccounts() {
        Spinner googleAccountsPicker = (Spinner) findViewById(R.id.google_account_picker);

        ArrayList<String> accountsList = new ArrayList<>();
        //Getting all registered Google Accounts on device
        try {
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            for (Account account : accounts) {
                accountsList.add(account.name);
            }
        } catch (Exception e) {
            Log.i("Exception", "Exception:" + e);
        }
        // add a none option for privacy reasons off for now...
        // accountsList.add("None");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        googleAccountsPicker.setAdapter(adapter);
        googleAccountsPicker.setOnItemSelectedListener(this);
    }

    /**
     * Handles the button press
     *
     * @param view current view
     */
    public void saveUserInputs(View view) {
        // EditText facebookUsername = (EditText) findViewById(R.id.facebook_username);
        // EditText facebookPassword = (EditText) findViewById(R.id.facebook_password);
        // EditText twitterUsername = (EditText) findViewById(R.id.twitter_username);
        // EditText twitterPassword = (EditText) findViewById(R.id.twitter_password);
        EditText workAddress = (EditText) findViewById(R.id.work_location);
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
     * Starts Main Activity
     */
    private void startMain() {
        mPreference.setFirstTimrRun(true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
