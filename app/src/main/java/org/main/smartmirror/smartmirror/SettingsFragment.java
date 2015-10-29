package org.main.smartmirror.smartmirror;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;


public class SettingsFragment extends Fragment {

    private Preferences mPreferences;
    private RadioGroup grpSpeechFreqGroup;
    private RadioGroup grpAppBrightness;
    private RadioGroup grpLightBrightness;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mPreferences = Preferences.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        // Speech Frequency radio group
        grpSpeechFreqGroup = (RadioGroup) view.findViewById(R.id.speech_frequency_group);
        grpSpeechFreqGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.speech_never:
                        mPreferences.setSpeechFrequency(Preferences.SPEECH_NEVER);
                        break;
                    case R.id.speech_rare:
                        mPreferences.setSpeechFrequency(Preferences.SPEECH_RARE);
                        break;
                    case R.id.speech_often:
                        mPreferences.setSpeechFrequency(Preferences.SPEECH_OFTEN);
                        break;
                    case R.id.speech_always:
                        mPreferences.setSpeechFrequency(Preferences.SPEECH_ALWAYS);
                        break;
                }
            }
        });

        // select the proper radio button based on current setting
        float freq = mPreferences.getSpeechFrequency();
        if (freq == Preferences.SPEECH_NEVER) {
            grpSpeechFreqGroup.check(R.id.speech_never);
        } else if (freq == Preferences.SPEECH_RARE) {
            grpSpeechFreqGroup.check(R.id.speech_rare);
        } else if (freq == Preferences.SPEECH_OFTEN) {
            grpSpeechFreqGroup.check(R.id.speech_often);
        } else if (freq == Preferences.SPEECH_ALWAYS) {
            grpSpeechFreqGroup.check(R.id.speech_always);
        }

        // Handle App Brightness radio group
        grpAppBrightness = (RadioGroup)view.findViewById(R.id.brightness_group);
        grpAppBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.brightness_vlow:
                        mPreferences.setAppBrightness(getActivity(), Preferences.BRIGHTNESS_VERYLOW);
                        break;
                    case R.id.brightness_low:
                        mPreferences.setAppBrightness(getActivity(), Preferences.BRIGHTNESS_LOW);
                        break;
                    case R.id.brightness_medium:
                        mPreferences.setAppBrightness(getActivity(), Preferences.BRIGHTNESS_MEDIUM);
                        break;
                    case R.id.brightness_high:
                        mPreferences.setAppBrightness(getActivity(), Preferences.BRIGHTNESS_HIGH);
                        break;
                    case R.id.brightness_vhigh:
                        mPreferences.setAppBrightness(getActivity(), Preferences.BRIGHTNESS_VERYHIGH);
                        break;
                }
            }
        });

        int appBrightness = mPreferences.getAppBrightness();
        switch (appBrightness){
            case Preferences.BRIGHTNESS_VERYLOW:
                grpAppBrightness.check(R.id.brightness_vlow);
                break;
            case Preferences.BRIGHTNESS_LOW:
                grpAppBrightness.check(R.id.brightness_low);
                break;
            case Preferences.BRIGHTNESS_MEDIUM:
                grpAppBrightness.check(R.id.brightness_medium);
                break;
            case Preferences.BRIGHTNESS_HIGH:
                grpAppBrightness.check(R.id.brightness_high);
                break;
            case Preferences.BRIGHTNESS_VERYHIGH:
                grpAppBrightness.check(R.id.brightness_vhigh);
                break;
        }

        // Handle LightBrightness settings
        grpLightBrightness = (RadioGroup) view.findViewById(R.id.light_group);
        grpLightBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.light_vlow:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_VERYLOW);
                        break;
                    case R.id.light_low:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_LOW);
                        break;
                    case R.id.light_medium:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_MEDIUM);
                        break;
                    case R.id.light_high:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_HIGH);
                        break;
                    case R.id.light_vhigh:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_VERYHIGH);
                        break;
                }
            }
        });

        int lightBrightness = mPreferences.getLightBrightness();
        switch (lightBrightness){
            case Preferences.BRIGHTNESS_VERYLOW:
                grpLightBrightness.check(R.id.light_vlow);
                break;
            case Preferences.BRIGHTNESS_LOW:
                grpLightBrightness.check(R.id.light_low);
                break;
            case Preferences.BRIGHTNESS_MEDIUM:
                grpLightBrightness.check(R.id.light_medium);
                break;
            case Preferences.BRIGHTNESS_HIGH:
                grpLightBrightness.check(R.id.light_high);
                break;
            case Preferences.BRIGHTNESS_VERYHIGH:
                grpLightBrightness.check(R.id.light_vhigh);
                break;
        }

        // TODO: handle weather display units (C / F) (kph / mph)

        return view;
    }
}
