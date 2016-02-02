package org.main.smartmirror.smartmirror;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Master N on 1/30/2016.
 */
public class AccountPickerDialogFragment extends DialogFragment {

    public ArrayList<AccountItem> list2; //Used to store AccountItem objects
    public String[] nameList; //Used to store account name strings from above object arraylist

    public void getAccs(){
       list2 = getData();
        nameList = new String[list2.size()];
        for (int i = 0; i < list2.size(); i++){
            nameList[i] = list2.get(i).getValue();
        }
    }

    String selection;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getAccs();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose difficulty level...").setSingleChoiceItems(nameList, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Find which account was picked
                for(int i = 0; i < nameList.length; i++){
                    if(which == i){
                        selection = nameList[which];
                    }
                }
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "You have selected : " + selection, Toast.LENGTH_SHORT).show();
                //Store selected account in preferences
                Preferences preferences = Preferences.getInstance(getActivity());
                preferences.setUserAccountName(selection);
                //Close activity to go to main activity
                getActivity().finish();
            }
        });
        return builder.create();
    }

    private ArrayList<AccountItem> getData() {

        ArrayList<AccountItem> accountsList = new ArrayList<AccountItem>();
        //Getting all registered Google Accounts on device
        try {
            Account[] accounts = AccountManager.get(getActivity()).getAccountsByType("com.google");
            for (Account account : accounts) {
                AccountItem item = new AccountItem(account.name);
                accountsList.add(item);
            }
        } catch (Exception e) {
            Log.i("Exception", "Exception:" + e);
        }
        return accountsList;
    }
}