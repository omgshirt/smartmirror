package org.main.smartmirror.smartmirror;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Class that handles the help dialog which shows the commands the user can issue
 */
public class HelpFragment extends DialogFragment {
    private AlertDialog.Builder mAlertDialogBuilder;

    //used as the constructor to get the new instance of Helper dialogue
    public static HelpFragment newInstance(String name) {
        HelpFragment frgHelpFrag = new HelpFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        frgHelpFrag.setArguments(args);
        return frgHelpFrag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        String name = getArguments().getString("name");
        Resources res = getResources();

        mAlertDialogBuilder = new AlertDialog.Builder(getActivity(),R.style.MyDialog);
        String title = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase() + " - Help";
        View vewHelp = inflater.inflate(R.layout.help_fragment,null);
        mAlertDialogBuilder.setTitle(title);
        mAlertDialogBuilder.setView((vewHelp));
        TextView txtCol1 = (TextView)vewHelp.findViewById(R.id.General_Help_Content);
        TextView txtCol2 = (TextView)vewHelp.findViewById(R.id.General_Help_content2);

        String strColumn1;
        String strColumn2;

        switch (name) {

            case "news":
                String[] arrayColumn1 = res.getStringArray(R.array.news_settings);
                String[] arrayColumn2 = res.getStringArray(R.array.news_settings2);
                strColumn1 = stringSpace(arrayColumn1);
                strColumn2 = stringSpace(arrayColumn2);
                txtCol1.setText(strColumn1);
                txtCol2.setText(strColumn2);
                break;

            // settings
            case "settings":
                arrayColumn1 = res.getStringArray(R.array.help_settings);
                arrayColumn2 = res.getStringArray(R.array.help_settings2);
                strColumn1 = stringSpace(arrayColumn1);
                strColumn2 = stringSpace(arrayColumn2);
                txtCol1.setText(strColumn1);
                txtCol2.setText(strColumn2);
                break;

            // default help
            default:
                arrayColumn1 = res.getStringArray(R.array.General_help_array);
                arrayColumn2 = res.getStringArray(R.array.General_help_array2);
                strColumn1 = stringSpace(arrayColumn1);
                strColumn2 = stringSpace(arrayColumn2);
                txtCol1.setText(strColumn1);
                txtCol2.setText(strColumn2);
                break;
        }
        return mAlertDialogBuilder.create();
    }

    public String stringSpace(String[] string){
        String str = "";
        for(int i = 0; i<string.length; i++){
            str += string[i]+"\n";
        }
        return str;
    }
}
