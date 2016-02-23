package org.main.smartmirror.smartmirror;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
public class Preferences implements LocationListener {
    private final String TAG = "Preferences";
    private static Preferences mPreferences = null;
    private SharedPreferences mSharedPreferences;
    private static Activity mActivity;

    //Google Account Email Preference
    public static final String PREFS_GMAIL = "accountName";
    //Google Account Email String
    private static String mUserAccountPref = "";

    // constants define the names of the values to be savked to the storage file
    public static final String PREFS_NAME = "MIRROR_PREFS";
    public static final String PREFS_SYSTEM_VOL = "MIRROR_PREFS_VOL";
    public static final String PREFS_SPEECH_VOL = "MIRROR_PREFS_SPEECH_VOL";

    public static final String PREFS_VOICE_ENABLED = "MIRROR_PREFS_VOICE_ENABLED";
    public static final String PREFS_REMOTE_ENABLED = "MIRROR_PREFS_REMOTE_ENABLED";

    public static final String PREFS_WEATHER_UNIT = "MIRROR_PREFS_WEATHER_UNIT";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";

    public static final String PREFS_LIGHT_BRIGHTNESS = "MIRROR_PREFS_LIGHT_BRIGHTNESS";
    public static final String PREFS_APP_BRIGHTNESS = "MIRROR_PREFS_APP_BRIGHTNESS";

    public static final String PREFS_WORK_LAT = "PREFS_WORK_LAT";
    public static final String PREFS_WORK_LONG = "PREFS_WORK_LONG";
    
    // Constants for screen brightness (0-255)
    public static final int BRIGHTNESS_VLOW = 10;
    public static final int BRIGHTNESS_LOW = 40;
    public static final int BRIGHTNESS_MEDIUM = 80;
    public static final int BRIGHTNESS_HIGH = 130;
    public static final int BRIGHTNESS_VHIGH = 225;

    // constant volumes
    public static final float VOL_OFF = 0f;
    public static final float VOL_VLOW = .2f;
    public static final float VOL_LOW = .4f;
    public static final float VOL_MEDIUM = .6f;
    public static final float VOL_HIGH = .8f;
    public static final float VOL_VHIGH = 1.0f;

    // default for work address
    public static final float WORK_LAT = 0f;
    public static final float WORK_LONG = 0f;
    
    public static final String CMD_LIGHT_VLOW = "light min";
    public static final String CMD_LIGHT_LOW = "light low";
    public static final String CMD_LIGHT_MEDIUM = "light medium";
    public static final String CMD_LIGHT_HIGH = "light high";
    public static final String CMD_LIGHT_VHIGH= "light max";

    public static final String CMD_SPEECH_OFF = "speech off";
    public static final String CMD_SPEECH_VLOW = "speech min";
    public static final String CMD_SPEECH_LOW = "speech low";
    public static final String CMD_SPEECH_MEDIUM = "speech medium";
    public static final String CMD_SPEECH_HIGH = "speech high";
    public static final String CMD_SPEECH_VHIGH= "speech max";

    public static final String CMD_REMOTE_ON = "remote on";
    public static final String CMD_REMOTE_OFF = "remote off";
    public static final String CMD_ENABLE_REMOTE = "enable remote";
    public static final String CMD_DISABLE_REMOTE = "disable remote";

    public static final String CMD_SCREEN_VLOW = "brightness min";
    public static final String CMD_SCREEN_LOW = "brightness low";
    public static final String CMD_SCREEN_MEDIUM = "brightness medium";
    public static final String CMD_SCREEN_HIGH = "brightness high";
    public static final String CMD_SCREEN_VHIGH= "brightness max";

    public static final String CMD_VOICE_OFF = "stop listening";
    public static final String CMD_VOICE_ON = "start listening";

    public static final String CMD_VOLUME_OFF = "volume off";
    public static final String CMD_VOLUME_VLOW = "volume min";
    public static final String CMD_VOLUME_LOW = "volume low";
    public static final String CMD_VOLUME_MEDIUM = "volume medium";
    public static final String CMD_VOLUME_HIGH = "volume high";
    public static final String CMD_VOLUME_VHIGH= "volume max";

    public static final String CMD_WEATHER_ENGLISH = "weather english";
    public static final String CMD_WEATHER_METRIC = "weather metric";

    public static final String CMD_TIME_12HR = "time twelve hour";
    public static final String CMD_TIME_24HR = "time twenty-four hour";

    public static final String ENGLISH = "english";
    public static final String METRIC = "metric";

    public static final String MPH = "mph";
    public static final String KPH = "kph";

    private int mAppBrightness;                     // general screen brightness
    private int mLightBrightness;                   // Night light brightness

    private boolean mRemoteEnabled;                 // Enable / disable remote control connections
    private boolean mVoiceEnabled;                  // Enable / disable voice recognition UNTIL keyword spoken

    private float mSystemVolume;                    // control general system volume
    private float mMusicVolume;                     // music stream volume
    private int mMusicVolumeHolder;
    private int mSystemVolumeHolder;
    private String mTimeFormat;
    private String mWeatherUnits;                      // Weather display format (English / metric)

    private double mLatitude;
    private double mLongitude;

    private double mWorkLatitude;
    private double mWorkLongitude;

    private String mDateFormat = "EEE LLL d";      // SimpleDateFormat string for date display
    public static final String TIME_FORMAT_24_HR = "H:mm";
    public static final String TIME_FORMAT_24_HR_SHORT = "H:mm";
    public static final String TIME_FORMAT_12_HR = "h:mm";
    public static final String TIME_FORMAT_12_HR_SHORT = "h:mm";


    // Handle any messages sent from MainActivity
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            handleSettingsCommand(context, message);
        }
    };

    private void handleSettingsCommand(Context context, String command) {
        switch (command) {

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

            // Speech Volume
            case CMD_SPEECH_OFF:
                setMusicVolume(VOL_OFF);
                break;
            case CMD_SPEECH_VLOW:
                setMusicVolume(VOL_VLOW);
                break;
            case CMD_SPEECH_LOW:
                setMusicVolume(VOL_LOW);
                break;
            case CMD_SPEECH_MEDIUM:
                setMusicVolume(VOL_MEDIUM);
                break;
            case CMD_SPEECH_HIGH:
                setMusicVolume(VOL_HIGH);
                break;
            case CMD_SPEECH_VHIGH:
                setMusicVolume(VOL_VHIGH);
                break;

            // Remote
            case CMD_REMOTE_OFF:
                speakText(R.string.speech_remote_off);
                setRemoteEnabled(false);
                break;
            case CMD_REMOTE_ON:
                speakText(R.string.speech_remote_on);
                setRemoteEnabled(true);
                break;

            // screen brightness
            case CMD_SCREEN_VLOW:
                setScreenBrightness(BRIGHTNESS_VLOW);
                break;
            case CMD_SCREEN_LOW:
                setScreenBrightness(BRIGHTNESS_LOW);
                break;
            case CMD_SCREEN_MEDIUM:
                setScreenBrightness(BRIGHTNESS_MEDIUM);
                break;
            case CMD_SCREEN_HIGH:
                setScreenBrightness(BRIGHTNESS_HIGH);
                break;
            case CMD_SCREEN_VHIGH:
                setScreenBrightness(BRIGHTNESS_VHIGH);
                break;

            // Voice recognition on / off
            case CMD_VOICE_OFF:
                if (isVoiceEnabled())
                    speakText(R.string.speech_voice_off);
                else
                    speakText(R.string.speech_voice_off_err);
                setVoiceEnabled(false);
                break;

            case CMD_VOICE_ON:
                if (isVoiceEnabled())
                    speakText(R.string.speech_voice_on_err);
                else
                    speakText(R.string.speech_voice_on);
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
                speakText(R.string.speech_weather_english);
                setWeatherUnits(ENGLISH);
                break;
            case CMD_WEATHER_METRIC:
                speakText(R.string.speech_weather_metric);
                setWeatherUnits(METRIC);
                break;

            // time display
            case CMD_TIME_12HR:
                setTimeFormat12hr();
                break;
            case CMD_TIME_24HR:
                setTimeFormat24hr();
                break;
            default:
                break;
        }

    }

    private Preferences(Activity activity) {
        mActivity = activity;
        mSharedPreferences = mActivity.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // grab saved values from mSharedPreferences if they exist, if not use defaults
        mMusicVolume = mSharedPreferences.getFloat(PREFS_SPEECH_VOL, VOL_LOW);
        mSystemVolume = mSharedPreferences.getFloat(PREFS_SYSTEM_VOL, VOL_LOW);
        mAppBrightness = mSharedPreferences.getInt(PREFS_APP_BRIGHTNESS, BRIGHTNESS_MEDIUM);
        mLightBrightness = mSharedPreferences.getInt(PREFS_LIGHT_BRIGHTNESS, BRIGHTNESS_LOW);
        mWeatherUnits = mSharedPreferences.getString(PREFS_WEATHER_UNIT, ENGLISH);

        mRemoteEnabled = mSharedPreferences.getBoolean(PREFS_REMOTE_ENABLED, true);
        mVoiceEnabled = mSharedPreferences.getBoolean(PREFS_VOICE_ENABLED, true);
        mTimeFormat = mSharedPreferences.getString(PREFS_TIME_FORMAT, TIME_FORMAT_12_HR);

        // Google Account Email Preferences
        mUserAccountPref = mSharedPreferences.getString(PREFS_GMAIL, "");

        // Work address
        mWorkLatitude = mSharedPreferences.getFloat(PREFS_WORK_LAT, WORK_LAT);
        mWorkLongitude = mSharedPreferences.getFloat(PREFS_WORK_LONG, WORK_LONG);

        // set brightness and volume to stored values
        mSystemVolumeHolder = getStreamVolume(AudioManager.STREAM_SYSTEM);
        mMusicVolumeHolder = getStreamVolume(AudioManager.STREAM_MUSIC);

        setSystemVolume(mSystemVolume);
        setMusicVolume(mMusicVolume);
        setScreenBrightness(mAppBrightness);

        // Find current lat and long positions.
        // This is not currently saved to the prefs file, system will re-discover location on start
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        if (location != null) {
            try {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();

                this.onLocationChanged(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("Location", "lat:" + Double.toString(mLatitude));
        Log.d("Location", "long:" + Double.toString(mLongitude));

        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // Clean up any refs that might hang around to prevent leaks.
    public void destroy() {
        Context appContext = MainActivity.getContextForApplication();
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(mMessageReceiver);
        setVolumesToSystemValues();
        mPreferences = null;
        mSharedPreferences = null;
        mActivity = null;
    }

    // returns the instance of the Preferences class, or creates one if it does not exist
    public static Preferences getInstance(Activity activity) {
        if (mPreferences == null) {
            Log.d("Preferences", "Creating new prefs instance...");
            mPreferences = new Preferences(activity);
        } else {
            mActivity = activity;
        }
        return mPreferences;
    }

    public float getSystemVolume() {
        return mSystemVolume;
    }

    /**
     * @param vol set system volume (0-1)
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
     *
     * @param vol value to set (0-1)
     */
    public void setMusicVolume(float vol) {
        if (vol < 0 || vol > 1) return;
        mMusicVolume = vol;
        setStreamVolume(vol, AudioManager.STREAM_MUSIC);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_SPEECH_VOL, mMusicVolume);
        edit.apply();
    }

    public void setVolumesToPrefValues() {
        setStreamVolume(mSystemVolume, AudioManager.STREAM_SYSTEM);
        setStreamVolume(mMusicVolume, AudioManager.STREAM_MUSIC);
    }

    public void setVolumesToSystemValues() {
        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, mMusicVolumeHolder, 0);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, mSystemVolumeHolder, 0);
    }

    // private helper sets vol for given stream
    // Gets the max volume allowed for this stream, then sets the volume
    private void setStreamVolume(float vol, int stream) {
        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(stream);
        int setVol = (int) (vol * max);
        am.setStreamVolume(stream, setVol, 0);
    }

    private int getStreamVolume(int stream) {
        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamVolume(stream);
    }

    /**
     * Sets weather display as english or metric
     *
     * @param unit Units to display
     */
    public void setWeatherUnits(String unit) {
        if (unit.equals(ENGLISH) || unit.equals(METRIC)) {
            mWeatherUnits = unit;
            // Invalidate the WEATHER_CACHE
            CacheManager.getInstance().deleteCache(WeatherFragment.WEATHER_CACHE);
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putString(PREFS_WEATHER_UNIT, mWeatherUnits);
            edit.apply();
        }
    }

    public String getWeatherUnits() {
        return mWeatherUnits;
    }

    public boolean weatherIsEnglish() {
        return mWeatherUnits.equals(ENGLISH);
    }

    /**
     * Converts fahrenheit temps to the appropriate unit, rounded to the nearest degree
     *
     * @param temp temp in degrees F
     * @return converted temp
     */
    public int convertTemperature(double temp) {
        if (mWeatherUnits.equals(METRIC)) {
            temp = (temp - 32) * 5 / 9;
        }
        return (int) Math.round(temp);
    }

    // returns the unicode string for deg C or deg F based on the WeatherIcons font set
    public String getTempString() {
        String units;
        // Context appContext = MainActivity.getContextForApplication();
        if (mWeatherUnits.equals(ENGLISH)) {
            units = mActivity.getResources().getString(R.string.weather_deg_f);
        } else {
            units = mActivity.getResources().getString(R.string.weather_deg_c);
        }
        return units;
    }

    /**
     * Converts mph into the appropriate unit, rounded to the nearest unit per hour
     *
     * @param speed in mph
     * @return speed in converted units
     */
    public int convertWindSpeed(double speed) {
        if (mWeatherUnits.equals(METRIC)) {
            speed *= 1.609;
        }
        return (int) Math.round(speed);
    }


    /**
     * Set the string used to format date display
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

    /**
     * Format time display for clock
     *
     * @param format string for displaying time in SimpleDateFormat
     */
    public void setTimeFormat(String format) {
        mTimeFormat = format;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_TIME_FORMAT, mTimeFormat);
        edit.apply();
    }

    public String getTimeFormat() {
        return mTimeFormat;
    }

    public String getShortTimeFormat() {
        return (isTimeFormat12hr()) ? TIME_FORMAT_12_HR_SHORT : TIME_FORMAT_24_HR_SHORT;
    }

    public boolean isTimeFormat12hr() {
        return mTimeFormat.equals(TIME_FORMAT_12_HR);
    }

    public void setTimeFormat24hr() {
        setTimeFormat(TIME_FORMAT_24_HR);
    }

    public void setTimeFormat12hr() {
        setTimeFormat(TIME_FORMAT_12_HR);
    }

    /** Set brightness value used by night light
     *
     * @param brightness int (0-255)
     */
    public void setLightBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) return;

        mLightBrightness = brightness;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(PREFS_LIGHT_BRIGHTNESS, mLightBrightness);
        edit.apply();

    }

    public int getLightBrightness() {
        return mLightBrightness;
    }

    /**
     * Set brightness value for the application
     *
     * @param brightness int (0-255)
     */
    public void setScreenBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) return;

        try {
            this.mAppBrightness = brightness;
            ScreenBrightnessHelper sbh = new ScreenBrightnessHelper();
            sbh.setScreenBrightness(mActivity, mAppBrightness);

            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putInt(PREFS_APP_BRIGHTNESS, mAppBrightness);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the application's current brightness to value stored in preferences
     */
    public void resetScreenBrightness() {
        setScreenBrightness(mAppBrightness);
    }

    public int getAppBrightness() {
        return mAppBrightness;
    }

    public boolean isRemoteEnabled() {
        return mRemoteEnabled;
    }

    /**
     * Set whether the app will register on the network.
     *
     * @param isEnabled boolean
     */
    public void setRemoteEnabled(boolean isEnabled) {
        if (mRemoteEnabled == isEnabled) return;

        if (mActivity instanceof MainActivity) {
            if (isEnabled) {
                ((MainActivity) mActivity).registerNsdService();
            } else {
                ((MainActivity) mActivity).unregisterNsdService();
            }
        }
        try {
            mRemoteEnabled = isEnabled;
            if (!mRemoteEnabled) {
                ((MainActivity) mActivity).showRemoteIcon(false);
            }
            ((MainActivity) mActivity).showRemoteDisabledIcon(!mRemoteEnabled);
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
     * @param isEnabled boolean
     */
    public void setVoiceEnabled(boolean isEnabled) {
        this.mVoiceEnabled = isEnabled;
        ((MainActivity)mActivity).showSpeechIcon(isEnabled);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_VOICE_ENABLED, mVoiceEnabled);
        edit.apply();
    }


    // helper sends a string to MainActivity to be spoken
    private void speakText(int stringId) {
        String text = mActivity.getResources().getString(stringId);
        ((MainActivity)mActivity).speakText(text);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getWorkLatitude() {
        return mWorkLatitude;
    }

    public void setWorkLatitude(double mWorkLatitude) {
        this.mWorkLatitude = mWorkLatitude;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_WORK_LAT, (float) mWorkLatitude);
        edit.apply();
    }

    public double getWorkLongitude() {
        return mWorkLongitude;
    }

    public void setWorkLongitude(double mWorkLongitude) {
        this.mWorkLongitude = mWorkLongitude;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putFloat(PREFS_WORK_LONG, (float) mWorkLongitude);
        edit.apply();
    }

    // Location Listener Implementation
    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location", "Location change detected");
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("Location", "onStatusChanged:" + Integer.toString(status));
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Get User Gmail Account to be used in other fragments
    public static String getUserAccountName() {
        return mUserAccountPref;
    }

    //Set User Account if null
    public void setUserAccountName(String userAcc) {
        mUserAccountPref = userAcc;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_GMAIL, userAcc);
        edit.apply();
    }
}
