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
    private Switch swtCameraEnabled;
    private Switch swtWeatherEnglish;
    private Switch swtTimeFormat;

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
                switch(checkedId){
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

        float vol = mPreferences.getSystemVolume();
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

        vol = mPreferences.getMusicVolume();
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

        // Handle App Brightness radio group
        grpAppBrightness = (RadioGroup)view.findViewById(R.id.brightness_group);
        grpAppBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
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

        int appBrightness = mPreferences.getAppBrightness();
        switch (appBrightness){
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

        // Handle LightBrightness settings
        grpLightBrightness = (RadioGroup) view.findViewById(R.id.light_group);
        grpLightBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
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

        int lightBrightness = mPreferences.getLightBrightness();
        switch (lightBrightness){
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

        // Voice Enabled Switch
        swtVoiceEnabled = (Switch) view.findViewById(R.id.switch_voice_enabled);
        swtVoiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setVoiceEnabled(true);
                } else {
                    mPreferences.setVoiceEnabled(false);
                }
                setVoiceSwitchText();
            }
        });
        swtVoiceEnabled.setChecked(mPreferences.isVoiceEnabled());
        setVoiceSwitchText();

        // Camera Enabled Switch
        swtCameraEnabled = (Switch) view.findViewById(R.id.switch_camera_enabled);
        swtCameraEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setCameraEnabled(true);
                } else {
                    mPreferences.setCameraEnabled(false);
                }
                setCameraSwitchText();
            }
        });
        swtCameraEnabled.setChecked(mPreferences.isCameraEnabled());
        setCameraSwitchText();

        // Remote Enabled Switch
        swtRemoteEnabled = (Switch) view.findViewById(R.id.switch_remote_enabled);
        swtRemoteEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setRemoteEnabled(true);
                } else {
                    mPreferences.setRemoteEnabled(false);
                }
                setRemoteSwitchText();
            }
        });
        swtRemoteEnabled.setChecked(mPreferences.isRemoteEnabled());
        setRemoteSwitchText();

        swtWeatherEnglish = (Switch) view.findViewById(R.id.switch_weather_units);
        swtWeatherEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setWeatherUnits(Preferences.ENGLISH);
                } else {
                    mPreferences.setWeatherUnits(Preferences.METRIC);
                }
                setWeatherSwitchText();
            }
        });
        swtWeatherEnglish.setChecked(mPreferences.weatherIsEnglish());
        setWeatherSwitchText();

        swtTimeFormat = (Switch) view.findViewById(R.id.switch_time_format);
        swtTimeFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreferences.setTimeFormat12hr();
                } else {
                    mPreferences.setTimeFormat24hr();
                }
                setTimeSwitchText();
            }
        });
        swtTimeFormat.setChecked(mPreferences.isTimeFormat12hr());
        setTimeSwitchText();

        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // If commands are broadcast to this fragment, respond by updating the UI
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("SettingsFragment", "Got message:\"" + message + "\"");
            Boolean checked = false;
            switch (message) {
                case Preferences.CMD_CAMERA_ON:
                    checked = true;
                case Preferences.CMD_CAMERA_OFF:
                    swtCameraEnabled.setChecked(checked);
                    setCameraSwitchText();
                    break;

                case Preferences.CMD_REMOTE_ON:
                    checked = true;
                case Preferences.CMD_REMOTE_OFF:
                    swtRemoteEnabled.setChecked(checked);
                    setRemoteSwitchText();
                    break;

                case Preferences.CMD_VOICE_ON:
                    checked = true;
                case Preferences.CMD_VOICE_OFF:
                    swtVoiceEnabled.setChecked(checked);
                    setVoiceSwitchText();
                    break;

                case Preferences.CMD_WEATHER_ENGLISH:
                    checked = true;
                case Preferences.CMD_WEATHER_METRIC:
                    swtWeatherEnglish.setChecked(checked);
                    setWeatherSwitchText();
                    break;

                case Preferences.CMD_TIME_12HR:
                    checked = true;
                case Preferences.CMD_TIME_24HR:
                    swtTimeFormat.setChecked(checked);
                    setTimeSwitchText();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private void setVoiceSwitchText() {
        setSwitchText(swtVoiceEnabled, "Voice Recognition: On", "Voice Recognition: Off" );
    }

    private void setWeatherSwitchText() {
        setSwitchText(swtWeatherEnglish, "Weather Units: English", "Weather Units: Metric" );
    }

    private void setRemoteSwitchText(){
        setSwitchText(swtRemoteEnabled, "Remote Control: On", "Remote Control: Off" );
    }

    private void setCameraSwitchText(){
        setSwitchText(swtCameraEnabled, "Camera: On", "Camera: Off" );
    }

    private void setTimeSwitchText() {
        setSwitchText(swtTimeFormat , "Time Format: 12hr", "Time Format: 24hr");
    }

    private void setSwitchText(Switch switchWidget, String isCheckedText, String notCheckedText) {
        if (switchWidget.isChecked()) {
            switchWidget.setText(isCheckedText);
        } else {
            switchWidget.setText(notCheckedText);
        }
    }
}
