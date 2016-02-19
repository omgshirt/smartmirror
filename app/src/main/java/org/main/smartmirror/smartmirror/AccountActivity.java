package org.main.smartmirror.smartmirror;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Activity that handles the Account Credentials and Work address
 */
public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        mPreferences = Preferences.getInstance(this);
        findGoogleAccounts();
        if (mPreferences.getWorkAddress() == null && mPreferences.getWorkAddress().isEmpty()) {
            startMain();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPreferences.setUserAccountName(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Gets the google accounts that are tied to the device
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
        accountsList.add("None");
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
//        EditText facebookUsername = (EditText) findViewById(R.id.facebook_username);
//        EditText facebookPassword = (EditText) findViewById(R.id.facebook_password);
//        EditText twitterUsername = (EditText) findViewById(R.id.twitter_username);
//        EditText twitterPassword = (EditText) findViewById(R.id.twitter_password);
        EditText workAddress = (EditText) findViewById(R.id.work_location);
        String strAddress = workAddress.getText().toString().replace(' ', '+');
        mPreferences.setWorkAddress(strAddress);
        startMain();
    }

    /**
     * Starts Main Activity
     */
    public void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
