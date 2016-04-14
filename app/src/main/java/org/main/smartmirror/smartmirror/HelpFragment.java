package org.main.smartmirror.smartmirror;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that handles the help dialog which shows the commands the user can issue
 */
public class HelpFragment extends Fragment {

    private LinearLayout mHelpLayout;
    private Runnable mRunnable;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private final int fadeInTime = 2000;
    private final int fadeOutTime = 2000;
    private final int durationTime = 20000;
    private final int displayLength = fadeInTime + durationTime + fadeOutTime;

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
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                removeHelpFragment();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // Set the runnable
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // Start the animation
                mHelpLayout.setAnimation(animation);
                mHelpLayout.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_fragment, container, false);
        mHelpLayout = (LinearLayout) view.findViewById(R.id.help_layout);
        mHelpLayout.setVisibility(View.INVISIBLE);

        String name = getArguments().getString("name");
        Resources res = getResources();

        TextView txtCurrentHelpHeader = (TextView) view.findViewById(R.id.fragment_help_header);
        TextView txtCurrentHelpContent = (TextView) view.findViewById(R.id.fragment_help_content);
        View vwDivider = view.findViewById(R.id.help_divider);
        TextView txtModeHeader = (TextView) view.findViewById(R.id.mode_header);
        TextView txtModeContent = (TextView) view.findViewById(R.id.mode_content);
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
            case Constants.MUSIC:
                arrayContent = res.getStringArray(R.array.music_commands);
                strContent = buildupStringFromArrays(arrayContent);
                txtCurrentHelpHeader.setText(name.substring(0,1).toUpperCase() + name.substring(1) + " - Help");
                txtCurrentHelpContent.setText(strContent);
                break;
            // night light
            case Constants.NIGHT_LIGHT:
                txtCurrentHelpHeader.setText("Color Options:");
                txtCurrentHelpContent.setText(strContent);
                break;
            default:
                txtCurrentHelpHeader.setVisibility(View.GONE);
                vwDivider.setVisibility(View.GONE);
                txtCurrentHelpContent.setVisibility(View.GONE);
                break;
        }

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, displayLength);
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

    /**
     * The equivalent of Activity.finish()
     */
    private void removeHelpFragment() {
        mTimerTask.cancel();
        ((MainActivity) getActivity()).removeFragment(Constants.HELP);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerTask.cancel();
    }
}
