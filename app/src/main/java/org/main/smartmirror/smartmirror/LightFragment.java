package org.main.smartmirror.smartmirror;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class LightFragment extends Fragment {

    private final String LIGHT_PREFS = "light preferences";
    private final String LIGHT_COLOR = "light color";
    private int mColor;
    private HashMap<String, Integer> colorMap;

    // Handle any messages sent from MainActivity
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            handleCommand(message);
        }
    };

    public LightFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] colorNames = getResources().getStringArray(R.array.color_names);
        colorMap = new HashMap<>();
        for (String color : colorNames) {
            int colorResId = getResources().getIdentifier(color, "color", getActivity().getPackageName());
            colorMap.put(color, colorResId);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.light_fragment, container, false);
        int brightness = Preferences.getInstance(getActivity()).getLightBrightness();

        // get the stored color, White if no value is set
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(LIGHT_PREFS, Context.MODE_PRIVATE);
        mColor = prefs.getInt(LIGHT_COLOR, getResources().getColor(R.color.white));
        view.setBackgroundColor(mColor);
        //setWindowBrightness(brightness);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // return brightness to the mAppBrightness level
        Preferences prefs = Preferences.getInstance(getActivity());
        prefs.resetScreenBrightness();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private void setWindowBrightness(int brightness) {
        ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
        sbh.setScreenBrightness(getActivity(), brightness);
    }

    @SuppressWarnings("deprecation")
    private void handleCommand(String command) {
        if (colorMap.containsKey(command)) {
            int colorId = colorMap.get(command);
            View view = getView();
            try {
                mColor = getResources().getColor(colorId);
                view.setBackgroundColor(mColor);
                saveColor(mColor);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    }

    private void saveColor(int color) {
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(LIGHT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(LIGHT_COLOR, color);
        edit.apply();
    }
}
