package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;


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
    public static final String PREFS_WEATHER_UNIT = "MIRROR_PREFS_WEATHER_UNIT";
    public static final String PREFS_SPEECH_FREQ = "MIRROR_PREFS_SPEECH_FREQ";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";
    public static final String PREFS_LIGHT_BRIGHTNESS = "MIRROR_PREFS_LIGHT_BRIGHTNESS";
    public static final String PREFS_APP_BRIGHTNESS = "MIRROR_PREFS_APP_BRIGHTNESS";
    public static final String PREFS_CAMERA_ENABLED = "MIRROR_PREFS_CAMERA_ENABLED";
    public static final String PREFS_WAKEON_SOUND = "MIRROR_PREFS_WAKEON_SOUND";
    public static final String PREFS_REMOTE_ENABLED = "MIRROR_PREFS_REMOTE_ENABLED";
    public static final String PREFS_MUSIC_VOL = "MIRROR_PREFS_MUSIC_VOL";

    // chance for TTS to happen (0-1)
    public static final float SPEECH_NEVER = 0;
    public static final float SPEECH_RARE = .25f;
    public static final float SPEECH_OFTEN = .5f;
    public static final float SPEECH_ALWAYS = 1;

    // Constants for screen brightness (0-255)
    public static final int BRIGHTNESS_VLOW= 10;
    public static final int BRIGHTNESS_LOW = 50;
    public static final int BRIGHTNESS_MEDIUM = 100;
    public static final int BRIGHTNESS_HIGH = 150;
    public static final int BRIGHTNESS_VHIGH = 225;

    // constants for volumes
    public static final float VOL_OFF = 0f;
    public static final float VOL_VLOW = .2f;
    public static final float VOL_LOW = .4f;
    public static final float VOL_MEDIUM = .6f;
    public static final float VOL_HIGH = .8f;
    public static final float VOL_VHIGH = 1.0f;

    public static final int ENGLISH = 0;
    public static final int METRIC = 1;
    public static final String MPH = "mph";
    public static final String KPH = "kph";

    private boolean mRemoteEnabled;
    private int mWeatherUnits;                      // Weather display format (English / metric)
    private float mSystemVolume;                    // control general system volume
    private float mMusicVolume;                     // music stream volume
    private float mSpeechFrequency;                 // control how often TTS voice responses occur (0-1)
    private String mDateFormat = "EEE, LLL d";      // SimpleDateFormat string for date display
    private String mTimeFormat = "h:mm a";          // Default string for time display
    private int mLightBrightness;                   // Night light brightness
    private int mAppBrightness;                     // general screen brightness

    private Preferences() {
        Context appContext = MainActivity.getContextForApplication();
        mSharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // grab saved values from mSharedPreferences if they exist, if not use defaults
        mSpeechFrequency = mSharedPreferences.getFloat(PREFS_SPEECH_FREQ, SPEECH_ALWAYS);
        mMusicVolume = mSharedPreferences.getFloat(PREFS_MUSIC_VOL, VOL_MEDIUM);
        mSystemVolume = mSharedPreferences.getFloat(PREFS_SYSTEM_VOL, VOL_MEDIUM);
        mAppBrightness = mSharedPreferences.getInt(PREFS_APP_BRIGHTNESS, BRIGHTNESS_MEDIUM);
        mLightBrightness = mSharedPreferences.getInt(PREFS_LIGHT_BRIGHTNESS, BRIGHTNESS_LOW);
        mRemoteEnabled = mSharedPreferences.getBoolean(PREFS_REMOTE_ENABLED, true);
        mWeatherUnits = mSharedPreferences.getInt(PREFS_WEATHER_UNIT, ENGLISH);
    }

    // Clean up any refs that might hang around to prevent leaks.
    public void destroy(){
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

    /** Sets weather display as imperial or metric
     *
     * @param unit
     */
    public void setWeatherUnits(int unit) {
        if (unit == ENGLISH || unit == METRIC) {
            mWeatherUnits = unit;
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putInt(PREFS_WEATHER_UNIT, mWeatherUnits);
            edit.apply();
        }
    }

    public int getWeatherUnits(){
        return mWeatherUnits;
    }

    // get a string representation of the units used for weather display
    public String getDisplayUnitsAsString() {
        if (mWeatherUnits == ENGLISH)  { return "imperial"; }
        else                            { return  "metric"; }
    }

    // returns the unicode string for deg C or deg F based on the WeatherIcons font set
    public String getTempUnits() {
        String units;
        Context appContext = MainActivity.getContextForApplication();
        if (mWeatherUnits == ENGLISH)  {
            units = appContext.getResources().getString(R.string.weather_deg_f);
        }
        else {
            units = appContext.getResources().getString(R.string.weather_deg_c);
        }
        return units;
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

    /** set string used to format time dis
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
        mAppBrightness = brightness;

        ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
        sbh.setScreenBrightness(activity, mAppBrightness);

        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(PREFS_APP_BRIGHTNESS, mAppBrightness);
        edit.apply();
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

    public void setRemoteEnabled(boolean isEnabled) {
        mRemoteEnabled = isEnabled;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_REMOTE_ENABLED, mRemoteEnabled);
        edit.apply();
    }
}
