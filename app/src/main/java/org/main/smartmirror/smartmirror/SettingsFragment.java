package org.main.smartmirror.smartmirror;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;


public class SettingsFragment extends Fragment {

    private Preferences mPreferences;
    private Switch swtSpeechEnabled;
    private Switch swtVoiceEnabled;
    private Switch swtRemoteEnabled;
    private Switch swtWeatherEnglish;
    private Switch swtTimeFormat;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        // Speech Enabled Switch
        swtSpeechEnabled = (Switch) view.findViewById(R.id.switch_speech_enabled);
        swtSpeechEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.setSoundOn(isChecked);
                if (isChecked) {
                    swtSpeechEnabled.setText(getResources().getString(R.string.lbl_sound_on));
                } else {
                    swtSpeechEnabled.setText(getResources().getString(R.string.lbl_sound_off));
                }
            }
        });
        swtSpeechEnabled.setChecked(mPreferences.isSoundOn());

        // Voice Enabled Switch
        swtVoiceEnabled = (Switch) view.findViewById(R.id.switch_voice_enabled);
        swtVoiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.setVoiceEnabled(isChecked);
                if (isChecked) {
                    swtVoiceEnabled.setText(getResources().getString(R.string.lbl_voice_on));
                } else {
                    swtVoiceEnabled.setText(getResources().getString(R.string.lbl_voice_off));
                }
            }
        });
        swtVoiceEnabled.setChecked(mPreferences.isVoiceEnabled());

        // Remote Enabled Switch
        swtRemoteEnabled = (Switch) view.findViewById(R.id.switch_remote_enabled);
        swtRemoteEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.setRemoteEnabled(isChecked);
                if (isChecked) {
                    swtRemoteEnabled.setText(getResources().getString(R.string.lbl_remote_on));
                } else {
                    swtRemoteEnabled.setText(getResources().getString(R.string.lbl_remote_off));
                }
            }
        });
        swtRemoteEnabled.setChecked(mPreferences.isRemoteEnabled());

        swtWeatherEnglish = (Switch) view.findViewById(R.id.switch_weather_units);
        swtWeatherEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setWeatherUnits(Preferences.ENGLISH);
                    swtWeatherEnglish.setText(getResources().getString(R.string.lbl_weather_english));
                } else {
                    mPreferences.setWeatherUnits(Preferences.METRIC);
                    swtWeatherEnglish.setText(getResources().getString(R.string.lbl_weather_metric));
                }
            }
        });
        swtWeatherEnglish.setChecked(mPreferences.weatherIsEnglish());

        swtTimeFormat = (Switch) view.findViewById(R.id.switch_time_format);
        swtTimeFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setTimeFormat12hr();
                    swtTimeFormat.setText(getResources().getString(R.string.lbl_time_format_12hr));
                } else {
                    mPreferences.setTimeFormat24hr();
                    swtTimeFormat.setText(getResources().getString(R.string.lbl_time_format_24hr));
                }
            }
        });
        swtTimeFormat.setChecked(mPreferences.isTimeFormat12hr());

        Button changeAccountbtn = (Button) view.findViewById(R.id.change_account_button);
        changeAccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), AccountActivity.class);
                mPreferences.setFirstTimeRun(true);
                startActivity(intent);
            }
        });

        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // If commands are broadcast to this fragment, respond by updating the UI.
    // Updates to Preferences are handled by the receiver for that class.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("SettingsFragment", "Got message:\"" + message + "\"");
            Boolean checked = false;
            switch (message) {

                case Preferences.CMD_SOUND_ON:
                    checked = true;
                case Preferences.CMD_SOUND_OFF:
                    swtSpeechEnabled.setChecked(checked);
                    break;

                case Preferences.CMD_MIRA_SOUND:
                    swtSpeechEnabled.toggle();
                    break;

                // remote on / off
                case Preferences.CMD_REMOTE_ON:
                    checked = true;
                case Preferences.CMD_REMOTE_OFF:
                    swtRemoteEnabled.setChecked(checked);
                    break;

                // voice recognition on / off
                case Preferences.CMD_VOICE_ON:
                    checked = true;
                case Preferences.CMD_VOICE_OFF:
                    swtVoiceEnabled.setChecked(checked);
                    break;

                case Constants.MIRA_LISTEN:
                    swtVoiceEnabled.toggle();
                    break;

                // weather english / metric
                case Preferences.CMD_WEATHER_ENGLISH:
                    checked = true;
                case Preferences.CMD_WEATHER_METRIC:
                    swtWeatherEnglish.setChecked(checked);
                    break;

                // time 12 / 24 hour
                case Preferences.CMD_TIME_12HR:
                    checked = true;
                case Preferences.CMD_TIME_24HR:
                    swtTimeFormat.setChecked(checked);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }
}
