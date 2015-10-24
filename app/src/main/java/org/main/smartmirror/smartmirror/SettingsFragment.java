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
    private RadioGroup mSpeechFreqGroup;

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
        mSpeechFreqGroup = (RadioGroup) view.findViewById(R.id.speech_frequency_group);
        mSpeechFreqGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
            mSpeechFreqGroup.check(R.id.speech_never);
        } else if (freq == Preferences.SPEECH_RARE) {
            mSpeechFreqGroup.check(R.id.speech_rare);
        } else if (freq == Preferences.SPEECH_OFTEN) {
            mSpeechFreqGroup.check(R.id.speech_often);
        } else if (freq == Preferences.SPEECH_ALWAYS) {
            mSpeechFreqGroup.check(R.id.speech_always);
        }

        // TODO: handle weather display units (C / F) (kph / mph)

        return view;
    }
}
