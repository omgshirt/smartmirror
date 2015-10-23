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

    public static final int FAHRENHEIT = 0;
    public static final int CELSIUS = 1;

    private int mTempUnits = FAHRENHEIT; // control which scale to use for weather
    private float mVolume = 1; // control volume for TTS and other sounds
    private float mSpeechFrequency = 1; // control how often TTS voice responses occur
    // Add other values here

    private Preferences() {
        Context appContext = MainActivity.getContextForApplication();
        sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            // set all the default preference values from XML(?)
        } else {
            // load all the preferences from the saved file
        }
    }

    // returns the instance of the Preferences class, or creates one if it does not exist
    public static Preferences getInstance() {
        if (mPreferences == null) {
            mPreferences = new Preferences();
        }
        return mPreferences;
    }

    // example sets volume, then saves that value to the prefs file
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


    public void setTempUnits(int unit) {
        if (unit == FAHRENHEIT || unit == CELSIUS) {
            mTempUnits = unit;
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putFloat(PREFS_TEMP_UNIT, mTempUnits);
            edit.apply();
        }
    }

    public int getTempUnits() {
        return mTempUnits;
    }

}
