package org.main.smartmirror.smartmirror;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener, SensorEventListener {

    // Globals, prefs, debug flags
    public static final boolean DEBUG = true;

    private static Context mContext;
    private Preferences mPreferences;

    public static final int ASLEEP = 0;
    public static final int LIGHT_SLEEP = 1;
    public static final int AWAKE = 2;

    // Help
    private HelpFragment mHelpFragment;

    // Light Sensor
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private long mLightSensorStartTime;
    private final long LIGHT_WAKE_DELAY = 4000;   // time delay before screen will wake due to light changes
    private RecentLightValues mRecentLightValues;

    // Sleep state & wakelocks
    // mirrorSleepState can be ASLEEP, LIGHT_SLEEP or AWAKE
    private int mirrorSleepState;
    private int defaultScreenTimeout;
    private String mInitialFragment = Constants.CALENDAR;
    private String mCurrentFragment;
    private final int WAKELOCK_TIMEOUT = 100;            // Wakelock should be held only briefly to trigger screen wake
    private PowerManager.WakeLock mWakeLock;
    private Timer mUITimer;
    private final long UI_TIMEOUT_DELAY = 1000 * 60 * 5; // User interactions reset screen on timer to 5 minutes
    private final int SCREEN_OFF_TIMEOUT = 5000;         // Timeout for lightSleep -> sleep transition
    private PowerManager mPowerManager;

    // WiFiP2p
    private WifiP2pManager mWifiManager;
    private WifiP2pManager.Channel mWifiChannel;
    private WifiP2pDeviceList mWifiDeviceList;
    private WifiP2pInfo mWifiInfo;
    private BroadcastReceiver mWifiReceiver;
    private IntentFilter mWifiIntentFilter;
    private RemoteServerAsyncTask mServerTask;
    public final static int PORT = 8888;
    public final static int SOCKET_TIMEOUT = 500;
    private ScheduledFuture<?> wifiHeartbeat;

    // TTS
    private TTSHelper mTTSHelper;

    // Speech recognition
    private Messenger mMessenger = new Messenger(new IHandler());
    private boolean mIsBound;
    private Messenger mService;
    private ImageView mSpeechIcon;

    // Sound effects
    private MediaPlayer mFXPlayer;

    private Runnable uiTimerRunnable = new Runnable() {
        @Override
        public void run() {
            clearScreenOnFlag();
        }
    };

    // used to establish a service connection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            initSpeechRecognition();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    // handles the messages from VoiceService to this Activity
    public class IHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case VoiceService.RESULT_SPEECH:
                    String result = msg.getData().getString("result");
                    handleVoiceCommand(result);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        // Load any application preferences. If prefs do not exist, set them to defaults
        mPreferences = Preferences.getInstance(this);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        checkMarshmallowPermissions();
        initializeWifiP2P();
        discoverWifiP2pPeers();
        mWifiReceiver = new WiFiDirectBroadcastReceiver(mWifiManager, mWifiChannel, this);
        initializeLightSensor();

        // initialize TextToSpeech (TTS)
        mTTSHelper = new TTSHelper(this);

        try {
            defaultScreenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Set up ScreenReceiver to hold screen on / off status
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        // Set up views and nav drawer
        setContentView(R.layout.activity_main);
        // speech icon turn it off for now
        mSpeechIcon = (ImageView)findViewById(R.id.speech_icon);
        mSpeechIcon.setVisibility(View.INVISIBLE);
        if (mPreferences.isVoiceEnabled()) {
            mSpeechIcon.setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Hide UI and actionbar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
                //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION    // commented out to keep nav buttons for testing
                //| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // req API 19
                //| View.SYSTEM_UI_FLAG_IMMERSIVE;      // req API 19
        decorView.setSystemUiVisibility(uiOptions);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void checkMarshmallowPermissions() {
        // check for permission to write system settings on API 23 and greater.
        // Leaving this in case we need the WRITE_SETTINGS permission later on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite( getApplicationContext() )) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, 1);
            }
        }
    }

    public static Context getContextForApplication() {
        return mContext;
    }

    // -------------------------  LIFECYCLE CALLBACKS ----------------------------

    /**
     * If this is called by the device waking up, it will trigger a new call to handleCommand(),
     * reloading the last visible fragment saved in mCurrentFragment
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(Constants.TAG, "onStart");
        mirrorSleepState = AWAKE;

        mIsBound = bindService(new Intent(this, VoiceService.class), mConnection, BIND_AUTO_CREATE);
        addScreenOnFlag();
        startUITimer();

        if (mPowerManager.isScreenOn()) {
            mPreferences.resetScreenBrightness();
        }

        mPreferences.setVolumesToPrefValues();
        stopWifiHeartbeat();
        stopLightSensor();
        startSpeechRecognition();
        registerReceiver(mWifiReceiver, mWifiIntentFilter);

        // on first load show initialFragment
        if (mCurrentFragment == null)  {
            wakeScreenAndDisplay(mInitialFragment);
        }
        // if the system was put to sleep from LIGHT_SLEEP, pop SleepFragment off
        else if ( mCurrentFragment.equals(Constants.LIGHT_SLEEP) ) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(Constants.TAG, "onResume");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPause(){
        super.onPause();
        Log.i(Constants.TAG, "onPause");
        // If the screen is not turning off, the app is going into the background: speech recognition is stopped.
        // This is (mostly) for debugging purposes as the finished program should always be in foreground.
        if (mPowerManager.isScreenOn()) {
            stopSpeechRecognition();
            mPreferences.setVolumesToSystemValues();
            setDefaultScreenOffTimeout();
        } else {
            // Otherwise the screen is turning off: start Light Sensor and maintain Wifi connection
            startWifiHeartbeat();
            startLightSensor();
        }
        stopUITimer();
        unregisterReceiver(mWifiReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(Constants.TAG, "onStop");
        mirrorSleepState = ASLEEP;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTSHelper.destroy();
        mMessenger = null;
        mPreferences.destroy();
        if (wifiHeartbeat != null) {
            wifiHeartbeat.cancel(true);
            wifiHeartbeat = null;
        }
        unbindService(mConnection);
        mIsBound = false;
        Log.i(Constants.TAG, "onDestroy");
    }

    // ------------------------- Broadcasts --------------------------

    /**
     * Broadcast a message on intentName. This is used to send any command not related to starting
     * a fragment to all listeners. The listeners can then take action if required by the command.
     * @param intentName intent name
     * @param msg String message to send
     */
    private void broadcastMessage(String intentName, String msg) {
        Intent intent = new Intent(intentName);
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // -------------------------- SCREEN WAKE / SLEEP ---------------------------------

    /**
     * Calling forces device to bypass keyguard if enabled.
     * It will not override password, pattern or biometric
     * locks if enabled from the system settings. Acquiring the wakelock in this method will trigger
     * the application to move from the onStop to onRestart.
     */
    @SuppressWarnings("deprecation")
    protected void exitSleep() {
        Log.i(Constants.TAG, "exitSleep() called");
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        mWakeLock.acquire(WAKELOCK_TIMEOUT);
    }

    protected void exitLightSleep(){
        setDefaultScreenOffTimeout();
        addScreenOnFlag();
        startUITimer();
        stopLightSensor();
        mirrorSleepState = AWAKE;
    }

    protected void enterLightSleep() {
        clearScreenOnFlag();
        setScreenOffTimeout();
        stopUITimer();
        startLightSensor();
        mirrorSleepState = LIGHT_SLEEP;
    }

	// Restores the screen off to the duration set when the application first ran.
    protected void setDefaultScreenOffTimeout() {
        // sanity check to prevent screen lockout from super-short screen timeout settings.
        if (defaultScreenTimeout < 1000) defaultScreenTimeout = 10000;
        Log.i(Constants.TAG, "setting screen timeout: " + defaultScreenTimeout + " ms");
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defaultScreenTimeout);
    }

    protected void setScreenOffTimeout() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT);
    }

    // Flags the system to keep the screen on indefinitely.
    protected void addScreenOnFlag(){
        Log.i(Constants.TAG, "Adding FLAG_KEEP_SCREEN_ON" );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Removes the KEEP_SCREEN_ON flag, allowing the screen to timeout normally.
    protected void clearScreenOnFlag() {
        Log.i(Constants.TAG, "clearing FLAG_KEEP_SCREEN_ON" );
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Start a timer to track interval between user interactions.
    // When expired, clear the screen on flag so the screen can time out per system settings.
    protected void startUITimer() {
        stopUITimer();
        mUITimer = new Timer();
        Log.i(Constants.TAG, "UI timer start. " + UI_TIMEOUT_DELAY + " ms" );
        mUITimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(Constants.TAG, "UI timeout reached at " + UI_TIMEOUT_DELAY + " ms");
                MainActivity.this.runOnUiThread(uiTimerRunnable);
            }
        }, UI_TIMEOUT_DELAY);
    }

    protected void stopUITimer(){
        if (mUITimer != null) {
            Log.i(Constants.TAG, "UI timer cancelled");
            mUITimer.cancel();
        }
    }

    // -------------------------- DRAWER AND INTERFACE ---------------------------------

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            handleCommand(item.toString().toLowerCase(Locale.US));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(DEBUG)
            Log.i(Constants.TAG, "NavigationItemSelected: " + item.toString());
        handleCommand(item.toString().toLowerCase(Locale.US));
        return true;
    }

    private void displayFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        if (!isFinishing()) {
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Log.e(Constants.TAG, "commit skipped. isFinishing() returned true");
        }
    }

    /**
     * Show a toast
     * @param text text to display
     * @param duration int duration: ex. Toast.LENGTH_LONG
     */
    public void showToast(String text, int duration) {
        Toast.makeText(this, text, duration).show();
    }

    /**
     * Entry point for processing user commands.
     * If sleeping, this will ignore commands except those which cause a state transition to "awake".
     * If command would wake the application, trigger proper state change and handle the command.
     * @param command input command
     */
    public void wakeScreenAndDisplay(String command) {
        if (mirrorSleepState == AWAKE) {
            startUITimer();
            hideHelpFragment(command);
        } else if (commandWakesFromSleep(command)) {
            if (mirrorSleepState == ASLEEP ) {
                exitSleep();
            } else {
				// change from LIGHT_SLEEP -> AWAKE. LIGHT_SLEEP only lasts ~10 seconds,
                // so these cases are not common.
                exitLightSleep();
                if (command.equals(Constants.LIGHT)) {
                    hideHelpFragment(command);
                } else {
                    // in LIGHT_SLEEP we're showing a black, empty fragment. Instead, display the last
                    // fragment shown before SleepFragment.
                    getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    public boolean commandWakesFromSleep(String command) {
        return (command.equals(Constants.WAKE) || command.equals(Constants.LIGHT));
    }

    /**
     * "help" displays the helpFragment. All other commands dismiss it.
     * @param command
     */
    public void hideHelpFragment(String command) {

        if (command.equals(Constants.HELP) && mHelpFragment == null) {
            mHelpFragment = HelpFragment.newInstance(getCurrentFragment());
            mHelpFragment.show(getFragmentManager(), "HelpFragment");
        }

        if (mHelpFragment != null) {
            mHelpFragment.dismiss();
            mHelpFragment = null;
        }

        closeMenuDrawer(command);
    }

    /**
     * Close the MenuDrawer if it is open. Open it on "Drawer" command
     * @param command command to be executed
     */
    public void closeMenuDrawer(String command) {
        // 'menu' command toggles menu drawer open / close. Other commands will close the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (command.equals(Constants.MENU) && !drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
            return;
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        String mGuardSection;
        String[] urlArr = getResources().getStringArray(R.array.guardian_sections);
        int i = 0;
        try {
            while (i < urlArr.length) {
                if (command.contains(urlArr[i])) {
                    mGuardSection = urlArr[i];
                    Bundle bundle = new Bundle();
                    bundle.putString("arrI", mGuardSection);
                    fragment = new NewsFragment();
                    fragment.setArguments(bundle);
                    break;
                } else {
                    i++;
                    Log.i("I heard: ", command);
                }
            }
        }catch (Exception e) {}



        // Create fragment
        handleCommand(command);
    }


    /**
     * Show a fragment or broadcast a command to listeners.
     * Do not call this method directly, instead use wakeScreenAndDisplay, which will make sure
     * the application is in the appropriate sleep state.
     * @param command command to process
     */
    private void handleCommand(String command){
        Fragment fragment = null;

        if (DEBUG) {
            Log.i(Constants.TAG, "handleCommand() status:" + mirrorSleepState + " command:\"" + command + "\"");
        }

        // Create fragment based on the command. If the input string is not a fragment,
        // broadcast the command to all registered receivers for evaluation.
        switch (command) {
            case Constants.CALENDAR:
                fragment = new CalendarFragment();
                break;
            case Constants.CAMERA:
                // TODO: can we handle this disabling within the CameraFragment instead?
                if(mPreferences.isCameraEnabled()) {
                    fragment = new CameraFragment();
                }
                else {
                    showToast(getResources().getString(R.string.camera_disabled_toast), Toast.LENGTH_LONG);
                }
                break;
            case Constants.FACEBOOK:
                fragment = new FacebookFragment();
                break;
            case Constants.GALLERY:
                fragment = new GalleryFragment();
                break;
            case Constants.GO_BACK:
                getSupportFragmentManager().popBackStack();
                break;
            case Constants.NEWS:
                NewsFragment.mGuardURL = NewsFragment.mDefaultGuardURL;
                Bundle bundle = new Bundle();
                bundle.putString("arrI", "world");
                fragment = new NewsFragment();
                fragment.setArguments(bundle);
                break;
            case Constants.NEWS_BODY:
                fragment = new NewsBodyFragment();
                break;
            case Constants.LIGHT:
                fragment = new LightFragment();
                break;
            case Constants.QUOTES:
                fragment = new QuoteFragment();
                break;
            case Constants.SETTINGS:
            case Constants.OPTIONS:
                fragment = new SettingsFragment();
                break;
            case Constants.SLEEP:
                fragment = new LightSleepFragment();
                enterLightSleep();
                command = Constants.LIGHT_SLEEP;
                break;
            case Constants.TWITTER:
                fragment = new TwitterFragment();
                break;
            case Constants.WAKE:
                break;
            case Constants.MAKEUP:
                fragment = new MakeupFragment();
                break;
            default:
                broadcastMessage("inputAction", command);
                break;
        }

        if(fragment != null){
            playSound(R.raw.celeste_a);
            //startTTS(command);
            mCurrentFragment = command;
            displayFragment(fragment);
        }
    }

    /**
     * Gets the fragment currently being viewed. If the mirror in SLEEP,
     * this will return the value of the last-displayed fragment.
     * @return String fragment name
     */
    protected String getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     * This method handles the speech icon indicator. It either hides
     * or shows the icon based on the flag
     * @param flag whether or not to show the icon
     */
    public void showSpeechIcon(boolean flag){
        if(flag) {
            mSpeechIcon.setVisibility(View.VISIBLE);
        } else if (mSpeechIcon.getVisibility() == View.VISIBLE) {
            mSpeechIcon.setVisibility(View.INVISIBLE);
        }
    }

    // ----------------------- SPEECH RECOGNITION --------------------------

    /**
     * Handles the result of the speech input. Conform voice inputs into standard commands
     * used by the remote.
     * @param input the command the user gave
     */
    public void handleVoiceCommand(String input) {
        String voiceInput = input.trim();
        Log.i(Constants.TAG, "handleVoiceCommand:\""+input+"\"");

        if (mPowerManager.isScreenOn() ) {
            showToast(input, Toast.LENGTH_LONG);
        }

        // if voice is disabled, ignore everything except "start listening" command
        if (!mPreferences.isVoiceEnabled()) {
            if (voiceInput.equals(Preferences.CMD_VOICE_ON) ) {
                broadcastMessage("inputAction", voiceInput);
            }
            return;
        }

        if(voiceInput.contains(Constants.WAKE)) {
            voiceInput = Constants.WAKE;
        }

        // time
        if(voiceInput.contains(Constants.SHOW_TIME)) {
            voiceInput = Constants.SHOW_TIME;
        } else if (voiceInput.contains(Constants.HIDE_TIME)) {
            voiceInput = Constants.HIDE_TIME;
        } else if (voiceInput.contains(Constants.TIME)) {
            voiceInput = Constants.TIME;
        }

        // weather
        if(voiceInput.contains(Constants.HIDE_WEATHER)) {
            voiceInput = Constants.HIDE_WEATHER;
        } else if (voiceInput.contains(Constants.SHOW_WEATHER)) {
            voiceInput = Constants.SHOW_WEATHER;
        } else if (voiceInput.contains(Preferences.CMD_WEATHER_ENGLISH)) {
            voiceInput = Preferences.CMD_WEATHER_ENGLISH;
        } else if (voiceInput.contains(Preferences.CMD_WEATHER_METRIC)) {
            voiceInput = Preferences.CMD_WEATHER_METRIC;
        } else if (voiceInput.contains(Constants.WEATHER)) {
            voiceInput = Constants.WEATHER;
        }


        if(voiceInput.contains(Constants.NIGHT_LIGHT)) {
            voiceInput = Constants.LIGHT;
        }

        if(voiceInput.contains(Constants.SLEEP)) {
            voiceInput = Constants.SLEEP;
        }

        // Junk fix for remote
        if(voiceInput.contains(Constants.REMOTE)) {
            if (voiceInput.contains("enable")) {
                voiceInput = Preferences.CMD_REMOTE_ON;
            } else if (voiceInput.contains("disable")) {
                voiceInput = Preferences.CMD_REMOTE_OFF;
            }
        }
        // more garbage...
        if(voiceInput.contains(Constants.CAMERA)) {
            if (voiceInput.contains("enable")) {
                voiceInput = Preferences.CMD_CAMERA_ON;
            } else if (voiceInput.contains("disable")) {
                voiceInput = Preferences.CMD_CAMERA_OFF;
            }
        }

        // Normalize speech commands to match remote control versions.
        switch (voiceInput) {
            case Constants.GO_TO_SLEEP:
                voiceInput = Constants.SLEEP;
                break;
            case Constants.HIDE:
                break;
            case Constants.OPTIONS:
                voiceInput = Constants.SETTINGS;
                break;
            case Constants.WAKE_UP:
                voiceInput = Constants.WAKE;
                break;
        }
        wakeScreenAndDisplay(voiceInput);
    }

    public void initSpeechRecognition() {
        try {
            //Log.i(Constants.TAG, "initSpeechRecognition()");
            Message msg = Message.obtain(null, VoiceService.INIT_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the speech recognizer
     */
    public void startSpeechRecognition(){
        Log.i(Constants.TAG, "startSpeechRecognition()");
        if(mTTSHelper.isSpeaking() || mService == null) return;
        try {
            //Log.i("VR", "startSpeechRecognition()");
            Message msg = Message.obtain(null, VoiceService.START_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
            showSpeechIcon(mPreferences.isVoiceEnabled());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the current speech recognition object
     */
    public void stopSpeechRecognition(){
        Log.i(Constants.TAG, "stopSpeechRecognition()");
        if (mService == null) return;
        try {
            Message msg = Message.obtain(null, VoiceService.STOP_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
            showSpeechIcon(false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------- Sound Effects Playback -----------------------------

    /**
     * Plays the sound resource located in /res/raw
     * @param _id sound resource to play
     */
    public void playSound(int _id)
    {
        if(mFXPlayer != null)
        {
            mFXPlayer.reset();
            mFXPlayer.release();
        }
        mFXPlayer = MediaPlayer.create(this, _id);
        if(mFXPlayer != null) {
            mFXPlayer.start();
        }
    }

    // --------------------------------- Text to Speech (TTS) ---------------------------------


    /**
     * Say a phrase using text to speech
     * @param phrase to speak
     */
    public void startTTS(final String phrase){
        Thread mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTTSHelper.speakText(phrase);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSpeechThread.start();
    }

    /**
     * Stop Text to Speech
     */
    public void stopTTS() {
        if (mTTSHelper != null) { mTTSHelper.stop(); }
    }

    public boolean isTTSSpeaking() {
        return ( mTTSHelper != null && mTTSHelper.isSpeaking() );
    }

    // ------------------------------  WIFI P2P  ----------------------------------

    /**
     * Create
     */
    private void initializeWifiP2P() {
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mWifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiChannel = mWifiManager.initialize(this, getMainLooper(), null);
    }

    /**
     * Callback from RemoteServerAsyncTask when a command is received from the remote control.
     * @param command String: received command
     */
    public void handleRemoteCommand(String command) {
        if (mPreferences.isRemoteEnabled())
            wakeScreenAndDisplay(command);
        else {
            Log.i(Constants.TAG, "Remote Disabled. Command ignored: \"" + command + "\"");
        }

    }

    // calls the P2pManager to refresh peer list
    public void discoverWifiP2pPeers() {
        mWifiManager.discoverPeers(mWifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (DEBUG)
                    Log.i(Constants.TAG, "Peer discovery successful");
            }

            @Override
            public void onFailure(int reasonCode) {
                if (DEBUG)
                    Log.i(Constants.TAG, "discoverWifiP2pPeers failed: " + reasonCode);
            }
        });
    }

    // Interface passes back a device list when the peer list changes, or discovery is successful
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        mWifiDeviceList = peers;
    }


    /** called when a connection is made to this device
     *
     * @param info response info
     */
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // make this the group owner and start the server to listen for commands
        if(DEBUG)
            Log.i(Constants.TAG, "Connection info: " + info.toString());
        mWifiInfo = info;
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 15;
        if (info.groupFormed && info.isGroupOwner) {
            if(DEBUG)
                Log.i(Constants.TAG, "onConnectionInfo is starting server...");
            startRemoteServer();
        } else if (info.groupFormed){
            Log.i(Constants.TAG, "group exists, mirror is not owner");
        }
    }

    /**
     * Enables or disables WifiP2P connections to the mirror. This should not be called directly,
     * but through Preferences.setRemoteEnabled()
     * @param isEnabled service state: enabled or disabled
     */
    public void setRemoteStatus(boolean isEnabled) {
        if (isEnabled) {
            // if there's a connection established, ignore
            // otherwise, start peer discovery.
            discoverWifiP2pPeers();
        } else {
            // if a connection exists, cancel and refuse further
            mServerTask.cancel(true);
        }
    }

    // Start a server socket: this will listen to commands from the remote control
    public void startRemoteServer() {
        mServerTask = new RemoteServerAsyncTask(this);
        mServerTask.execute();
    }

    // Start a thread that keeps the wifip2p connection alive by performing network discovery.
    public void startWifiHeartbeat() {
        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(1);

        final Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                discoverWifiP2pPeers();
                Log.i(Constants.TAG, "Heartbeat: discoverWifiP2pPeers()" );
            }
        };
        wifiHeartbeat = scheduler.scheduleAtFixedRate(heartbeatTask, 360, 360,
                TimeUnit.SECONDS);
    }

    // Stop the heartbeat thread
    public void stopWifiHeartbeat() {
        if (wifiHeartbeat != null) {
            wifiHeartbeat.cancel(true);
        }
    }

    // --------------------------- LIGHT SENSOR --------------------------------------

    private void initializeLightSensor(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void startLightSensor() {
        try {
            mRecentLightValues = new RecentLightValues();
            mLightSensorStartTime = System.currentTimeMillis();
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception e) {
            Log.e(Constants.TAG, "startLightSensor exception. LightSensor may not be initialized");
        }
    }

    public void stopLightSensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do something here if sensor accuracy changes
    }

    /**
     * Light sensor tracks the last 20 light values. After an initial delay set by LIGHT_WAKE_DELAY,
     * if it detects a sudden increase over the running average, a wake command is sent to the device.
     * @param event light event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float currentLight = event.values[0];
        mRecentLightValues.addValue(currentLight);
        float recentLightAvg = mRecentLightValues.getAverage();

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            //Log.i(Constants.TAG, "Light sensor value:" + Float.toString(currentLight) );
            //Log.i(Constants.TAG, "recent light avg: " + recentLightAvg);
            if ( currentLight > recentLightAvg * 3 && lightWakeDelayExceeded() ){
                // Stop any further callbacks from the sensor.
                stopLightSensor();
                wakeScreenAndDisplay(Constants.WAKE);
            }
        }
    }

    private boolean lightWakeDelayExceeded() {
        return (System.currentTimeMillis() - mLightSensorStartTime > LIGHT_WAKE_DELAY);
    }

    // holds values reported by the light sensor and reports their average
    private class RecentLightValues {
        int index = 0;
        int size = 20;
        float[] recentValues = new float[size];

        void addValue(float val) {
            recentValues[index] = val;
            index = (++index) % size;
        }

        float getAverage(){
            float sum = 0;
            for(float v : recentValues) {
                sum += v;
            }
            return sum / recentValues.length;
        }
    }
}