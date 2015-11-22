package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


/**
 * Created by Brian on 10/22/2015.
 *
 * This is a singleton class to hold preferences for the application
 * Get the instance of it by using the getInstance() method
 *
 * Class is created at MainActivity start and loads the SharedPrefences for the application
 * Access is by getters and setters, which also handle file storage:
 */
public class Preferences {

    private static Preferences mPreferences = null;
    private SharedPreferences mSharedPreferences;

    // constants define the names of the values to be saved to the storage file
    public static final String PREFS_NAME = "MIRROR_PREFS";
    public static final String PREFS_SYSTEM_VOL = "MIRROR_PREFS_VOL";
    public static final String PREFS_MUSIC_VOL = "MIRROR_PREFS_MUSIC_VOL";

    public static final String PREFS_CAMERA_ENABLED = "MIRROR_PREFS_CAMERA_ENABLED";
    public static final String PREFS_VOICE_ENABLED = "MIRROR_PREFS_VOICE_ENABLED";
    public static final String PREFS_REMOTE_ENABLED = "MIRROR_PREFS_REMOTE_ENABLED";
    public static final String PREFS_SPEECH_FREQ = "MIRROR_PREFS_SPEECH_FREQ";

    public static final String PREFS_WEATHER_UNIT = "MIRROR_PREFS_WEATHER_UNIT";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";

    public static final String PREFS_LIGHT_BRIGHTNESS = "MIRROR_PREFS_LIGHT_BRIGHTNESS";
    public static final String PREFS_APP_BRIGHTNESS = "MIRROR_PREFS_APP_BRIGHTNESS";

    // chance for TTS to happen (0-1)
    public static final float SPEECH_NEVER = 0;
    public static final float SPEECH_RARE = .25f;
    public static final float SPEECH_OFTEN = .5f;
    public static final float SPEECH_ALWAYS = 1;

    // Constants for screen brightness (0-255)
    public static final int BRIGHTNESS_VLOW = 10;
    public static final int BRIGHTNESS_LOW = 40;
    public static final int BRIGHTNESS_MEDIUM = 80;
    public static final int BRIGHTNESS_HIGH = 130;
    public static final int BRIGHTNESS_VHIGH = 225;

    // constants for volumes
    public static final float VOL_OFF = 0f;
    public static final float VOL_VLOW = .1f;
    public static final float VOL_LOW = .3f;
    public static final float VOL_MEDIUM = .5f;
    public static final float VOL_HIGH = .7f;
    public static final float VOL_VHIGH = 1.0f;

    // strings
    public static final String CMD_CAMERA_ON = "camera on";
    public static final String CMD_CAMERA_OFF = "camera off";

    public static final String CMD_LIGHT_VLOW = "light very low";
    public static final String CMD_LIGHT_LOW = "light low";
    public static final String CMD_LIGHT_MEDIUM = "light medium";
    public static final String CMD_LIGHT_HIGH = "light high";
    public static final String CMD_LIGHT_VHIGH= "light very high";

    public static final String CMD_MUSIC_OFF = "music off";
    public static final String CMD_MUSIC_VLOW = "music very low";
    public static final String CMD_MUSIC_LOW = "music low";
    public static final String CMD_MUSIC_MEDIUM = "music medium";
    public static final String CMD_MUSIC_HIGH = "music high";
    public static final String CMD_MUSIC_VHIGH= "music very high";

    public static final String CMD_REMOTE_ON = "remote on";
    public static final String CMD_REMOTE_OFF = "remote off";

    public static final String CMD_SCREEN_VLOW = "screen very low";
    public static final String CMD_SCREEN_LOW = "screen low";
    public static final String CMD_SCREEN_MEDIUM = "screen medium";
    public static final String CMD_SCREEN_HIGH = "screen high";
    public static final String CMD_SCREEN_VHIGH= "screen very high";

    public static final String CMD_SPEECH_NEVER = "speech never";
    public static final String CMD_SPEECH_RARE = "speech rare";
    public static final String CMD_SPEECH_OFTEN = "speech often";
    public static final String CMD_SPEECH_ALWAYS = "speech always";

    public static final String CMD_VOICE_OFF = "stop listening";
    public static final String CMD_VOICE_ON = "start listening";

    public static final String CMD_VOLUME_OFF = "volume off";
    public static final String CMD_VOLUME_VLOW = "volume very low";
    public static final String CMD_VOLUME_LOW = "volume low";
    public static final String CMD_VOLUME_MEDIUM = "volume medium";
    public static final String CMD_VOLUME_HIGH = "volume high";
    public static final String CMD_VOLUME_VHIGH= "volume very high";

    public static final String CMD_WEATHER_ENGLISH = "weather english";
    public static final String CMD_WEATHER_METRIC = "weather metric";

    public static final String OFF = "off";
    public static final String ON = "on";
    public static final String ENGLISH = "imperial";
    public static final String LISTENING = "listening";
    public static final String METRIC = "metric";

    public static final String MPH = "mph";
    public static final String KPH = "kph";

    private int mAppBrightness;                     // general screen brightness
    private int mLightBrightness;                   // Night light brightness

    private boolean mRemoteEnabled;                 // Enable / disable remote control connections
    private boolean mCameraEnabled;                 // Enable / disable all camera-related actions
    private boolean mVoiceEnabled;                  // Enable / disable voice recognition UNTIL keyword spoken
    private float mSpeechFrequency;                 // control how often TTS voice responses occur (0-1)

    private float mSystemVolume;                    // control general system volume
    private float mMusicVolume;                     // music stream volume

    private String mDateFormat = "EEE, LLL d";      // SimpleDateFormat string for date display
    private String mTimeFormat = "h:mm a";          // Default string for time display
    private String mWeatherUnits;                      // Weather display format (English / metric)


    // Handle any messages sent from MainActivity
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("Preferences", "Got message: " + message);
            handleSettingsCommand(context, message);
        }
    };

    private void handleSettingsCommand(Context context, String command) {
        switch (command) {
            //camera
            case CMD_CAMERA_OFF:
                setCameraEnabled(false);
                break;
            case CMD_CAMERA_ON:
                setCameraEnabled(true);
                break;

            // Light
            case CMD_LIGHT_VLOW:
                setLightBrightness(BRIGHTNESS_VLOW);
                break;
            case CMD_LIGHT_LOW:
                setLightBrightness(BRIGHTNESS_LOW);
                break;
            case CMD_LIGHT_MEDIUM:
                setLightBrightness(BRIGHTNESS_MEDIUM);
                break;
            case CMD_LIGHT_HIGH:
                setLightBrightness(BRIGHTNESS_HIGH);
                break;
            case CMD_LIGHT_VHIGH:
                setLightBrightness(BRIGHTNESS_VHIGH);
                break;

            // Music
            case CMD_MUSIC_OFF:
                setMusicVolume(VOL_OFF);
                break;
            case CMD_MUSIC_VLOW:
                setMusicVolume(VOL_VLOW);
                break;
            case CMD_MUSIC_LOW:
                setMusicVolume(VOL_LOW);
                break;
            case CMD_MUSIC_MEDIUM:
                setMusicVolume(VOL_MEDIUM);
                break;
            case CMD_MUSIC_HIGH:
                setMusicVolume(VOL_HIGH);
                break;
            case CMD_MUSIC_VHIGH:
                setMusicVolume(VOL_VHIGH);
                break;

            // Remote
            case CMD_REMOTE_OFF:
                // TODO: FIX THIS
                break;
            case CMD_REMOTE_ON:
                break;

            // screen brightness
                // TODO: fix how this works, too
            case CMD_SCREEN_VLOW:
                break;
            case CMD_SCREEN_LOW:
                break;
            case CMD_SCREEN_MEDIUM:
                break;
            case CMD_SCREEN_HIGH:
                break;
            case CMD_SCREEN_VHIGH:
                break;

            // speech frequency
            case CMD_SPEECH_NEVER:
                setSpeechFrequency(SPEECH_NEVER);
                break;
            case CMD_SPEECH_RARE:
                setSpeechFrequency(SPEECH_RARE);
                break;
            case CMD_SPEECH_OFTEN:
                setSpeechFrequency(SPEECH_OFTEN);
                break;
            case CMD_SPEECH_ALWAYS:
                setSpeechFrequency(SPEECH_ALWAYS);
                break;

            // Voice recognition on / off
            case CMD_VOICE_OFF:
                setVoiceEnabled(false);
                break;
            case CMD_VOICE_ON:
                ((MainActivity)context).startTTS(LISTENING);
                setVoiceEnabled(true);
                break;

            // system volume
            case CMD_VOLUME_OFF:
                setSystemVolume(VOL_OFF);
                break;
            case CMD_VOLUME_VLOW:
                setSystemVolume(VOL_VLOW);
                break;
            case CMD_VOLUME_LOW:
                setSystemVolume(VOL_LOW);
                break;
            case CMD_VOLUME_MEDIUM:
                setSystemVolume(VOL_MEDIUM);
                break;
            case CMD_VOLUME_HIGH:
                setSystemVolume(VOL_HIGH);
                break;
            case CMD_VOLUME_VHIGH:
                setSystemVolume(VOL_VHIGH);
                break;

            // weather units
            case CMD_WEATHER_ENGLISH:
                setWeatherUnits(ENGLISH);
                break;
            case CMD_WEATHER_METRIC:
                setWeatherUnits(METRIC);
                break;

            default:
                break;
        }

    }

    private Preferences() {
        Context appContext = MainActivity.getContextForApplication();
        mSharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // grab saved values from mSharedPreferences if they exist, if not use defaults
        mSpeechFrequency = mSharedPreferences.getFloat(PREFS_SPEECH_FREQ, SPEECH_ALWAYS);
        mMusicVolume = mSharedPreferences.getFloat(PREFS_MUSIC_VOL, VOL_VLOW);
        mSystemVolume = mSharedPreferences.getFloat(PREFS_SYSTEM_VOL, VOL_VLOW);
        mAppBrightness = mSharedPreferences.getInt(PREFS_APP_BRIGHTNESS, BRIGHTNESS_MEDIUM);
        mLightBrightness = mSharedPreferences.getInt(PREFS_LIGHT_BRIGHTNESS, BRIGHTNESS_LOW);
        mWeatherUnits = mSharedPreferences.getString(PREFS_WEATHER_UNIT, ENGLISH);

        mRemoteEnabled = mSharedPreferences.getBoolean(PREFS_REMOTE_ENABLED, true);
        mCameraEnabled = mSharedPreferences.getBoolean(PREFS_CAMERA_ENABLED, true);
        mVoiceEnabled = mSharedPreferences.getBoolean(PREFS_VOICE_ENABLED, true);

        // This may not work (giving appContext)
        LocalBroadcastManager.getInstance(appContext).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // Clean up any refs that might hang around to prevent leaks.
    public void destroy(){
        Context appContext = MainActivity.getContextForApplication();
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(mMessageReceiver);
        mPreferences = null;
        mSharedPreferences = null;
    }

    // returns the instance of the Preferences class, or creates one if it does not exist
    public static Preferences getInstance() {
        if (mPreferences == null) {
            mPreferences = new Preferences();
        }
        return mPreferences;
    }

    public float getSystemVolume() {
        return mSystemVolume;
    }

    /**
     *
     * @param vol set system volume (0-1)
     *
     */
    public void setSystemVolume(float vol) {
        if (vol < 0 || vol > 1) return;
        mSystemVolume = vol;
        setStreamVolume(vol, AudioManager.STREAM_SYSTEM);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_SYSTEM_VOL, mSystemVolume);
        edit.apply();
    }

    public float getMusicVolume() {
        return mMusicVolume;
    }

    /**
     * Sets the volume for music stream
     * @param vol value to set (0-1)
     */
    public void setMusicVolume(float vol) {
        if (vol < 0 || vol > 1) return;
        mMusicVolume = vol;
        setStreamVolume(vol, AudioManager.STREAM_MUSIC);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_MUSIC_VOL, mMusicVolume);
        edit.apply();
    }

    // private helper to set the vol to the given stream
    // Gets the max volume allowed for this stream, then sets the volume
    private void setStreamVolume(float vol, int stream) {
        Context context = MainActivity.getContextForApplication();
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(stream);
        int setVol = (int)(vol * max);
        am.setStreamVolume(stream, setVol, 0);
    }

    /** Sets weather display as english or metric
     *
     * @param unit Units to display ( 1=English / 0=Metric)
     */
    public void setWeatherUnits(String unit) {
        if (unit.equals(ENGLISH) || unit.equals(METRIC)) {
            mWeatherUnits = unit;
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putString(PREFS_WEATHER_UNIT, mWeatherUnits);
            edit.apply();
        }
    }

    public String getWeatherUnits(){
        return mWeatherUnits;
    }

    /**
     * Converts fahrenheit temps to the appropriate unit, rounded to the nearest degree
     * @param temp temp in degrees F
     * @return converted temp
     */
    public int convertTemperature(double temp) {
        if (mWeatherUnits.equals(METRIC)) {
            temp = (temp - 32) * 5 / 9;
        }
        return (int)Math.round(temp);
    }

    // returns the unicode string for deg C or deg F based on the WeatherIcons font set
    public String getTempString() {
        String units;
        Context appContext = MainActivity.getContextForApplication();
        if ( mWeatherUnits.equals(ENGLISH) )  {
            units = appContext.getResources().getString(R.string.weather_deg_f);
        }
        else {
            units = appContext.getResources().getString(R.string.weather_deg_c);
        }
        return units;
    }

    /**
     * Converts mph into the appropriate unit, rounded to the nearest unit per hour
     * @param speed in mph
     * @return speed in converted units
     */
    public int convertWindSpeed(double speed) {
        if (mWeatherUnits.equals(METRIC)) {
            speed *= 1.609;
        }
        return (int)Math.round(speed);
    }


    /** Set the string used to format date display
     *
     * @param format string for displaying date in SimpleDateFormat
     */
    public void setDateFormat(String format) {
        // might do some validation here
        mDateFormat = format;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_DATE_FORMAT, mDateFormat);
        edit.apply();
    }

    public String getDateFormat() {
        return mDateFormat;
    }

    /** Format time display for clock
     *
     * @param format string for displaying time in SimpleDateFormat
     */
    public void setTimeFormat(String format) {
        // might do some validation here
        mTimeFormat = format;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_TIME_FORMAT, mTimeFormat);
        edit.apply();
    }

    public String getTimeFormat() {
        return mTimeFormat;
    }

    /**
     *
     * @param frequency how often the TTS should speak (0-1)
     */
    public void setSpeechFrequency(float frequency) {
        if (frequency < 0 || frequency > 1) return;
        mSpeechFrequency = frequency;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_SPEECH_FREQ, mSpeechFrequency);
        edit.apply();
    }

    public float getSpeechFrequency() {
        return mSpeechFrequency;
    }

    /** Set brightness value used by night light
     *
     *  @param brightness int (0-255)
     */
    public void setLightBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) return;

        mLightBrightness = brightness;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(PREFS_LIGHT_BRIGHTNESS, mLightBrightness);
        edit.apply();

    }

    public int getLightBrightness () {
        return mLightBrightness;
    }

    /** Set brightness value for the application
     *
     *  @param brightness int (0-255)
     */
    public void setAppBrightness(Activity activity, int brightness) {
        if (brightness < 0 || brightness > 255) return;

        try {
            this.mAppBrightness = brightness;
            ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
            sbh.setScreenBrightness(activity, mAppBrightness);

            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putInt(PREFS_APP_BRIGHTNESS, mAppBrightness);
            edit.apply();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sets the application's current brightness to value stored in preferences
     *  Requires Activity context
     */
    public void setAppBrightness(Activity activity) {
        setAppBrightness(activity, mAppBrightness);
    }

    public int getAppBrightness () {
        return mAppBrightness;
    }

    public boolean isRemoteEnabled() {
        return  mRemoteEnabled;
    }

    /**
     * Set whether the app will broadcast for wifi connections
     * @param activity instance of MainActivity
     * @param isEnabled boolean
     */
    public void setRemoteEnabled(Activity activity, boolean isEnabled) {
        try {
            mRemoteEnabled = isEnabled;
            ((MainActivity)activity).setRemoteStatus(mRemoteEnabled);
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean(PREFS_REMOTE_ENABLED, mRemoteEnabled);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isVoiceEnabled() {
        return mVoiceEnabled;
    }

    /**
     * Sets the voice enabled status
     * @param mVoiceEnabled boolean
     */
    public void setVoiceEnabled( boolean mVoiceEnabled) {
        this.mVoiceEnabled = mVoiceEnabled;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_VOICE_ENABLED, mVoiceEnabled);
        edit.apply();
    }

    public boolean isCameraEnabled() {
        return mCameraEnabled;
    }

    public void setCameraEnabled(boolean mCameraEnabled) {
        this.mCameraEnabled = mCameraEnabled;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_CAMERA_ENABLED, mCameraEnabled);
        edit.apply();
    }
}
