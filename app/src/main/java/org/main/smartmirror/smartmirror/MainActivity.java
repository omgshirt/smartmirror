package org.main.smartmirror.smartmirror;

import android.app.ActionBar;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
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
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener, NewsFragment.ArticleSelectedListener, GmailFragment.OnNextMessageListener {

    // Globals, prefs, debug flags
    public static final boolean DEBUG = false;
    private static Context mContext;
    private Preferences mPreferences;

    // Mira
    private Mira mira;

    // Content Frames
    private ViewGroup contentFrame1;
    private ViewGroup contentFrame2;
    private ViewGroup contentFrame3;

    private ImageView imgRemoteIcon;
    private ImageView imgRemoteDisabledIcon;
    private ImageView imgSoundOffIcon;
    private ImageView imgSpeechIcon;
    private ImageView imgStayAwakeIcon;


    // Set initial fragments & track displayed views
    private String mInitialFragment = Constants.WORLD;
    private Stack<Fragment> mForwardStack;

    // FrameSize maintains the size of the content window between state changes
    private int frame1Visibility = View.VISIBLE;
    private int frame2Visibility = View.VISIBLE;
    private int frame3Visibility = View.VISIBLE;

    private enum FrameSize {CLOSE_SCREEN, SMALL_SCREEN, WIDE_SCREEN, FULL_SCREEN}

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

    // Sleep state
    private boolean nightLightOnWake = false;           // tracks if device should wake into LightFragment
    private int mirrorSleepState;

    // Interaction Timer / Wakelock
    private int defaultScreenTimeout;
    private final int WAKELOCK_TIMEOUT = 100;            // Wakelock should be held only briefly to trigger screen wake
    private PowerManager.WakeLock mWakeLock;
    private Timer mUITimer;
    private final long DEFAULT_INTERACTION_TIMEOUT = 1000 * 60 * 5;
    private long mInteractionTimeout = DEFAULT_INTERACTION_TIMEOUT; // User interactions reset screen on timer to 5 minutes
    private final int SLEEP_DELAY = 5000;                           // Timeout for lightSleep -> sleep transition
    private PowerManager mPowerManager;

    // Text to Speech - TTS
    private TTSHelper mTTSHelper;

    // Network Service Discovery - NSD
    NsdHelper mNsdHelper;
    private Handler mRemoteHandler;
    private RemoteConnection mRemoteConnection;

    // Speech recognition
    private Messenger mMessenger = new Messenger(new IHandler());
    private boolean mIsBound;
    private Messenger mService;

    // Sound effects
    private MediaPlayer mFXPlayer;

    // Cancel Audio routing to headphones
    private HeadphoneAudioCanceller mHeadphoneAudioCanceller;


    private Runnable uiTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPreferences.isStayingAwake()) {
                Log.i(Constants.TAG, "Screen set to stay awake");
            } else {
                clearScreenOnFlag();
            }
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
        Fragment fragment = NewsBodyFragment.newInstance(articleTitle, articleBody);
        displayFragment(fragment, Constants.NEWS_BODY, true);
    }

    /**
     * handles the messages from VoiceService to this Activity
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
            assert command != null;
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

        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        mira = Mira.getInstance(this);

        // Load any application preferences. If prefs do not exist, set them to defaults
        mPreferences = Preferences.getInstance(this);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // On first run, show the AccountActivity for setup.
        if (mPreferences.isFirstTimeRun()) {
            startActivity(new Intent(this, AccountActivity.class));
        }

        // Set up the forward stack. This mimics the action of the back stack, allowing
        // users to navigate forwards through data displays.
        if (mForwardStack == null)
            mForwardStack = new Stack<>();

        /*
         Initialize the content frames.
         frame1 = weather / gmail / calendar / traffic
         frame2 = help area
         frame3 = user-selected
        */
        contentFrame1 = (ViewGroup) findViewById(R.id.content_frame_1);
        contentFrame2 = (ViewGroup) findViewById(R.id.content_frame_2);
        contentFrame3 = (ViewGroup) findViewById(R.id.content_frame_3);

        initializeLightSensor();

        // TextToSpeech (TTS) init
        mTTSHelper = new TTSHelper(this);

        // Get the system screen timeout. This value should be written back to the system settings
        // when the application finishes.
        try {
            defaultScreenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Set up ScreenReceiver to hold screen on / off status
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        // Start Network Discovery Service (NSD) & create handler for communication with RemoteConnection class.
        mRemoteHandler = new RemoteHandler();
        mRemoteConnection = new RemoteConnection(this, mRemoteHandler);
        mNsdHelper = new NsdHelper(this);

        // Status Icons: Remote / Remote Disabled / Speech / Stay Awake
        // Initialize and display icons based on current preference settings
        imgRemoteIcon = (ImageView) findViewById(R.id.remote_icon);
        imgRemoteDisabledIcon = (ImageView) findViewById(R.id.remote_dc_icon);
        if (!mPreferences.isRemoteEnabled()) {
            imgRemoteDisabledIcon.setVisibility(View.VISIBLE);
        }

        imgSpeechIcon = (ImageView) findViewById(R.id.speech_icon);
        if (mPreferences.isVoiceEnabled()) {
            imgSpeechIcon.setVisibility(View.VISIBLE);
        }

        imgStayAwakeIcon = (ImageView) findViewById(R.id.stay_awake_icon);
        if (mPreferences.isStayingAwake()) {
            imgStayAwakeIcon.setVisibility(View.VISIBLE);
        }

        imgSoundOffIcon = (ImageView) findViewById(R.id.sound_off_icon);
        if (!mPreferences.isSoundOn()) {
            imgSoundOffIcon.setVisibility(View.VISIBLE);
        }


        // Set up the navigation drawer. This is useful for debugging, but could be remove on release.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);


        // Set audio manager to force audio over HDMI.
        // This is essential when using a lav mic for audio input as the system will attempt to route
        // audio output to the headphone jack.
        mHeadphoneAudioCanceller = new HeadphoneAudioCanceller(this);

    }

    public static Context getContextForApplication() {
        return mContext;
    }

    // -------------------------  LIFECYCLE CALLBACKS ----------------------------

    /**
     * If this is called by the device waking up, it will trigger a new call to handleCommand(),
     * reloading the last visible fragment
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(Constants.TAG, "onStart");

        setUIFlags();

        if (mirrorSleepState == ASLEEP) {
            restoreContentFrameVisibility();
        }

        mirrorSleepState = AWAKE;

        mIsBound = bindService(new Intent(this, VoiceService.class), mVoiceConnection, BIND_AUTO_CREATE);

        addScreenOnFlag();
        resetInteractionTimer();
        stopLightSensor();
        startSpeechRecognition();

        // on first load show initialFragment
        if (getCurrentFragment() == null) {
            wakeScreenAndDisplay(mInitialFragment);
        } else if (nightLightOnWake) {
            // If the mirror was woken with NIGHT_LIGHT command, change to that fragment immediately.
            nightLightOnWake = false;
            wakeScreenAndDisplay(Constants.NIGHT_LIGHT);
        }
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
            setDefaultScreenOffTimeout();
        } else {
            // Otherwise the screen is turning off: start Light Sensor
            startLightSensor();
        }
        stopUITimer();
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

        mHeadphoneAudioCanceller.teardown();

        mNsdHelper.tearDown();
        mRemoteConnection.tearDown();
        setDefaultScreenOffTimeout();

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
        //Log.i(Constants.TAG, "broadcastMessage:\"" + msg + "\"");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // -------------------------- SCREEN WAKE / SLEEP ---------------------------------

    /**
     * Calling forces device to bypass keyguard if enabled.
     * It will not override password, pattern or biometric locks if enabled from the system settings.
     * Acquiring the wakelock in this method will trigger the application to move from onStop to onRestart.
     */
    @SuppressWarnings("deprecation")
    protected void exitSleep() {
        if (mirrorSleepState != ASLEEP) return;
        Log.i(Constants.TAG, "exitSleep() called");

        // If wake command was triggered by NIGHT_LIGHT command, wake silently.
        if (nightLightOnWake) {
            mira.appWakingSilently();
        } else {
            mira.appWaking();
        }

        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        mWakeLock.acquire(WAKELOCK_TIMEOUT);
    }

    protected void exitLightSleep() {
        if (mirrorSleepState != LIGHT_SLEEP) return;
        restoreContentFrameVisibility();
        setDefaultScreenOffTimeout();
        addScreenOnFlag();
        resetInteractionTimer();
        stopLightSensor();
        mirrorSleepState = AWAKE;
        mira.appWaking();
    }

    protected void enterLightSleep() {
        if (mirrorSleepState != AWAKE) return;
        mPreferences.setStayAwake(false);

        mirrorSleepState = LIGHT_SLEEP;
        mInteractionTimeout = DEFAULT_INTERACTION_TIMEOUT;
        resetInteractionTimer();

        // temporarily hide all content frames for duration of LIGHT_SLEEP
        setContentFrameVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        clearScreenOnFlag();
        setScreenOffTimeout();
        stopUITimer();
        startLightSensor();

        // Stop any ongoing speech. This may cause sleep messages to fail...
        mTTSHelper.stop();
        mira.appSleeping();
    }

    // ------------------------ UI Visibility / Content Frames ---------------------------

    public void setUIFlags() {
        // Hide UI items, force full screen view and hide the actionbar
        Log.i(Constants.TAG, "setting UI flags");
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public boolean viewHidden(View view) {
        return (view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE);
    }

    /**
     * Sets visibility for the 3 content frames based on the FrameSize parameter.
     * @param fs FrameSize
     */
    protected void setContentFrameValues(FrameSize fs) {

        switch (fs) {
            case CLOSE_SCREEN:
                setContentFrameValues(View.VISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SMALL_SCREEN:
                setContentFrameValues(View.VISIBLE, View.VISIBLE, View.VISIBLE);
                break;
            case WIDE_SCREEN:
                setContentFrameValues(View.VISIBLE, View.GONE, View.VISIBLE);
                break;
            case FULL_SCREEN:
                setContentFrameValues(View.GONE, View.GONE, View.VISIBLE);
                break;
        }
    }

    /**
     * Set the visibility of the 3 content frames and stores those values for future reference.
     * Passing a null for any parameter will keep that frame in its current state.
     *
     * @param frameOne   View.VISIBILITY
     * @param frameTwo   View.VISIBILITY
     * @param frameThree View.VISIBILITY
     */
    protected void setContentFrameValues(@Nullable Integer frameOne, @Nullable Integer frameTwo, @Nullable Integer frameThree) {
        frame1Visibility = (frameOne == null) ? frame1Visibility : frameOne;
        frame2Visibility = (frameTwo == null) ? frame2Visibility : frameTwo;
        frame3Visibility = (frameThree == null) ? frame3Visibility : frameThree;
        restoreContentFrameVisibility();
    }

    protected void restoreContentFrameVisibility() {
        contentFrame1.setVisibility(frame1Visibility);
        contentFrame2.setVisibility(frame2Visibility);
        contentFrame3.setVisibility(frame3Visibility);
    }

    /**
     * Temporarily sets content frame visibility to given values.
     * This can be undone by calling restoreContentFrameVisibility()
     */
    private void setContentFrameVisibility(int frameOne, int frameTwo, int frameThree) {
        contentFrame1.setVisibility(frameOne);
        contentFrame2.setVisibility(frameTwo);
        contentFrame3.setVisibility(frameThree);
    }

    // Restore screen off timer to the value captured when the application launched.
    protected void setDefaultScreenOffTimeout() {
        // sanity check to prevent screen lockout from super-short screen timeout settings.
        if (defaultScreenTimeout < 1000) defaultScreenTimeout = 10000;
        Log.i(Constants.TAG, "setting screen timeout: " + defaultScreenTimeout + " ms");
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defaultScreenTimeout);
    }

    protected void setScreenOffTimeout() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SLEEP_DELAY);
    }

    /**
     * Keep the screen on indefinitely.
     */
    protected void addScreenOnFlag() {
        Log.i(Constants.TAG, "Adding FLAG_KEEP_SCREEN_ON");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Clear the KEEP_SCREEN_ON flag, allowing the screen to timeout normally.
     */
    protected void clearScreenOnFlag() {
        Log.i(Constants.TAG, "clearing FLAG_KEEP_SCREEN_ON");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Start a timer to track interval between user interactions.
     * When this timers expires, clear the screen on flag so the screen can sleep per system settings.
     */
    protected void resetInteractionTimer() {
        stopUITimer();
        mUITimer = new Timer();
        //Log.i(Constants.TAG, "Interaction timer set :: " + mInteractionTimeout + " ms");
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
            //Log.i(Constants.TAG, "Interaction timer cancelled");
            mUITimer.cancel();
        }
    }

    // -------------------------- DRAWER AND INTERFACE ---------------------------------

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
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

    /**
     * Display the HelpFragment within content_frame_2
     */
    private void createHelpFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = HelpFragment.newInstance(getCurrentFragment().getTag());
        ft.replace(R.id.content_frame_2, fragment, Constants.HELP);
        ft.commit();
    }

    /**
     * Display a fragment within content_frame_3
     *
     * @param fragment       fragment to show
     * @param addToBackStack if true, fragment will be added to the back stack
     */
    private void displayFragment(Fragment fragment, String tag, boolean addToBackStack) {
        Log.i(Constants.TAG, "Displaying fragment :: " + tag);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
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
     * Remove the fragment given by tag (if it exists)
     *
     * @param tag tag to remove
     */
    public void removeFragment(String tag) {
        Log.i(Constants.TAG, "removing fragment: " + tag);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && !fragment.isRemoving()) {
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
     *
     * @param text     text to show
     * @param gravity  View.Gravity
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
     * Show an error fragment when a user is not logged in to a web service
     */
    public void displayNotSignedInFragment(String tag, boolean addToBackStack) {
        Fragment fragment = NotSignedInFragment.newInstance(getCurrentFragment().getTag());
        displayFragment(fragment, tag, addToBackStack);
    }

    /**
     * Entry point for processing user commands.
     * <p/>
     * If sleeping, this will ignore commands except those which cause a state transition to "awake".
     * If command would wake the application, trigger proper state change and handle the command.
     * <p/>
     * Actions related to commands are processed in this order: Wake the screen, hide the help fragment,
     * close menu drawer, set content frame visibility, then change fragments or broadcast the command to
     * any command listeners.
     *
     * @param command input command
     */
    public void wakeScreenAndDisplay(String command) {
        if (mirrorSleepState == AWAKE) {
            resetInteractionTimer();
            handleHelpFragment(command);
        } else if (commandWakesFromSleep(command)) {
            if (mirrorSleepState == ASLEEP) {
                // NIGHT_LIGHT will change to that fragment & wakes device silently
                if (command.equals(Constants.NIGHT_LIGHT)) {
                    nightLightOnWake = true;
                }
                exitSleep();
            } else {
                exitLightSleep();
                if (command.equals(Constants.NIGHT_LIGHT)) {
                    //wake and directly show LightFragment
                    handleHelpFragment(command);
                }
            }
        }
    }

    public boolean commandWakesFromSleep(String command) {
        return (command.equals(Constants.WAKE)
                || command.equals(Constants.WAKE_UP)
                || command.equals(Constants.MIRROR_MIRROR)
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

        boolean helpIsVisible = (null != getSupportFragmentManager().findFragmentByTag(Constants.HELP));

        if (!helpIsVisible && (command.equals(Constants.HELP) )) {
            // If frame3 is in any visible state, return it to 'small screen' proportion
            if (frame3Visibility == View.VISIBLE) {
                setContentFrameValues(FrameSize.SMALL_SCREEN);
            }
            createHelpFragment();
        }

        if (helpIsVisible) {
            // remove HelpFragment if visible
            removeFragment(Constants.HELP);
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
            Log.i(Constants.TAG, "handleCommand() status:" + mirrorSleepState + " command: \"" + command + "\"");
        }

        /*
        Reject if command is same as tag of currentFragment. This prevents several instances of
        the same fragment type appearing 'on top' of one another, though interleaving is still possible
        (whereby the back stack would contain fragments in an order such as ABAB).

        Does not apply to music fragments.
        */
        Fragment currentFragment;
        if ((currentFragment = getCurrentFragment()) != null) {
            if (currentFragment.getTag().equals(command) && !(currentFragment instanceof MusicFragment)) {
                Log.i(Constants.TAG, "Command ignored : same as tagged fragment.");
                return;
            }
        }

        // Check whether command is a news desk
        if (Constants.DESK_HASH.contains(command)) {
            fragment = NewsFragment.newInstance(command);
        }

        // Create a new fragment based on the command. If the input string is not a fragment,
        // broadcast the command to all registered receivers for evaluation.
        if (fragment == null) {
            switch (command) {
                case Constants.GO_BACK:
                    // Can't go back if the window is closed.
                    if (frame3Visibility != View.INVISIBLE) {
                        mForwardStack.add(getCurrentFragment());
                        // Pop back stack if there's any previous fragments
                        if (getSupportFragmentManager().getBackStackEntryCount() > 1)
                            getSupportFragmentManager().popBackStack();
                    }
                    break;
                case Constants.GO_FORWARD:
                    if (!mForwardStack.isEmpty())
                        fragment = mForwardStack.pop();
                    break;
                case Constants.CALENDAR:
                    fragment = new CalendarFragment();
                    break;
                case Constants.CAMERA:
                    fragment = new CameraFragment();
                    break;
                case Constants.CLOSE_SCREEN:
                case Constants.CLOSE_WINDOW:
                case Constants.HIDE_SCREEN:
                    setContentFrameValues(FrameSize.CLOSE_SCREEN);
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
                case Constants.GMAIL:
                    fragment = new GmailFragment();
                    break;
                case Constants.HELP:
                    break;
                case Constants.MAXIMIZE:
                case Constants.FULL_SCREEN:
                    setContentFrameValues(FrameSize.FULL_SCREEN);
                    break;
                case Constants.MINIMIZE:
                case Constants.SMALL_SCREEN:
                    setContentFrameValues(FrameSize.SMALL_SCREEN);
                    break;
                case Constants.MUSIC:
                    fragment = MusicFragment.NewInstance("");
                    break;
                case Constants.NIGHT_LIGHT:
                case Constants.SHOW_LIGHT:
                    // Night light always starts in full screen
                    setContentFrameVisibility(View.GONE, View.GONE, View.VISIBLE);
                    fragment = new LightFragment();
                    break;
                case Constants.NEWS:
                    fragment = new NewsSectionListFragment();
                    break;
                case Constants.OPEN_WINDOW:
                    if (viewHidden(contentFrame3)) {
                        setContentFrameValues(null, null, View.VISIBLE);
                    }
                    break;
                case Constants.PHOTOS:
                    fragment = new PhotosFragment();
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
                    break;
                case Constants.TWITTER:
                    fragment = new TwitterFragment();
                    break;
                case Constants.WAKE:
                case Constants.WAKE_UP:
                case Constants.MIRROR_MIRROR:
                    break;
                case Constants.WIDE_SCREEN:
                    setContentFrameValues(FrameSize.WIDE_SCREEN);
                    break;
                case Constants.ALTERNATIVE:
                case Constants.AMBIENT:
                case Constants.CLASSICAL:
                case Constants.DANCE:
                case Constants.JAZZ:
                case Constants.RAP:
                case Constants.ROCK:
                    if (currentFragment instanceof MusicFragment) {
                        ((MusicFragment) currentFragment).changeToStation(command);
                    } else {
                        fragment = MusicFragment.NewInstance(command);
                    }
                    break;
                case Constants.R_ALTERNATIVE:
                case Constants.R_AMBIENT:
                case Constants.R_CLASSICAL:
                case Constants.R_DANCE:
                case Constants.R_JAZZ:
                case Constants.R_RAP:
                case Constants.R_ROCK:
                    if (currentFragment instanceof MusicFragment) {
                        ((MusicFragment) currentFragment).forceStartStation(command);
                    } else {
                        fragment = MusicFragment.NewInstance(command);
                    }
                    break;
                default:
                    broadcastMessage("inputAction", command);
                    break;
            }
        }

        if (fragment != null) {
            // put this fragment into contentFrame3. Frame 1 is added via XML. Frame 2 (help) is added via handleHelp() method
            displayFragment(fragment, command, true);

            // ensure that contentFrame3 is visible
            if (viewHidden(contentFrame3)) {
                setContentFrameValues(null, null, View.VISIBLE);
            }
        }
    }

    /**
     * Gets the fragment currently being viewed within content_frame_3.
     *
     * @return Fragment
     */
    protected Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_frame_3);
    }

    // ----------------------------- Status Icons ------------------------------------------

    /**
     * Show or hide the given icon
     *
     * @param icon    ImageView to be adjusted
     * @param display true to display icon, false to hide
     */
    public void showIcon(ImageView icon, boolean display) {
        if (display) {
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }
    }

    public void showRemoteIcon(boolean display) {
        showIcon(imgRemoteIcon, display);
    }

    public void showRemoteDisabledIcon(boolean display) {
        showIcon(imgRemoteDisabledIcon, display);
    }

    public void showSpeechIcon(boolean display) {
        showIcon(imgSpeechIcon, (display && !isTTSSpeaking()));
    }

    public void showStayAwakeIcon(boolean display) {
        showIcon(imgStayAwakeIcon, display);
    }

    public void showSoundOffIcon(boolean display) {
        showIcon(imgSoundOffIcon, display);
    }

    // ----------------------- SPEECH RECOGNITION --------------------------

    /**
     * Handle the result of speech input.
     * Normalize inputs when several phrases are paired with the same action ('settings' & 'options')
     *
     * @param command voice command returned from VoiceService
     */
    @SuppressWarnings("deprecation")
    public void handleVoiceCommand(String command) {

        Log.i(Constants.TAG, "handleVoiceCommand:\"" + command + "\"");

        // If voice is disabled, ignore everything except "start listening" and wake commands.
        // This way, the device can be awakened with more commands without first having to enable listening.
        if (!mPreferences.isVoiceEnabled() && !commandWakesFromSleep(command)) {

            if (command.equals(Preferences.CMD_VOICE_ON) || command.equals(Constants.MIRA_LISTEN)) {
                broadcastMessage("inputAction", command);
            }

            /*
            else {
                showToast(getResources().getString(R.string.speech_voice_off_err), Toast.LENGTH_SHORT);
            }
            */
            return;
        }

        // show the command to the user
        showToast(command, Toast.LENGTH_SHORT);

        switch (command) {
            case Preferences.CMD_DISABLE_REMOTE:
                command = Preferences.CMD_REMOTE_OFF;
                break;
            case Preferences.CMD_ENABLE_REMOTE:
                command = Preferences.CMD_REMOTE_ON;
                break;
            case Constants.SHOW_LIGHT:
                command = Constants.NIGHT_LIGHT;
                break;
        }

        wakeScreenAndDisplay(command);
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

    // --------------------------------- GMail ---------------------------------------------

    public int getUnreadCount() {
        int count = 0;
        GmailHomeFragment gmhf = (GmailHomeFragment) getSupportFragmentManager().findFragmentById(R.id.gmail_home_fragment);
        if (gmhf != null)
            count = gmhf.getUnreadCount();

        return count;
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
        Log.i(Constants.TAG, "speakText :: " + phrase);
        if (mPreferences.isSoundOn()) {
            mTTSHelper.start(phrase);
        }
    }

    /**
     * speak text even if speech is disabled in the preferences
     */
    public void forceSpeakText(final String phrase) {
        mTTSHelper.start(phrase);
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
        if (mRemoteConnection.getLocalPort() > -1 && mPreferences.isRemoteEnabled()) {
            mNsdHelper.registerService(mRemoteConnection.getLocalPort());
        } else {
            Log.d(Constants.TAG, "ServerSocket isn't bound.");
        }
    }

    public void unregisterNsdService() {
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

    public void disconnectRemote() {
        mRemoteConnection.stopRemoteClient();
    }

    /**
     * Callback from RemoteServerAsyncTask when a command is issued by the remote control.
     * Several commands are generic toggles and will perform different
     * operations depending on the mirror's current state.
     *
     * @param command String: received command
     */
    public void handleRemoteCommand(String command) {
        Log.i(Constants.TAG, "remote msg :: " + command);

        switch (command) {
            case Constants.REMOTE_INCREASE_SCREEN:
                command = increaseScreenSize();
                break;
            case Constants.REMOTE_TOGGLE_LISTENING:
                command = (mPreferences.isVoiceEnabled()) ? Preferences.CMD_VOICE_OFF : Preferences.CMD_VOICE_ON;
                break;
            case Constants.REMOTE_TOGGLE_SLEEPS_STATE:
                command = (mirrorSleepState == AWAKE) ? Constants.SLEEP : Constants.WAKE;
                break;
            case Constants.REMOTE_TOGGLE_SOUND:
                command = (mPreferences.isSoundOn()) ? Preferences.CMD_SOUND_OFF : Preferences.CMD_SOUND_ON;
                break;
            case Constants.REMOTE_TOGGLE_TIME_FORMAT:
                command = (mPreferences.isTimeFormat12hr()) ? Preferences.CMD_TIME_24HR : Preferences.CMD_TIME_12HR;
                break;
            case Constants.REMOTE_TOGGLE_WEATHER_FORMAT:
                command = (mPreferences.getWeatherUnits().equals(Preferences.ENGLISH)) ? Preferences.CMD_WEATHER_METRIC :
                        Preferences.CMD_WEATHER_ENGLISH;
                break;
            case Constants.REMOTE_ARTICLE_1:
                command = Constants.ONE;
                break;
            case Constants.REMOTE_ARTICLE_2:
                command = Constants.TWO;
                break;
            case Constants.REMOTE_ARTICLE_3:
                command = Constants.THREE;
                break;
            case Constants.REMOTE_ARTICLE_4:
                command = Constants.FOUR;
                break;
            case Constants.REMOTE_ARTICLE_5:
                command = Constants.FIVE;
                break;
            case Constants.REMOTE_ARTICLE_6:
                command = Constants.SIX;
                break;
            case Constants.REMOTE_ARTICLE_7:
                command = Constants.SEVEN;
                break;
            case Constants.REMOTE_ARTICLE_8:
                command = Constants.EIGHT;
                break;
            case Constants.REMOTE_ARTICLE_9:
                command = Constants.NINE;
                break;
            case Constants.REMOTE_ARTICLE_10:
                command = Constants.TEN;
                break;
        }

        if (mPreferences.isRemoteEnabled())
            wakeScreenAndDisplay(command);
        else {
            Log.i(Constants.TAG, "Remote Disabled. Command ignored: \"" + command + "\"");
        }
    }

    /**
     * Based on the current configuration of the 3 content frames, increase the size of contentFrame3
     * by one step in this order: SMALL_SCREEN -> WIDE_SCREEN -> FULL_SCREEN -> CLOSE_SCREEN
     *
     * @return command corresponding to new screen sizes
     */
    private String increaseScreenSize() {
        if (mirrorSleepState != AWAKE) return "";

        String command = Constants.SMALL_SCREEN;

        if (contentFrame3.getVisibility() == View.INVISIBLE) {
            command = Constants.SMALL_SCREEN;
        } else if (contentFrame1.getVisibility() == View.VISIBLE &&
                contentFrame2.getVisibility() == View.VISIBLE) {
            command = Constants.WIDE_SCREEN;
        } else if (contentFrame1.getVisibility() == View.GONE) {
            command = Constants.CLOSE_SCREEN;
        } else if (contentFrame2.getVisibility() == View.GONE) {
            command = Constants.FULL_SCREEN;
        }
        return command;
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
            //Log.i("Light Sensor", "Current value:" + Float.toString(currentLight) + " avg:" + recentLightAvg);
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

    public void onNextCommand() {
        //Here we update GmailHomeFragment
        GmailHomeFragment gmailHomeFrag = (GmailHomeFragment)
                getSupportFragmentManager().findFragmentById(R.id.gmail_home_fragment);

        if (gmailHomeFrag != null) {
            gmailHomeFrag.updateUnreadCount();
        }
    }

    /**
     * Configure the voice recognition to use a shorter command when music is actively streaming.
     * Setting false returns to normal command list.
     *
     * @param commandListType MusicFragment.MUSIC_COMMAND_LIST or MusicFragment.NORMAL_COMMAND_LIST
     */
    public void setVoiceCommandMode(int commandListType) {
        int msgType;

        if (commandListType == MusicFragment.MUSIC_COMMAND_LIST) {
            // set VR to music mode
            msgType = VoiceService.MUSIC_COMMAND_LIST;
        } else {
            // set VR to normal mode
            msgType = VoiceService.NORMAL_COMMAND_LIST;
        }

        try {
            Message msg = Message.obtain(null, msgType);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException re) {
            re.printStackTrace();
        }

    }
}