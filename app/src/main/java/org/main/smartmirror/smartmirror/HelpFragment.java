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
    private static final String TITLE="Available Commands";

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
        View vewHelp = inflater.inflate(R.layout.help_fragment, null);
        String name = getArguments().getString("name");
        Resources res = getResources();

        mAlertDialogBuilder = new AlertDialog.Builder(getActivity(),R.style.MyDialog);
        mAlertDialogBuilder.setTitle(TITLE);
        mAlertDialogBuilder.setView(vewHelp);
        TextView txtHelpHeader = (TextView)vewHelp.findViewById(R.id.help_header);
        TextView txtHelpContent = (TextView)vewHelp.findViewById(R.id.help_content);

        String strContent;

        switch (name) {

            // night light
            case Constants.NIGHT_LIGHT:
                String[] arrayContent = res.getStringArray(R.array.color_names);
                strContent = stringSpace(arrayContent);
                txtHelpHeader.setText("Color Options:");
                txtHelpContent.setText(strContent);
                break;
            // news
            case Constants.NEWS:
                arrayContent = res.getStringArray(R.array.guardian_sections);
                strContent = stringSpace(arrayContent);
                txtHelpHeader.setText("Choose a news section:");
                txtHelpContent.setText(strContent);
                break;

            // settings
            case Constants.SETTINGS:
                arrayContent = res.getStringArray(R.array.help_settings2);
                strContent = stringSpace(arrayContent);
                txtHelpHeader.setText("General Settings:");
                txtHelpContent.setText(strContent);
                break;

            // default help
            default:
                arrayContent = res.getStringArray(R.array.general_help);
                strContent = stringSpace(arrayContent);
                txtHelpHeader.setText("Choose a View");
                txtHelpContent.setText(strContent);
                break;
        }
        return mAlertDialogBuilder.create();
    }

    public String stringSpace(String[] string){
        String str = "";
        for(int i = 0; i < string.length; i++){
            str += string[i] + "\n";
        }
        return str;
    }
}
