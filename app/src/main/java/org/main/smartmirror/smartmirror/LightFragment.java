package org.main.smartmirror.smartmirror;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        int brightness = Preferences.getInstance(getActivity()).getLightBrightness();
        setWindowBrightness(brightness);
        return view;
    }

    public void onPause() {
        super.onPause();
        // return brightness to the mAppBrightness level
        Preferences prefs = Preferences.getInstance(getActivity());
        prefs.resetScreenBrightness();
    }

    private void setWindowBrightness(int brightness) {
        ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
        sbh.setScreenBrightness(getActivity(), brightness);
    }
}
