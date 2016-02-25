package org.main.smartmirror.smartmirror;

import android.app.KeyguardManager;
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
import android.net.nsd.NsdServiceInfo;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener,NewsFragment.ArticleSelectedListener {

    // Globals, prefs, debug flags
    public static final boolean DEBUG = true;
    private static Context mContext;
    private Preferences mPreferences;

    // Mira
    private Mira mira;

    // Content Frames
    private ViewGroup contentFrame1;
    private ViewGroup contentFrame2;
    private ViewGroup contentFrame3;

    private ImageView imgSpeechIcon;
    private ImageView imgRemoteIcon;
    private ImageView imgRemoteDisabledIcon;

    // Set initial fragments & track displayed views
    private String mInitialFragment = Constants.NEWS;
    private String mCurrentFragment;

    // FrameSize maintains the size of the content window between state changes
    private int frame1Visibility = View.VISIBLE;
    private int frame2Visibility = View.VISIBLE;
    private int frame3Visibility = View.VISIBLE;


    // Light Sensor
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private long mLightSensorStartTime;
    private final long LIGHT_WAKE_DELAY = 4000;   // time delay before screen will wake due to light changes
    private RecentLightValues mRecentLightValues;

    // Mirror state
    public static final int ASLEEP = 0;
    public static final int LIGHT_SLEEP = 1;
    public static final int AWAKE = 2;

    // Sleep state & wakelocks
    private int mirrorSleepState;
    private int defaultScreenTimeout;
    private final int WAKELOCK_TIMEOUT = 100;            // Wakelock should be held only briefly to trigger screen wake
    private PowerManager.WakeLock mWakeLock;
    private Timer mUITimer;
    private final long DEFAULT_INTERACTION_TIMEOUT = 1000 * 60 * 5;
    private long mInteractionTimeout = DEFAULT_INTERACTION_TIMEOUT; // User interactions reset screen on timer to 5 minutes
    private final int SLEEP_DELAY = 5000;                           // Timeout for lightSleep -> sleep transition
    private PowerManager mPowerManager;

    // TTS
    private TTSHelper mTTSHelper;

    // NSD
    NsdHelper mNsdHelper;
    private Handler mRemoteHandler;
    private RemoteConnection mRemoteConnection;

    // Speech recognition
    private Messenger mMessenger = new Messenger(new IHandler());
    private boolean mIsBound;
    private Messenger mService;

    // Sound effects
    private MediaPlayer mFXPlayer;

    private Runnable uiTimerRunnable = new Runnable() {
        @Override
        public void run() {
            clearScreenOnFlag();
        }
    };

    // used to establish a service connection
    private ServiceConnection mVoiceConnection = new ServiceConnection() {
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

    /**
     * Callback from NewsFragment when a news article has been selected
     *
     * @param articleTitle article title
     * @param articleBody  article text
     */
    @Override
    public void onArticleSelected(String articleTitle, String articleBody) {
        Fragment fragment = NewsBodyFragment.NewInstance(articleTitle, articleBody);
        displayFragment(fragment, Constants.NEWS_BODY, true);
    }

    /**
     * handles the messages from VoiceService to this Activity
     *
     */
    public class IHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VoiceService.RESULT_SPEECH:
                    String result = msg.getData().getString("result");
                    handleVoiceCommand(result);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Handler for receiving messages from the remote control application
     */
    public class RemoteHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            String command = msg.getData().getString("msg");
            if (command.equals(RemoteConnection.SERVER_STARTED)) {
                // Server Socket has been created
                registerNsdService();
            } else {
                handleRemoteCommand(command);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkMarshmallowPermissions();
        mContext = getApplicationContext();

        // Load any application preferences. If prefs do not exist, set them to defaults
        mPreferences = Preferences.getInstance(this);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        setContentView(R.layout.activity_main);
        mira = Mira.getInstance(this);

        /*
         content frames hold data displays.
         frame1 = weather
         frame2 = help
         frame3 = data / variable
        */
        contentFrame1 = (ViewGroup)findViewById(R.id.content_frame_1);
        contentFrame2 = (ViewGroup)findViewById(R.id.content_frame_2);
        contentFrame3 = (ViewGroup)findViewById(R.id.content_frame_3);

        initializeLightSensor();

        // TextToSpeech (TTS) init
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

        // Start Network Discovery Service (NSD) & create handler
        mRemoteHandler = new RemoteHandler();
        mRemoteConnection = new RemoteConnection(this, mRemoteHandler);
        mNsdHelper = new NsdHelper(this);

        // Status Icons
        imgRemoteIcon = (ImageView)findViewById(R.id.remote_icon);
        imgRemoteDisabledIcon = (ImageView)findViewById(R.id.remote_dc_icon);
        imgSpeechIcon = (ImageView) findViewById(R.id.speech_icon);
        if (mPreferences.isVoiceEnabled()) {
            imgSpeechIcon.setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
    }

    private void checkMarshmallowPermissions() {
        // check for permission to write system settings on API 23 and greater.
        // Leaving this in case we need the WRITE_SETTINGS permission later on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
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

        mIsBound = bindService(new Intent(this, VoiceService.class), mVoiceConnection, BIND_AUTO_CREATE);
        addScreenOnFlag();
        resetInteractionTimer();

        if (mPowerManager.isScreenOn()) {
            mPreferences.resetScreenBrightness();
        }

        // start NSD
        //mNsdHelper.discoverServices();

        mPreferences.setVolumesToPrefValues();
        stopLightSensor();
        startSpeechRecognition();

        // on first load show initialFragment
        if (mCurrentFragment == null || mCurrentFragment == Constants.CALENDAR) { //Temporary Fix TODO: Fix
            wakeScreenAndDisplay(mInitialFragment);
        }
        // if the system was put to sleep fr
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPause() {
        super.onPause();
        Log.i(Constants.TAG, "onPause");
        // If the screen is not turning off, the app is going into the background: speech recognition is stopped.
        // This is (mostly) for debugging purposes as the finished program should always be in foreground.
        if (mPowerManager.isScreenOn()) {
            stopSpeechRecognition();
            mPreferences.setVolumesToSystemValues();
            setDefaultScreenOffTimeout();
        } else {
            // Otherwise the screen is turning off: start Light Sensor
            startLightSensor();
        }
        stopUITimer();

        //stop NSD. This will prevent discovery while sleeping....
        //mNsdHelper.stopDiscovery();
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
        CacheManager.destroy();

        mNsdHelper.tearDown();
        mRemoteConnection.tearDown();

        unbindService(mVoiceConnection);
        mIsBound = false;
        Log.i(Constants.TAG, "onDestroy");
    }

    // ------------------------- Broadcasts --------------------------

    /**
     * Broadcast a message on intentName. This is used to send any command not related to starting
     * a fragment to all listeners. The listeners can then take action if required by the command.
     *
     * @param intentName intent name
     * @param msg        String message to send
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

        // Set content frames sizes to their stored values
        restoreContentFrameVisibility();

        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        mWakeLock.acquire(WAKELOCK_TIMEOUT);
    }

    protected void exitLightSleep() {

        restoreContentFrameVisibility();
        setDefaultScreenOffTimeout();
        addScreenOnFlag();
        resetInteractionTimer();
        stopLightSensor();
        mirrorSleepState = AWAKE;
    }

    protected void enterLightSleep() {

        mirrorSleepState = LIGHT_SLEEP;
        mInteractionTimeout = DEFAULT_INTERACTION_TIMEOUT;
        resetInteractionTimer();

        // temporarily hide all content frames for duration of LIGHT_SLEEP
        setContentFrameVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        clearScreenOnFlag();
        setScreenOffTimeout();
        stopUITimer();
        startLightSensor();
        mira.saySleepMessage();

    }

    // ------------------------ UI Visibility / Content Frames ---------------------------

    /**
     * Makes a view visible if it is currently INVISIBLE or GONE
     * @param view view to show
     */
    public void showViewIfHidden(View view){
        if (viewHidden(view)) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public boolean viewHidden(View view){
        return (view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE);
    }

    /**
     * Sets all content frames and adjusts view to new values
     *
     */
    protected void setContentFrameValues(int frameOne, int frameTwo, int frameThree) {
        frame1Visibility = frameOne;
        frame2Visibility = frameTwo;
        frame3Visibility = frameThree;
        restoreContentFrameVisibility();
    }

    protected void restoreContentFrameVisibility() {
        contentFrame1.setVisibility(frame1Visibility);
        contentFrame2.setVisibility(frame2Visibility);
        contentFrame3.setVisibility(frame3Visibility);
    }

    /**
     * Temporarily set content frame visibility to given values
     */
    private void setContentFrameVisibility(int frameOne, int frameTwo, int frameThree) {
        contentFrame1.setVisibility(frameOne);
        contentFrame2.setVisibility(frameTwo);
        contentFrame3.setVisibility(frameThree);
    }

    // Restores the screen off to the duration set when the application first ran.
    protected void setDefaultScreenOffTimeout() {
        // sanity check to prevent screen lockout from super-short screen timeout settings.
        if (defaultScreenTimeout < 1000) defaultScreenTimeout = 10000;
        Log.i(Constants.TAG, "setting screen timeout: " + defaultScreenTimeout + " ms");
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defaultScreenTimeout);
    }

    protected void setScreenOffTimeout() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SLEEP_DELAY);
    }

    // Flags the system to keep the screen on indefinitely.
    protected void addScreenOnFlag() {
        Log.i(Constants.TAG, "Adding FLAG_KEEP_SCREEN_ON");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Removes the KEEP_SCREEN_ON flag, allowing the screen to timeout normally.
    protected void clearScreenOnFlag() {
        Log.i(Constants.TAG, "clearing FLAG_KEEP_SCREEN_ON");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /** Start a timer to track interval between user interactions.
    * When expired, clear the screen on flag so the screen can time out per system settings.
    */
     protected void resetInteractionTimer() {
        stopUITimer();
        mUITimer = new Timer();
        Log.i(Constants.TAG, "Interaction timer set :: " + mInteractionTimeout + " ms");
        mUITimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(Constants.TAG, "Interaction timeout reached :: " + mInteractionTimeout + " ms");
                MainActivity.this.runOnUiThread(uiTimerRunnable);
            }
        }, mInteractionTimeout);
    }

    protected void stopUITimer() {
        if (mUITimer != null) {
            Log.i(Constants.TAG, "Interaction timer cancelled");
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
            wakeScreenAndDisplay(item.toString().toLowerCase(Locale.US));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (DEBUG)
            Log.i(Constants.TAG, "NavigationItemSelected: " + item.toString());
        wakeScreenAndDisplay(item.toString().toLowerCase(Locale.US));
        return true;
    }

    private void displayHelpFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame_2, fragment, Constants.HELP);
        ft.commit();
    }

    /**
     * Display the fragment within content_frame_3
     *
     * @param fragment       fragment to show
     * @param addToBackStack if fragment should be added to back stack
     */
    private void displayFragment(Fragment fragment, String tag, boolean addToBackStack) {
        Log.i(Constants.TAG, "Displaying fragment :: " + tag);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame_3, fragment, tag);
        if (!isFinishing()) {
            if (addToBackStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        } else {
            Log.e(Constants.TAG, "commit skipped. isFinishing() returned true");
        }
    }

    /**
     * Remove the fragment given by tag if it exists
     * @param tag tag to remove
     */
    private void removeFragment(String tag) {
        Log.i(Constants.TAG, "removing fragment: " + tag);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            ft.remove(fragment);
            ft.commit();
        }
    }

    /**
     * Show a toast centered in the top center of the screen
     *
     * @param text     text to display
     * @param duration int duration: ex. Toast.LENGTH_LONG
     */
    public void showToast(String text, int duration) {
            showToast(text, Gravity.TOP | Gravity.CENTER_HORIZONTAL, duration);
    }

    /**
     * Show a toast with given gravity for duration
     * @param text text to show
     * @param gravity View.Gravity
     * @param duration Toast.Duration
     */
    @SuppressWarnings("deprecation")
    public void showToast(String text, int gravity, int duration) {

        if (!mPowerManager.isScreenOn()) return;

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView txtLayout = (TextView) layout.findViewById(R.id.text);
        txtLayout.setText(text);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(gravity, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Entry point for processing user commands.
     *
     * If sleeping, this will ignore commands except those which cause a state transition to "awake".
     * If command would wake the application, trigger proper state change and handle the command.
     *
     * Actions related to commands are processed in this order: Wake the screen, hide the help fragment,
     * close menu drawer, set content frame visibility, then change fragments and / or broadcast command to
     * command listeners.
     *
     * @param command input command
     */
    public void wakeScreenAndDisplay(String command) {
        if (mirrorSleepState == AWAKE) {
            resetInteractionTimer();
            handleHelpFragment(command);
        } else if (commandWakesFromSleep(command)) {
            if (mirrorSleepState == ASLEEP) {
                exitSleep();
            } else {
                exitLightSleep();
                if (command.equals(Constants.NIGHT_LIGHT)) {
                    // if the command is light (a special case) wake and directly show LightFragment
                    handleHelpFragment(command);
                }
            }
        }
    }

    public boolean commandWakesFromSleep(String command) {
        return (command.equals(Constants.WAKE)
                || command.equals(Constants.NIGHT_LIGHT)
                || command.equals(Constants.MIRA_WAKE));
    }

    /**
     * Help command displays the helpFragment & sets content visibility to default.
     * Saying "help" again closes help
     *
     * @param command command to process
     */
    public void handleHelpFragment(String command) {

        if (command.equals(Constants.HELP) || command.equals(Constants.SHOW_HELP)) {
            boolean helpIsVisible = (null != getSupportFragmentManager().findFragmentByTag(Constants.HELP));
            Log.i(Constants.TAG, "helpIsVisible :: " + helpIsVisible);
            if (helpIsVisible) {
                // remove HelpFragment if visible
                removeFragment(Constants.HELP);
            } else  {
                // If frame3 is in any visible state, return it to 'small screen' proportion
                if (frame3Visibility == View.VISIBLE) {
                    setContentFrameValues(View.VISIBLE, View.VISIBLE, View.VISIBLE);
                }
                displayHelpFragment(HelpFragment.newInstance(getCurrentFragment()));
            }
        }
        closeMenuDrawer(command);
    }

    /**
     * Close the MenuDrawer if it is open. Open it on "Drawer" command
     *
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

        setContentVisibility(command);
    }


    /**
     * Adjust the visible content frames if required by the command. Currently empty.
     * @param command command to be executed
     */
    private void setContentVisibility(String command) {
        handleCommand(command);
    }

    /**
     * Show a fragment or broadcast a command to listeners.
     * Do not call this method directly, instead use wakeScreenAndDisplay, which will make sure
     * the application is in the appropriate sleep state.
     *
     * @param command command to process
     */
    private void handleCommand(String command) {
        Fragment fragment = null;

        if (DEBUG) {
            Log.i(Constants.TAG, "handleCommand() status:" + mirrorSleepState + " command:\"" + command + "\"");
        }

        // Refuse commands if they would re-load the currently visible fragment
        if (command.equals(mCurrentFragment)) {
            return;
        }

        // look for news desk
        if (Constants.DESK_HASH.contains(command)) {
            fragment = NewsFragment.newInstance(command);
        }

        // Create fragment based on the command. If the input string is not a fragment,
        // broadcast the command to all registered receivers for evaluation.
        switch (command) {
            case Constants.CALENDAR:
                fragment = new CalendarFragment();
                break;
            case Constants.CAMERA:
                fragment = new CameraFragment();
                break;
            case Constants.CLOSE_SCREEN:
            case Constants.CLOSE_WINDOW:
            case Constants.HIDE_SCREEN:
                //fragment = new BlankFragment();
                setContentFrameValues(View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case Constants.FACEBOOK:
                fragment = new FacebookFragment();
                break;
            case Constants.FORECAST:
                fragment = new ForecastFragment();
                break;
            case Constants.GALLERY:
                fragment = new GalleryFragment();
                break;
            case Constants.BACK:
            case Constants.GO_BACK:
                if (frame3Visibility != View.INVISIBLE) {
                    // Can't go back if the window is closed.
                    getSupportFragmentManager().popBackStack();
                }
                break;
            case Constants.MAXIMIZE:
            case Constants.FULL_SCREEN:
                setContentFrameValues(View.GONE, View.GONE, View.VISIBLE);
                break;
            case Constants.MINIMIZE:
            case Constants.SMALL_SCREEN:
                setContentFrameValues(View.VISIBLE, View.VISIBLE, View.VISIBLE);
                break;
            case Constants.NIGHT_LIGHT:
            case Constants.SHOW_LIGHT:
                // Night light always starts in full screen
                setContentFrameVisibility(View.GONE, View.GONE, View.VISIBLE);
                fragment = new LightFragment();
                break;
            case Constants.NEWS:
                //NewsFragment.mGuardURL = NewsFragment.mDefaultGuardURL;
                fragment = NewsFragment.newInstance("world");
                break;
            case Constants.OPEN_WINDOW:
                showViewIfHidden(contentFrame3);
                break;
            case Constants.PHOTOS:
                // create photos fragment
                break;
            case Constants.QUOTES:
                fragment = new QuoteFragment();
                break;
            case Constants.SETTINGS:
            case Constants.OPTIONS:
                fragment = SettingsFragment.newInstance();
                break;
            case Constants.SLEEP:
            case Constants.GO_TO_SLEEP:
            case Constants.MIRA_SLEEP:
                enterLightSleep();
                command = mCurrentFragment;
                break;
            case Constants.STAY_AWAKE:
                // Screen awake for 7 days
                if (mInteractionTimeout == 604800000) {
                    speakText(getResources().getString(R.string.speech_stay_awake_err));
                }else {
                    speakText(getResources().getString(R.string.speech_stay_awake));
                    mInteractionTimeout = 604800000;
                    resetInteractionTimer();
                }
                break;
            case Constants.TRAFFIC:
                fragment = new TrafficFragment();
                break;
            case Constants.TWITTER:
                fragment = new TwitterFragment();
                break;
            case Constants.WAKE:
                break;
            case Constants.WIDE_SCREEN:
                setContentFrameValues(View.VISIBLE, View.GONE, View.VISIBLE);
                break;
            default:
                broadcastMessage("inputAction", command);
                break;
        }

        if (fragment != null) {
            mCurrentFragment = command;
            //boolean addToBackStack = !(fragment instanceof BlankFragment);
            displayFragment(fragment, command, true);
            showViewIfHidden(contentFrame3);
        }
    }

    /**
     * Gets the fragment currently being viewed.
     *
     * @return String fragment name
     */
    protected String getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     * Show or hide the given icon
     *
     * @param icon ImageView to be adjusted
     * @param display true to display icon, false to hide
     */
    public void showIcon(ImageView icon, boolean display) {
        if (display) {
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }
    }

    public void showRemoteIcon(boolean display){
        if (display && mPreferences.isRemoteEnabled()) {
            showIcon(imgRemoteIcon, true);
        } else if (!display) {
            showIcon(imgRemoteIcon, false);
        }
    }

    public void showRemoteDisabledIcon(boolean display){
        if (display && !mPreferences.isRemoteEnabled()){
            showIcon(imgRemoteDisabledIcon, true);
        } else {
            showIcon(imgRemoteDisabledIcon, false);
        }
    }

    public void showSpeechIcon(boolean showIcon) {
        if (showIcon && mPreferences.isVoiceEnabled()) {
            showIcon(imgSpeechIcon, true);
        } else if (!showIcon) {
            showIcon(imgSpeechIcon, false);
        }
    }


    // ----------------------- SPEECH RECOGNITION --------------------------

    /**
     * Handle the result of speech input.
     * Normalize inputs when several phrases are paired with the same action ('settings' & 'options')
     *
     * @param input the command the user gave
     */
    @SuppressWarnings("deprecation")
    public void handleVoiceCommand(String input) {
        //String voiceInput = input.trim();
        Log.i(Constants.TAG, "handleVoiceCommand:\"" + input + "\"");

        // if voice is disabled, ignore everything except "start listening" and "wake / night light" commands
        if (!mPreferences.isVoiceEnabled() && !commandWakesFromSleep(input)) {
            if (input.equals(Preferences.CMD_VOICE_ON) || input.equals(Preferences.CMD_VOICE_OFF)) {
                broadcastMessage("inputAction", input);
            } else {
                showToast(getResources().getString(R.string.speech_voice_off_err), Toast.LENGTH_SHORT);
            }
            return;
        }

        // show the command to the user
        showToast(input, Toast.LENGTH_SHORT);

        // reduce duplicate commands to one?
        switch (input) {
            case Preferences.CMD_DISABLE_REMOTE:
                input = Preferences.CMD_REMOTE_OFF;
                break;
            case Preferences.CMD_ENABLE_REMOTE:
                input = Preferences.CMD_REMOTE_ON;
                break;
        }

        wakeScreenAndDisplay(input);
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
    public void startSpeechRecognition() {
        Log.i(Constants.TAG, "startSpeechRecognition()");
        if (mTTSHelper.isSpeaking() || mService == null) return;
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
    public void stopSpeechRecognition() {
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
     *
     * @param _id sound resource to play
     */
    public void playSound(int _id) {
        if (mFXPlayer != null) {
            mFXPlayer.reset();
            mFXPlayer.release();
        }
        mFXPlayer = MediaPlayer.create(this, _id);
        if (mFXPlayer != null) {
            mFXPlayer.start();
        }
        stopSpeechRecognition();

        mFXPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                startSpeechRecognition();
            }
        });
    }

    // --------------------------------- Text to Speech (TTS) ---------------------------------


    /**
     * Say a phrase using text to speech
     *
     * @param phrase to speak
     */
    public void speakText(final String phrase) {
        Thread mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTTSHelper.start(phrase);
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
        if (mTTSHelper != null) {
            mTTSHelper.stop();
        }
    }

    public boolean isTTSSpeaking() {
        return (mTTSHelper != null && mTTSHelper.isSpeaking());
    }

    // --------------------------- REMOTE CONTROL -----------------------------
    
    public void registerNsdService() {
        if (mRemoteConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mRemoteConnection.getLocalPort());
        } else {
            Log.d(Constants.TAG, "ServerSocket isn't bound.");
        }
    }

    public void unregisterNsdService(){
        mNsdHelper.unregisterService();
    }

    public void connectToRemote(NsdServiceInfo service) {
        if (service != null) {
            Log.d(NsdHelper.TAG, "Connecting to server :: " + service.toString());
            mRemoteConnection.connectToServer(service.getHost(), service.getPort());
        } else {
            Log.d(NsdHelper.TAG, "No service to connect to!");
        }
    }


    /**
     * Callback from RemoteServerAsyncTask when a command is received from the remote control.
     *
     * @param command String: received command
     */
    public void handleRemoteCommand(String command) {
        Log.i(Constants.TAG, "remote msg :: " + command);
        if (command.equals("light")) {
            command = Constants.NIGHT_LIGHT;
        }
        if (mPreferences.isRemoteEnabled())
            wakeScreenAndDisplay(command);
        else {
            Log.i(Constants.TAG, "Remote Disabled. Command ignored: \"" + command + "\"");
        }

    }


    // --------------------------- LIGHT SENSOR --------------------------------------

    private void initializeLightSensor() {
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
     *
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
            if (currentLight > recentLightAvg * 3 && lightWakeDelayExceeded()) {
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

        float getAverage() {
            float sum = 0;
            for (float v : recentValues) {
                sum += v;
            }
            return sum / recentValues.length;
        }
    }
}