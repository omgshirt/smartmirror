package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 1/29/2016.
 */

import java.util.ArrayList;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class GetAccountsOnDevice extends FragmentActivity {
    public static ArrayList<AccountItem> list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_picker_dialog);
    }

    public void selectAccount(View v){
        SingleChoiceAccountPicker my_dialog = new SingleChoiceAccountPicker();
        my_dialog.show(getFragmentManager(), "my_dialog");

    }
}
