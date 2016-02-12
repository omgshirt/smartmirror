package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 1/29/2016.
 */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

//AccountPickerActivity creates DialogFragment through AccountPickerDialogFragment using AccountItem objects

public class AccountPickerActivity extends FragmentActivity {

    public static ArrayList<AccountItem> list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_picker_dialog);
        //Dialog for choosing account
        AccountPickerDialogFragment my_dialog = new AccountPickerDialogFragment();
        my_dialog.show(getFragmentManager(), "my_dialog");
    }
}
