package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity that handles the Account Credentials and Work address
 */
public class AccountActivity extends AppCompatActivity {

    private Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        mPreferences = Preferences.getInstance(this);
    }

    public void startMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        EditText workAddress = (EditText) findViewById(R.id.work_location);
        String strAddress = workAddress.getText().toString().replace(' ', '+');
        Log.i("Address", strAddress);
        mPreferences.setWorkAddress(strAddress);
        startActivity(intent);
    }
}
