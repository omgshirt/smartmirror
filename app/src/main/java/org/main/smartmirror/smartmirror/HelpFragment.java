package org.main.smartmirror.smartmirror;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that handles the help dialog which shows the commands the user can issue
 */
public class HelpFragment extends Fragment {

    private Runnable mRunnable;
    private Timer mTimer;
    private TextView txtCurrentHelpHeader;
    private TextView txtCurrentHelpContent;
    private TextView txtModeHeader;
    private TextView txtModeContent;
    private TextView txtNewsHelpHeader;
    private TextView txtNewsHelpContent;
    private TextView txtHelpHeader;
    private TextView txtHelpContent;
    private TimerTask mTimerTask;

    private final int fadeInTime = 2000;
    private final int fadeOutTime = 2000;
    private final int displayLength = 20000;
    private final int offset = fadeInTime + displayLength + fadeInTime;


    public static HelpFragment newInstance(String fragName) {
        Bundle args = new Bundle();
        args.putString("name", fragName);
        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimer = new Timer();

        // Set-up the fade in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInTime);

        // Set-up the fade out
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setStartOffset(fadeInTime + displayLength);
        fadeOut.setDuration(fadeOutTime);

        // Create our animation
        final AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                txtCurrentHelpContent.setAnimation(animation);
                txtCurrentHelpHeader.setAnimation(animation);
                txtHelpContent.setAnimation(animation);
                txtHelpHeader.setAnimation(animation);
                txtModeContent.setAnimation(animation);
                txtModeHeader.setAnimation(animation);
                txtNewsHelpContent.setAnimation(animation);
                txtNewsHelpHeader.setAnimation(animation);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_fragment, container, false);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        
        mTimer.scheduleAtFixedRate(mTimerTask, 0, displayLength);

        String name = getArguments().getString("name");
        Log.i(Constants.TAG, name);
        Resources res = getResources();

        txtCurrentHelpHeader = (TextView) view.findViewById(R.id.fragment_help_header);
        txtCurrentHelpContent = (TextView) view.findViewById(R.id.fragment_help_content);
        txtModeHeader = (TextView) view.findViewById(R.id.mode_header);
        txtModeContent = (TextView) view.findViewById(R.id.mode_content);
        txtNewsHelpHeader = (TextView) view.findViewById(R.id.news_header);
        txtNewsHelpContent = (TextView) view.findViewById(R.id.news_content);
        txtHelpHeader = (TextView) view.findViewById(R.id.general_help_header);
        txtHelpContent = (TextView) view.findViewById(R.id.general_help_content);

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
            // capitalize the first letters in the arrays and append a new line.
            string[i] = string[i].substring(0, 1).toUpperCase() + string[i].substring(1);
            str += string[i] + "\n";
        }
        return str;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
