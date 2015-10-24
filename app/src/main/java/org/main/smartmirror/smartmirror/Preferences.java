package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private SharedPreferences sharedPreferences;

    // constants define the names of the values to be saved to the storage file
    public static final String PREFS_NAME = "MIRROR_PREFS";
    public static final String PREFS_VOL = "MIRROR_PREFS_VOL";
    public static final String PREFS_TEMP_UNIT = "MIRROR_PREFS_TEMP_UNIT";
    public static final String PREFS_SPEECH_FREQ = "MIRROR_PREFS_SPEECH_FREQ";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";
    public static final String PREFS_WIND_FORMAT = "MIRROR_PREFS_WIND_FORMAT";

    public static final int FAHRENHEIT = 0;
    public static final int CELSIUS = 1;
    public static final String MPH = "mph";
    public static final String KPH = "kph";

    private int mTempUnits = FAHRENHEIT;    // control which unit scale to use for temperature (0,1)
    private float mVolume;                  // control volume for TTS and other sounds (0-1)
    private float mSpeechFrequency;         // control how often TTS voice responses occur (0-1)
    private String mDateFormat = "EEE, LLL d";    // SimpleDateFormat string for displaying date
    private String mTimeFormat = "h:mm a";         // Default string for displaying time
    private String mWindDisplayFormat = MPH;

    private Preferences() {
        Context appContext = MainActivity.getContextForApplication();
        sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // grab saved values from sharedPreferences if they exist, if not use defaults
        mSpeechFrequency = sharedPreferences.getFloat(PREFS_SPEECH_FREQ, (float) 1);
        mTempUnits = sharedPreferences.getInt(PREFS_TEMP_UNIT, FAHRENHEIT);
        mWindDisplayFormat = sharedPreferences.getString(PREFS_WIND_FORMAT, MPH);
        mVolume = sharedPreferences.getFloat(PREFS_VOL, (float) .8);
    }

    // returns the instance of the Preferences class, or creates one if it does not exist
    public static Preferences getInstance() {
        if (mPreferences == null) {
            mPreferences = new Preferences();
        }
        return mPreferences;
    }

    // ------- VOLUME ---------
    public void setVolume(double vol) {
        float volume = (float) vol;
        if (volume > 1 || volume < 0) {
            return;
        }

        mVolume = volume;
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putFloat(PREFS_VOL, mVolume);
        edit.apply();
    }

    public float getVolume() {
        return mVolume;
    }

    // ------- TEMPERATURE UNITS ---------
    public void setTempUnits(int unit) {
        if (unit == FAHRENHEIT || unit == CELSIUS) {
            mTempUnits = unit;
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putFloat(PREFS_TEMP_UNIT, mTempUnits);
            edit.apply();
        }
    }

    public String getTempUnits() {
        String units;
        if (mTempUnits == FAHRENHEIT) {
            units = "F";
        } else {
            units = "C";
        }
        return units;
    }

    public String getTempFormat(){
        String format;
        if (mTempUnits == FAHRENHEIT) {
            format = "imperial";
        } else
            format = "metric";
        return format;
    }

    // --------- DATE FORMAT  ---------
    public void setDateFormat(String format) {
        // might do some validation here
        mDateFormat = format;
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREFS_DATE_FORMAT, mDateFormat);
        edit.apply();
    }

    public String getDateFormat() {
        return mDateFormat;
    }

    // --------- TIME FORMAT  ---------
    public void setTimeFormat(String format) {
        // might do some validation here
        mTimeFormat = format;
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREFS_TIME_FORMAT, mTimeFormat);
        edit.apply();
    }

    public String getTimeFormat() {
        return mTimeFormat;
    }

    // ----------- WIND DISPLAY FORMAT ------------
    public void setWindDisplayFormat(String format) {
        mWindDisplayFormat = format;
    }

    public String getWindDisplayFormat() {
        return mWindDisplayFormat;
    }

}
