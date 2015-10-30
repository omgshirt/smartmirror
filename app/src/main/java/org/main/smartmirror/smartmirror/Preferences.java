package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final String PREFS_VOL = "MIRROR_PREFS_VOL";
    public static final String PREFS_WEATHER_UNIT = "MIRROR_PREFS_WEATHER_UNIT";
    public static final String PREFS_SPEECH_FREQ = "MIRROR_PREFS_SPEECH_FREQ";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";
    public static final String PREFS_LIGHT_BRIGHTNESS = "MIRROR_PREFS_LIGHT_BRIGHTNESS";
    public static final String PREFS_APP_BRIGHTNESS = "MIRROR_PREFS_APP_BRIGHTNESS";
    public static final String PREFS_CAMERA_ENABLED = "MIRROR_PREFS_CAMERA_ENABLED";
    public static final String PREFS_WAKEON_SOUND = "MIRROR_PREFS_WAKEON_SOUND";

    // chance for TTS to happen (0-1)
    public static final float SPEECH_NEVER = 0;
    public static final float SPEECH_RARE = .25f;
    public static final float SPEECH_OFTEN = .5f;
    public static final float SPEECH_ALWAYS = 1;

    // Constants for screen brightness (0-255)
    public static final int BRIGHTNESS_VERYLOW = 10;
    public static final int BRIGHTNESS_LOW = 60;
    public static final int BRIGHTNESS_MEDIUM = 120;
    public static final int BRIGHTNESS_HIGH = 160;
    public static final int BRIGHTNESS_VERYHIGH = 250;
    public static final int BRIGHTNESS_DEFAULT = BRIGHTNESS_MEDIUM;

    public static final float VOLUME_DEFAULT = 0.8f;        // not used currently

    public static final int IMPERIAL = 0;
    public static final int METRIC = 1;
    public static final String MPH = "mph";
    public static final String KPH = "kph";

    private int mDisplayUnits = IMPERIAL;    // control which unit scale to use for temperature (0,1)
    private float mVolume;                  // control volume for TTS and other sounds (0-1)
    private float mSpeechFrequency;         // control how often TTS voice responses occur (0-1)
    private String mDateFormat = "EEE, LLL d";    // SimpleDateFormat string for displaying date
    private String mTimeFormat = "h:mm a";         // Default string for displaying time
    private int mLightBrightness;                  // Night light brightness
    private int mAppBrightness;                     // general brightness setting for other modes

    private Preferences() {
        Context appContext = MainActivity.getContextForApplication();
        mSharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // grab saved values from mSharedPreferences if they exist, if not use defaults
        mSpeechFrequency = mSharedPreferences.getFloat(PREFS_SPEECH_FREQ, SPEECH_ALWAYS);
        mDisplayUnits = mSharedPreferences.getInt(PREFS_WEATHER_UNIT, IMPERIAL);
        mVolume = mSharedPreferences.getFloat(PREFS_VOL, VOLUME_DEFAULT);
        mLightBrightness = mSharedPreferences.getInt(PREFS_LIGHT_BRIGHTNESS, BRIGHTNESS_LOW);
        mAppBrightness = mSharedPreferences.getInt(PREFS_APP_BRIGHTNESS, BRIGHTNESS_DEFAULT);
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

    /**
     *
     * @param vol set system volume (0-1)
     *            This may not be required
     */
    public void setVolume(float vol) {
        float volume = vol;
        if (volume > 1 || volume < 0) {
            return;
        }

        mVolume = volume;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_VOL, mVolume);
        edit.apply();
    }

    public float getVolume() {
        return mVolume;
    }

    /** Sets weather display as imperial or metric
     *
     * @param unit
     */
    public void setDisplayUnits(int unit) {
        if (unit == IMPERIAL || unit == METRIC) {
            mDisplayUnits = unit;
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putInt(PREFS_WEATHER_UNIT, mDisplayUnits);
            edit.apply();
        }
    }

    public int getDisplayUnits(){
        return mDisplayUnits;
    }

    // get a string representation of the units used for weather display
    public String getDisplayUnitsAsString() {
        if (mDisplayUnits == IMPERIAL)  { return "imperial"; }
        else                            { return  "metric"; }
    }

    // returns the unicode string for deg C or deg F based on the WeatherIcons font set
    public String getTempUnits() {
        String units;
        Context appContext = MainActivity.getContextForApplication();
        if (mDisplayUnits == IMPERIAL)  {
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

    public String getWindDisplayFormat() {
        String val;
        if (mDisplayUnits == IMPERIAL) {
            val = MPH;
        } else
            val = KPH;
        return val;
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
}
