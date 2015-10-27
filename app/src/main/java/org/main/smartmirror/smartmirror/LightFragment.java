package org.main.smartmirror.smartmirror;


import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LightFragment extends Fragment {

    public LightFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.light_fragment, container, false);

        int brightness = Preferences.getInstance().getLightBrightness();
        try {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            setWindowBrightness(brightness);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "System brightness not found");
        }
        return view;
    }

    public void onPause() {
        super.onPause();
        // return brightness to the mAppBrightness level
        Preferences prefs = Preferences.getInstance();
        prefs.setAppBrightness(getActivity());
    }

    private void setWindowBrightness(int brightness) {
        ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
        sbh.setScreenBrightness(getActivity(), brightness);
    }
}
