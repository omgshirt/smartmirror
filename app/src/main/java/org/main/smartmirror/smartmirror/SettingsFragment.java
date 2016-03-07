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
import android.widget.RadioGroup;
import android.widget.Switch;


public class SettingsFragment extends Fragment {

    private Preferences mPreferences;
    private RadioGroup grpAppBrightness;
    private RadioGroup grpLightBrightness;
    private RadioGroup grpSysVolume;
    private RadioGroup grpSpeechVolume;
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
        if (getArguments() != null) {
        }
        mPreferences = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        grpSysVolume = (RadioGroup) view.findViewById(R.id.sys_vol_group);
        grpSysVolume.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sys_vol_off:
                        mPreferences.setSystemVolume(Preferences.VOL_OFF);
                        break;
                    case R.id.sys_vol_vlow:
                        mPreferences.setSystemVolume(Preferences.VOL_VLOW);
                        break;
                    case R.id.sys_vol_low:
                        mPreferences.setSystemVolume(Preferences.VOL_LOW);
                        break;
                    case R.id.sys_vol_medium:
                        mPreferences.setSystemVolume(Preferences.VOL_MEDIUM);
                        break;
                    case R.id.sys_vol_high:
                        mPreferences.setSystemVolume(Preferences.VOL_HIGH);
                        break;
                    case R.id.sys_vol_vhigh:
                        mPreferences.setSystemVolume(Preferences.VOL_VHIGH);
                        break;
                }
            }
        });

        setSystemVolume(mPreferences.getSystemVolume());

        grpSpeechVolume = (RadioGroup) view.findViewById(R.id.mus_vol_group);
        grpSpeechVolume.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.mus_vol_off:
                        mPreferences.setMusicVolume(Preferences.VOL_OFF);
                        break;
                    case R.id.mus_vol_vlow:
                        mPreferences.setMusicVolume(Preferences.VOL_VLOW);
                        break;
                    case R.id.mus_vol_low:
                        mPreferences.setMusicVolume(Preferences.VOL_LOW);
                        break;
                    case R.id.mus_vol_medium:
                        mPreferences.setMusicVolume(Preferences.VOL_MEDIUM);
                        break;
                    case R.id.mus_vol_high:
                        mPreferences.setMusicVolume(Preferences.VOL_HIGH);
                        break;
                    case R.id.mus_vol_vhigh:
                        mPreferences.setMusicVolume(Preferences.VOL_VHIGH);
                        break;
                }
            }
        });

        setMusicVolume(mPreferences.getMusicVolume());

        // App Brightness radio group
        grpAppBrightness = (RadioGroup) view.findViewById(R.id.brightness_group);
        grpAppBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.brightness_vlow:
                        mPreferences.setScreenBrightness(Preferences.BRIGHTNESS_VLOW);
                        break;
                    case R.id.brightness_low:
                        mPreferences.setScreenBrightness(Preferences.BRIGHTNESS_LOW);
                        break;
                    case R.id.brightness_medium:
                        mPreferences.setScreenBrightness(Preferences.BRIGHTNESS_MEDIUM);
                        break;
                    case R.id.brightness_high:
                        mPreferences.setScreenBrightness(Preferences.BRIGHTNESS_HIGH);
                        break;
                    case R.id.brightness_vhigh:
                        mPreferences.setScreenBrightness(Preferences.BRIGHTNESS_VHIGH);
                        break;
                }
            }
        });

        setScreenBrightness(mPreferences.getAppBrightness());

        // Handle LightBrightness settings
        grpLightBrightness = (RadioGroup) view.findViewById(R.id.light_group);
        grpLightBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.light_vlow:
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_VLOW);
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
                        mPreferences.setLightBrightness(Preferences.BRIGHTNESS_VHIGH);
                        break;
                }
            }
        });
        setLightBrightness(mPreferences.getLightBrightness());


        // Voice Enabled Switch
        swtVoiceEnabled = (Switch) view.findViewById(R.id.switch_voice_enabled);
        swtVoiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.setVoiceEnabled(isChecked);
                if (isChecked) {
                    swtVoiceEnabled.setText(getResources().getString(R.string.voice_on));
                } else {
                    swtVoiceEnabled.setText(getResources().getString(R.string.voice_off));
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
                    swtRemoteEnabled.setText(getResources().getString(R.string.remote_on));
                } else {
                    swtRemoteEnabled.setText(getResources().getString(R.string.remote_off));
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
                    swtWeatherEnglish.setText(getResources().getString(R.string.weather_english));
                } else {
                    mPreferences.setWeatherUnits(Preferences.METRIC);
                    swtWeatherEnglish.setText(getResources().getString(R.string.weather_metric));
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
                    swtTimeFormat.setText(getResources().getString(R.string.time_format_12hr));
                } else {
                    mPreferences.setTimeFormat24hr();
                    swtTimeFormat.setText(getResources().getString(R.string.time_format_24hr));
                }
            }
        });
        swtTimeFormat.setChecked(mPreferences.isTimeFormat12hr());

        Button changeAccountbtn = (Button) view.findViewById(R.id.change_account_button);
        changeAccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                mPreferences.setFirstTimeRun(false);
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

                // screen brightness
                case Preferences.CMD_SCREEN_VLOW:
                    setScreenBrightness(Preferences.BRIGHTNESS_VLOW);
                    break;
                case Preferences.CMD_SCREEN_LOW:
                    setScreenBrightness(Preferences.BRIGHTNESS_LOW);
                    break;
                case Preferences.CMD_SCREEN_MEDIUM:
                    setScreenBrightness(Preferences.BRIGHTNESS_MEDIUM);
                    break;
                case Preferences.CMD_SCREEN_HIGH:
                    setScreenBrightness(Preferences.BRIGHTNESS_HIGH);
                    break;
                case Preferences.CMD_SCREEN_VHIGH:
                    setScreenBrightness(Preferences.BRIGHTNESS_VHIGH);
                    break;

                // Light
                case Preferences.CMD_LIGHT_VLOW:
                    setLightBrightness(Preferences.BRIGHTNESS_VLOW);
                    break;
                case Preferences.CMD_LIGHT_LOW:
                    setLightBrightness(Preferences.BRIGHTNESS_LOW);
                    break;
                case Preferences.CMD_LIGHT_MEDIUM:
                    setLightBrightness(Preferences.BRIGHTNESS_MEDIUM);
                    break;
                case Preferences.CMD_LIGHT_HIGH:
                    setLightBrightness(Preferences.BRIGHTNESS_HIGH);
                    break;
                case Preferences.CMD_LIGHT_VHIGH:
                    setLightBrightness(Preferences.BRIGHTNESS_VHIGH);
                    break;

                // system volume
                case Preferences.CMD_VOLUME_OFF:
                    setSystemVolume(Preferences.VOL_OFF);
                    break;
                case Preferences.CMD_VOLUME_VLOW:
                    setSystemVolume(Preferences.VOL_VLOW);
                    break;
                case Preferences.CMD_VOLUME_LOW:
                    setSystemVolume(Preferences.VOL_LOW);
                    break;
                case Preferences.CMD_VOLUME_MEDIUM:
                    setSystemVolume(Preferences.VOL_MEDIUM);
                    break;
                case Preferences.CMD_VOLUME_HIGH:
                    setSystemVolume(Preferences.VOL_HIGH);
                    break;
                case Preferences.CMD_VOLUME_VHIGH:
                    setSystemVolume(Preferences.VOL_VHIGH);
                    break;

                // Speech Volume
                case Preferences.CMD_SPEECH_OFF:
                    setMusicVolume(Preferences.VOL_OFF);
                    break;
                case Preferences.CMD_SPEECH_VLOW:
                    setMusicVolume(Preferences.VOL_VLOW);
                    break;
                case Preferences.CMD_SPEECH_LOW:
                    setMusicVolume(Preferences.VOL_LOW);
                    break;
                case Preferences.CMD_SPEECH_MEDIUM:
                    setMusicVolume(Preferences.VOL_MEDIUM);
                    break;
                case Preferences.CMD_SPEECH_HIGH:
                    setMusicVolume(Preferences.VOL_HIGH);
                    break;
                case Preferences.CMD_SPEECH_VHIGH:
                    setMusicVolume(Preferences.VOL_VHIGH);
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

    // Methods to update UI elements

    public void setSystemVolume(float vol) {
        if (vol == Preferences.VOL_OFF) {
            grpSysVolume.check(R.id.sys_vol_off);
        } else if (vol == Preferences.VOL_VLOW) {
            grpSysVolume.check(R.id.sys_vol_vlow);
        } else if (vol == Preferences.VOL_LOW) {
            grpSysVolume.check(R.id.sys_vol_low);
        } else if (vol == Preferences.VOL_MEDIUM) {
            grpSysVolume.check(R.id.sys_vol_medium);
        } else if (vol == Preferences.VOL_HIGH) {
            grpSysVolume.check(R.id.sys_vol_high);
        } else if (vol == Preferences.VOL_VHIGH) {
            grpSysVolume.check(R.id.sys_vol_vhigh);
        }
    }

    public void setMusicVolume(float vol) {
        if (vol == Preferences.VOL_OFF) {
            grpSpeechVolume.check(R.id.mus_vol_off);
        } else if (vol == Preferences.VOL_VLOW) {
            grpSpeechVolume.check(R.id.mus_vol_vlow);
        } else if (vol == Preferences.VOL_LOW) {
            grpSpeechVolume.check(R.id.mus_vol_low);
        } else if (vol == Preferences.VOL_MEDIUM) {
            grpSpeechVolume.check(R.id.mus_vol_medium);
        } else if (vol == Preferences.VOL_HIGH) {
            grpSpeechVolume.check(R.id.mus_vol_high);
        } else if (vol == Preferences.VOL_VHIGH) {
            grpSpeechVolume.check(R.id.mus_vol_vhigh);
        }
    }

    public void setScreenBrightness(int brightness) {
        switch (brightness) {
            case Preferences.BRIGHTNESS_VLOW:
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
            case Preferences.BRIGHTNESS_VHIGH:
                grpAppBrightness.check(R.id.brightness_vhigh);
                break;
        }
    }

    public void setLightBrightness(int brightness) {
        switch (brightness) {
            case Preferences.BRIGHTNESS_VLOW:
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
            case Preferences.BRIGHTNESS_VHIGH:
                grpLightBrightness.check(R.id.light_vhigh);
                break;
        }
    }
}
