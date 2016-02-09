package org.main.smartmirror.smartmirror;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Class that handles the help dialog which shows the commands the user can issue
 */
public class HelpFragment extends DialogFragment {
    private AlertDialog.Builder mAlertDialogBuilder;
    private static final String DIALOG_TITLE="Help";

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
        mAlertDialogBuilder.setTitle((name.substring(0,1).toUpperCase() + name.substring(1)) + " - " + DIALOG_TITLE);
        mAlertDialogBuilder.setView(vewHelp);
        TextView txtCurrentHelpHeader = (TextView)vewHelp.findViewById(R.id.fragment_help_header);
        TextView txtCurrentHelpContent = (TextView)vewHelp.findViewById(R.id.fragment_help_content);
        TextView txtModeHeader = (TextView)vewHelp.findViewById(R.id.mode_header);
        TextView txtModeContent = (TextView)vewHelp.findViewById(R.id.mode_content);
        TextView txtNewsHelpHeader = (TextView)vewHelp.findViewById(R.id.news_header);
        TextView txtNewsHelpContent = (TextView)vewHelp.findViewById(R.id.news_content);
        TextView txtHelpHeader = (TextView)vewHelp.findViewById(R.id.general_help_header);
        TextView txtHelpContent = (TextView)vewHelp.findViewById(R.id.general_help_content);

        // set the default help!
        String strContent;
        String[] arrayContent;
        // Modes
        arrayContent = res.getStringArray(R.array.modes_list);
        strContent = stringSpace(arrayContent);
        txtModeHeader.setText("Mode");
        txtModeContent.setText(strContent);
        // News
        arrayContent = res.getStringArray(R.array.guardian_sections);
        strContent = stringSpace(arrayContent);
        txtNewsHelpHeader.setText("News");
        txtNewsHelpContent.setText(strContent);
        // General
        arrayContent = res.getStringArray(R.array.general_help);
        strContent = stringSpace(arrayContent);
        txtHelpHeader.setText("General Help");
        txtHelpContent.setText(strContent);

        switch (name) {
            // facebook
            case Constants.FACEBOOK:
                arrayContent = res.getStringArray(R.array.facebook_help);
                strContent = stringSpace(arrayContent);
                txtCurrentHelpHeader.setText("Controls");
                txtCurrentHelpContent.setText(strContent);
                break;
            // camera
            case Constants.CAMERA:
                arrayContent = res.getStringArray(R.array.camera_help);
                strContent = stringSpace(arrayContent);
                txtCurrentHelpHeader.setText("Controls");
                txtCurrentHelpContent.setText(strContent);
                break;
            // night light
            case Constants.LIGHT:
                arrayContent = res.getStringArray(R.array.color_names);
                strContent = stringSpace(arrayContent);
                txtCurrentHelpHeader.setText("Color Options:");
                txtCurrentHelpContent.setText(strContent);
                break;
            default:
                txtCurrentHelpHeader.setVisibility(View.GONE);
                txtCurrentHelpContent.setVisibility(View.GONE);
            break;
        }
        return mAlertDialogBuilder.create();
    }

    public String stringSpace(String[] string){
        String str = "";
        for(int i = 0; i < string.length; i++){
            // capitalize the first letter in the arrays
            string[i] = string[i].substring(0,1).toUpperCase() + string[i].substring(1);
            str += string[i] + "\n";
        }
        return str;
    }
}
