package org.main.smartmirror.smartmirror;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Class that handles the help dialog which shows the commands the user can issue
 */
public class HelpFragment extends Fragment {

    public static HelpFragment newInstance(String fragName) {
        Bundle args = new Bundle();
        args.putString("name", fragName);
        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.help_fragment, container, false);
        String name = getArguments().getString("name");
        Log.i(Constants.TAG, name);
        Resources res = getResources();

        TextView txtCurrentHelpHeader = (TextView) view.findViewById(R.id.fragment_help_header);
        TextView txtCurrentHelpContent = (TextView) view.findViewById(R.id.fragment_help_content);
        TextView txtModeHeader = (TextView) view.findViewById(R.id.mode_header);
        TextView txtModeContent = (TextView) view.findViewById(R.id.mode_content);
        TextView txtNewsHelpHeader = (TextView) view.findViewById(R.id.news_header);
        TextView txtNewsHelpContent = (TextView) view.findViewById(R.id.news_content);
        TextView txtHelpHeader = (TextView) view.findViewById(R.id.general_help_header);
        TextView txtHelpContent = (TextView) view.findViewById(R.id.general_help_content);

        // set the default help!
        String strContent;
        String[] arrayContent;
        // Modes
        arrayContent = res.getStringArray(R.array.modes_list);
        strContent = buildupStringFromArrays(arrayContent);
        txtModeHeader.setText("Mode");
        txtModeContent.setText(strContent);
        // News
        arrayContent = res.getStringArray(R.array.guardian_sections);
        strContent = buildupStringFromArrays(arrayContent);
        txtNewsHelpHeader.setText("News");
        txtNewsHelpContent.setText(strContent);
        // General
        arrayContent = res.getStringArray(R.array.general_help);
        strContent = buildupStringFromArrays(arrayContent);
        txtHelpHeader.setText("General Help");
        txtHelpContent.setText(strContent);

        switch (name) {
            // facebook
            case Constants.FACEBOOK:
                arrayContent = res.getStringArray(R.array.facebook_help);
                strContent = buildupStringFromArrays(arrayContent);
                txtCurrentHelpHeader.setText(name.substring(0, 1).toUpperCase() + name.substring(1) + " - Help");
                txtCurrentHelpContent.setText(strContent);
                break;
            // camera
            case Constants.CAMERA:
                arrayContent = res.getStringArray(R.array.camera_help);
                strContent = buildupStringFromArrays(arrayContent);
                txtCurrentHelpHeader.setText("Controls");
                txtCurrentHelpContent.setText(strContent);
                break;
            // night light
            case Constants.NIGHT_LIGHT:
                arrayContent = res.getStringArray(R.array.color_names);
                strContent = buildupStringFromArrays(arrayContent);
                txtCurrentHelpHeader.setText("Color Options:");
                txtCurrentHelpContent.setText(strContent);
                break;
            default:
                txtCurrentHelpHeader.setVisibility(View.GONE);
                txtCurrentHelpContent.setVisibility(View.GONE);
                break;
        }
        return view;
    }

    /**
     * Builds up a single string from an array to be used in a TextView
     *
     * @param string the array to be converted
     * @return the converted string.
     */
    public String buildupStringFromArrays(String[] string) {
        String str = "";
        for (int i = 0; i < string.length; i++) {
            // capitalize the first letter in the arrays
            string[i] = string[i].substring(0, 1).toUpperCase() + string[i].substring(1);
            str += string[i] + "\n";
        }
        return str;
    }
}
