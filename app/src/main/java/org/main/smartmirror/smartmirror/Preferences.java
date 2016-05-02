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
 * <p/>
 * This is a singleton class to hold preferences for the application
 * Get the instance of it by using the getInstance() method
 * <p/>
 * Class is created at MainActivity start and loads the SharedPrefences for the application
 * Access is by getters and setters, which also handle file storage:
 */
public class Preferences implements LocationListener {

    private static Preferences mPreferences = null;
    private SharedPreferences mSharedPreferences;
    private static Activity mActivity;

    //Google Account Email Preference
    public static final String PREFS_GMAIL = "PREFS_GMAIL";
    public static final String PREFS_TOKENID = "PREFS_TOKENID";

    // constants define the names of the values to be savked to the storage file
    public static final String PREFS_NAME = "MIRROR_PREFS";
    public static final String PREFS_SPEECH_ENABLED = "MIRROR_SPEECH_ENABLED";

    public static final String PREFS_VOICE_ENABLED = "MIRROR_PREFS_VOICE_ENABLED";
    public static final String PREFS_REMOTE_ENABLED = "MIRROR_PREFS_REMOTE_ENABLED";

    public static final String PREFS_WEATHER_UNIT = "MIRROR_PREFS_WEATHER_UNIT";
    public static final String PREFS_DATE_FORMAT = "MIRROR_PREFS_DATE_FORMAT";
    public static final String PREFS_TIME_FORMAT = "MIRROR_PREFS_TIME_FORMAT";

    public static final String PREFS_FIRST_NAME = "MIRROR_PREFS_FIRST_NAME";
    public static final String PREFS_LAST_NAME = "MIRROR_PREFS_LAST_NAME";

    public static final String PREFS_FIRST_TIME_RUN = "MIRROR_FIRST_TIME_RUN";

    public static final String PREFS_WORK_LOC = "PREFS_WORK_LOC";
    public static final String PREFS_WORK_LAT = "PREFS_WORK_LAT";
    public static final String PREFS_WORK_LONG = "PREFS_WORK_LONG";

    public static final String PREFS_GOOGLE_ACCESS_TOKEN = "PREFS_GOOGLE_ACCESS_TOKEN";

    public static final String PREFS_FACEBOOK_ACCOUNT = "PREFS_FACEBOOK_ACCOUNT";
    public static final String PREFS_FACEBOOK_CREDENTIAL = "PREFS_FACEBOOK_CREDENTIAL";

    public static final String PREFS_TWITTER_ACCOUNT = "PREFS_TWITTER_ACCOUNT";

    // default for work address
    public static final float WORK_LAT = 0f;
    public static final float WORK_LONG = 0f;

    public static final String CMD_SOUND_OFF = "sound off";
    public static final String CMD_SOUND_ON = "sound on";
    public static final String CMD_MIRA_SOUND = "mira sound"; // toggle sound on / off

    public static final String CMD_REMOTE_ON = "remote on";
    public static final String CMD_REMOTE_OFF = "remote off";
    public static final String CMD_ENABLE_REMOTE = "enable remote";
    public static final String CMD_DISABLE_REMOTE = "disable remote";

    public static final String CMD_VOICE_OFF = "stop listening";
    public static final String CMD_VOICE_ON = "start listening";

    public static final String CMD_WEATHER_ENGLISH = "weather english";
    public static final String CMD_WEATHER_METRIC = "weather metric";

    public static final String CMD_TIME_12HR = "time twelve hour";
    public static final String CMD_TIME_24HR = "time twenty-four hour";

    public static final String CMD_STAY_AWAKE = "stay awake";

    public static final String ENGLISH = "english";
    public static final String METRIC = "metric";

    //Google Account Email String
    private String mUserFirstName = "";
    private String mUserLastName = "";

    private boolean mRemoteEnabled;                 // Enable / disable remote control connections
    private boolean mVoiceEnabled;                  // Enable / disable voice recognition UNTIL keyword spoken

    private boolean mSoundOn;

    private String mTimeFormat;
    private String mWeatherUnits;                   // Weather display format (English / metric)

    private boolean isWeatherVisible;
    private boolean isTimeVisible;

    private boolean mFirstTimeRun;

    private boolean mStayAwake;                     // if true, screen will stay on until cancelled
    private double mLatitude;
    private double mLongitude;

    //Google Account Email String
    private double mWorkLatitude;
    private double mWorkLongitude;
    private String mGmailAccount;
    private String mGoogleAccessToken;
    private String mFacebookAccount;
    private String mFacebookCredential;
    private String mTokenId;
    private String mTwitterAccount;
    private String mWorkLocation;

    //private String mDateFormat = "EEE LLL d";      // SimpleDateFormat string for date display
    private String mDateFormat = "EEE MMM d";
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
            handleSettingsCommand(message);
        }
    };

    private void handleSettingsCommand(String command) {
        switch (command) {

            // Voice recognition on / off
            case CMD_VOICE_OFF:
                if (isVoiceEnabled()) {
                    speakText(R.string.speech_voice_off);
                    setVoiceEnabled(false);
                } else {
                    speakText(R.string.speech_voice_off_err);
                }
                break;

            case CMD_VOICE_ON:
                if (isVoiceEnabled()) {
                    speakText(R.string.speech_voice_on_err);
                } else {
                    speakText(R.string.speech_voice_on);
                    setVoiceEnabled(true);
                }
                break;

            // Speech on / off
            case CMD_SOUND_OFF:
                if (isSoundOn()) {
                    forceSpeakText(R.string.sound_off);
                    setSoundOn(false);
                } else {
                    forceSpeakText(R.string.sound_off_err);
                }
                break;
            case CMD_SOUND_ON:
                if (isSoundOn()) {
                    forceSpeakText(R.string.sound_on_err);
                } else {
                    setSoundOn(true);
                    forceSpeakText(R.string.sound_on);
                }
                break;
            case CMD_MIRA_SOUND:
                if (isSoundOn()) {
                    forceSpeakText(R.string.sound_off);
                    setSoundOn(false);
                } else {
                    forceSpeakText(R.string.sound_on);
                    setSoundOn(true);
                }
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

            case Constants.MIRA_LISTEN:
                if (isVoiceEnabled()) {
                    speakText(R.string.speech_voice_off);
                    setVoiceEnabled(false);
                } else {
                    speakText(R.string.speech_voice_on);
                    setVoiceEnabled(true);
                }
                break;

            // weather units
            case CMD_WEATHER_ENGLISH:
                if (weatherIsEnglish()) {
                    speakText(R.string.speech_weather_english_err);
                } else {
                    speakText(R.string.speech_weather_english);
                    setWeatherUnits(ENGLISH);
                }
                break;
            case CMD_WEATHER_METRIC:
                if (!weatherIsEnglish()) {
                    speakText(R.string.speech_weather_metric_err);
                } else {
                    speakText(R.string.speech_weather_metric);
                    setWeatherUnits(METRIC);
                }
                break;

            // time display
            case CMD_TIME_12HR:
                setTimeFormat12hr();
                break;
            case CMD_TIME_24HR:
                setTimeFormat24hr();
                break;

            /** CMD_STAY_AWAKE is a toggle */
            case CMD_STAY_AWAKE:
                if (mStayAwake) {
                    speakText(R.string.speech_stay_awake_cancel);
                    setStayAwake(false);
                } else {
                    speakText(R.string.speech_stay_awake);
                    setStayAwake(true);
                }
                break;

            default:
                break;
        }

    }

    private Preferences(Activity activity) {
        mActivity = activity;
        mSharedPreferences = mActivity.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mSoundOn = mSharedPreferences.getBoolean(PREFS_SPEECH_ENABLED, true);

        // grab saved values from mSharedPreferences if they exist, if not use defaults
        mWeatherUnits = mSharedPreferences.getString(PREFS_WEATHER_UNIT, ENGLISH);

        mRemoteEnabled = mSharedPreferences.getBoolean(PREFS_REMOTE_ENABLED, true);
        mVoiceEnabled = mSharedPreferences.getBoolean(PREFS_VOICE_ENABLED, true);
        mTimeFormat = mSharedPreferences.getString(PREFS_TIME_FORMAT, TIME_FORMAT_12_HR);

        // Google Account Email Preferences
        mGmailAccount = mSharedPreferences.getString(PREFS_GMAIL, "");
        mGoogleAccessToken = mSharedPreferences.getString(PREFS_GOOGLE_ACCESS_TOKEN, "");
        mFirstTimeRun = mSharedPreferences.getBoolean(PREFS_FIRST_TIME_RUN, true);
        mTokenId = mSharedPreferences.getString(PREFS_TOKENID, "");

        // Work address
        mWorkLocation = mSharedPreferences.getString(PREFS_WORK_LOC, "");
        mWorkLatitude = mSharedPreferences.getFloat(PREFS_WORK_LAT, WORK_LAT);
        mWorkLongitude = mSharedPreferences.getFloat(PREFS_WORK_LONG, WORK_LONG);

        mFacebookAccount = mSharedPreferences.getString(PREFS_FACEBOOK_ACCOUNT, "");
        mFacebookCredential = mSharedPreferences.getString(PREFS_FACEBOOK_CREDENTIAL, "");

        mTwitterAccount = mSharedPreferences.getString(PREFS_TWITTER_ACCOUNT, "");

        // User name
        mUserFirstName = mSharedPreferences.getString(PREFS_FIRST_NAME, "Defaultfirstname");
        mUserLastName = mSharedPreferences.getString(PREFS_LAST_NAME, "Defaultlastname");

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

        isWeatherVisible = true;
        isTimeVisible = true;

        Log.d("Location", "lat:" + Double.toString(mLatitude));
        Log.d("Location", "long:" + Double.toString(mLongitude));

        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // Clean up any refs that might hang around to prevent leaks.
    public void destroy() {
        Context appContext = MainActivity.getContextForApplication();
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(mMessageReceiver);
        mPreferences = null;
        mSharedPreferences = null;
        mActivity = null;
    }

    /**
     * Returns the instance of the Preferences class, or creates one if it does not exist.
     * On start, this will first be called by AccountActivity during the setup phase.
     * It will then be called by MainActivity, which is essential due to the close binding between
     * methods in this class and MainActivity.
     *
     * @param activity creating activity
     * @return singleton of the Preferences class
     */
    public static Preferences getInstance(Activity activity) {
        if (mPreferences == null) {
            Log.d("Preferences", "Creating new prefs instance...");
            mPreferences = new Preferences(activity);
        }
        return mPreferences;
    }

    /**
     * Sets weather display as english or metric
     *
     * @param unit Units to display
     */
    public void setWeatherUnits(String unit) {
        if (mWeatherUnits.equals(unit)) return;

        if (unit.equals(ENGLISH) || unit.equals(METRIC)) {
            mWeatherUnits = unit;

            CacheManager.getInstance().invalidateCache(WeatherFragment.WEATHER_CACHE);
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

    public boolean isWeatherVisible() {
        return isWeatherVisible;
    }

    public void setIsWeatherVisible(boolean isWeatherVisible) {
        this.isWeatherVisible = isWeatherVisible;
    }

    public boolean isTimeVisible() {
        return isTimeVisible;
    }

    public void setIsTimeVisible(boolean isTimeVisible) {
        this.isTimeVisible = isTimeVisible;
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

    public boolean isRemoteEnabled() {
        return mRemoteEnabled;
    }

    /**
     * Set enable / disabled status for remote control.
     * Disabling will unregister the service and display the remote disabled icon.
     * Enabling registers the service on the network and hides remote disabled icon.
     *
     * @param enable enable or disable the remote control
     */
    public void setRemoteEnabled(boolean enable) {
        if (mRemoteEnabled == enable) return;
        mRemoteEnabled = enable;

        if (mActivity instanceof MainActivity) {
            if (enable) {
                ((MainActivity) mActivity).registerNsdService();
            } else {
                ((MainActivity) mActivity).unregisterNsdService();
                ((MainActivity) mActivity).disconnectRemote();
            }
        }

        try {
            if (!mRemoteEnabled) {
                // when disabling, hide remote connected icon
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
     *
     * @param isEnabled boolean
     */
    public void setVoiceEnabled(boolean isEnabled) {
        Log.i(Constants.TAG, "setVoiceEnabled :: " + isEnabled);
        if (isEnabled == mVoiceEnabled) return;
        this.mVoiceEnabled = isEnabled;
        ((MainActivity) mActivity).showSpeechIcon(isEnabled);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_VOICE_ENABLED, mVoiceEnabled);
        edit.apply();
    }

    public void setSoundOn(boolean enable) {
        if (enable == mSoundOn) return;
        this.mSoundOn = enable;

        float vol = 0f;
        if (enable) {
            vol = .5f;

        }

        AudioManager audio = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVol = (int) (maxVolume * vol);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
        ((MainActivity)mActivity).showSoundOffIcon(!enable);

        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_SPEECH_ENABLED, mSoundOn);
        edit.apply();
    }

    public boolean isSoundOn() {
        return mSoundOn;
    }

    // helper sends a string to MainActivity to be spoken
    private void speakText(int stringId) {
        String text = mActivity.getResources().getString(stringId);
        ((MainActivity) mActivity).speakText(text);
    }

    private void forceSpeakText(int stringId) {
        String text = mActivity.getResources().getString(stringId);
        ((MainActivity) mActivity).forceSpeakText(text);
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

    public void setWorkLocation(String location) {
        Log.i(Constants.TAG, "settings location :: " + location);
        mWorkLocation = location;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_WORK_LOC, mWorkLocation);
        edit.apply();
    }

    public String getWorkLocation() {
        return mWorkLocation;
    }

    public boolean isWorkAddressSet() {
        return (!mWorkLocation.isEmpty());
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

    public void setFirstTimeRun(boolean mFirstTimeRun) {
        this.mFirstTimeRun = mFirstTimeRun;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(PREFS_FIRST_TIME_RUN, mFirstTimeRun);
        edit.commit();
    }

    public boolean isFirstTimeRun() {
        return mFirstTimeRun;
    }

    //Get User Gmail Account to be used in other fragments
    public String getGmailAccount() {
        return mGmailAccount;
    }

    //Set User Account if null
    public void setGmailAccount(String userAcc) {
        this.mGmailAccount = userAcc;
        Log.i(Constants.TAG, "setting email :: " + userAcc);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_GMAIL, userAcc);
        edit.apply();
    }

    public String getUsername() {
        if (!mGmailAccount.isEmpty())
            return mGmailAccount.substring(0, mGmailAccount.indexOf('@'));
        else
            return "";
    }

    public String getUserFirstName() {
        return mUserFirstName;
    }

    public void setUserFirstName(String mUserFirstName) {
        this.mUserFirstName = mUserFirstName;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_FIRST_NAME, mUserFirstName);
        edit.apply();
    }

    public String getUserLastName() {
        return mUserLastName;
    }

    public void setUserLastName(String mUserLastName) {
        this.mUserLastName = mUserLastName;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_LAST_NAME, mUserLastName);
        edit.apply();
    }

    public boolean isLoggedInToGmail() {
        if (mGmailAccount.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void setUserId(String mTokenId) {
        this.mTokenId = mTokenId;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_TOKENID, mTokenId);
        edit.apply();
    }

    public String getUserId() {
        return mTokenId;
    }

    public void setAccessToken(String mGoogleAccessToken) {
        this.mGoogleAccessToken = mGoogleAccessToken;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_GOOGLE_ACCESS_TOKEN, mGoogleAccessToken);
        edit.apply();
    }

    public String getAccessToken() {
        return mGoogleAccessToken;
    }

    public void setStayAwake(boolean stayAwake) {
        if (mStayAwake == stayAwake) return;

        mStayAwake = stayAwake;
        ((MainActivity) mActivity).showStayAwakeIcon(mStayAwake);
    }

    public boolean isStayingAwake() {
        return mStayAwake;
    }

    public void setFacebookCredential(String mFacebookCredentials) {
        this.mFacebookCredential = mFacebookCredentials;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_FACEBOOK_CREDENTIAL, mFacebookCredentials);
        edit.apply();
    }

    public String getFacebookCredential(){
        return mFacebookCredential;
    }


    public void setFacebookAccount(String mFacebookAccount) {
        this.mFacebookAccount = mFacebookAccount;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_FACEBOOK_ACCOUNT, mFacebookAccount);
        edit.apply();
    }

    public String getFacebookAccount() {
        return mFacebookAccount;
    }

    public boolean isLoggedInToFacebook() {
        if (mFacebookCredential.isEmpty())
            return false;
        else
            return true;
    }

    public void setTwitterAccount(String mTwitterAccount) {
        this.mTwitterAccount = mTwitterAccount;
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(PREFS_TWITTER_ACCOUNT, mTwitterAccount);
        edit.apply();
    }

    public String getTwitterAccount() {
        return mTwitterAccount;
    }
}
