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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Locale;
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

    public static final int SLEEPING = 0;
    public static final int LIGHT_SLEEP = 1;
    public static final int AWAKE = 2;

    // Help
    private HelpFragment mHelpFragment;

    // Light Sensor
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private boolean mLightIsOff;


    // News
    public static String mDefaultURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";

    // Sleep state & wakelocks
    // mirrorSleepState can be SLEEPING, LIGHT_SLEEP or AWAKE
    private int mirrorSleepState;
    private BroadcastReceiver mScreenReceiver;
    private String mCurrentFragment = null;
    private final int WAKELOCK_TIMEOUT = 1000;
    private PowerManager.WakeLock mWakeLock;


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

    // used to establish a service connection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            initSpeechRecognition();
            // not sure if I need this keep me
            /*try {
                Message msg = Message.obtain(null, VoiceService.REGISTER_SERV);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;

            // not sure if I need this keep me
            /*try {
                Message msg = Message.obtain(null, VoiceService.UNREGISTER_SERV);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }
    };

    // handles the messages from Service to this Activity
    public class IHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case VoiceService.RESULT_SPEECH:
                    String result = msg.getData().getString("result");
                    if(DEBUG)
                        Log.i("MAIN", result);
                    handleVoiceCommand(result);
                    break;
                case VoiceService.SHOW_ICON:
                    speechIconHandler(true);
                    break;
                case VoiceService.HIDE_ICON:
                    speechIconHandler(false);
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

        // check for permission to write system settings on API 23 and greater.
        // Leaving this in case we need the WRITE_SETTINGS permission later on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite( getApplicationContext() )) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, 1);
            }
        }

        // initialize TTS
        mTTSHelper = new TTSHelper(this);

        // Initialize WiFiP2P services
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mWifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiChannel = mWifiManager.initialize(this, getMainLooper(), null);
        discoverPeers();

        mWifiReceiver = new WiFiDirectBroadcastReceiver(mWifiManager, mWifiChannel, this);

        // Light Sensor for waking / sleeping
        initializeLightSensor();

        // Set up ScreenReceiver to hold screen on / off status
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, intentFilter);

        // Set up view and nav drawer
        setContentView(R.layout.activity_main);
        // speech icon turn it off for now
        mSpeechIcon = (ImageView)findViewById(R.id.speech_icon);
        mSpeechIcon.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static Context getContextForApplication() {
        return mContext;
    }

    // -------------------------  LIFECYCLE CALLBACKS ----------------------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(Constants.TAG, "onStart");
        mIsBound = bindService(new Intent(this, VoiceService.class), mConnection, BIND_AUTO_CREATE);
        mirrorSleepState = AWAKE;
        // if there's a fragment pending to display, show it
        if (mCurrentFragment != null) {
            displayView(mCurrentFragment);
        } else {
            // on first run mCurrentFragment isn't set: start with weather displayed
            displayView(Constants.WEATHER);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(Constants.TAG, "onResume");
        Log.i(Constants.TAG, "ScreenIsOn:" + ScreenReceiver.screenIsOn);
        mPreferences.resetScreenBrightness();
        registerReceiver(mWifiReceiver, mWifiIntentFilter);
        if (ScreenReceiver.screenIsOn) {
            stopWifiHeartbeat();
            stopLightSensor();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(Constants.TAG, "onPause");
        Log.i(Constants.TAG, "ScreenIsOn:" + ScreenReceiver.screenIsOn);
        unregisterReceiver(mWifiReceiver);
        if (!ScreenReceiver.screenIsOn) {
            mirrorSleepState = SLEEPING;
            startWifiHeartbeat();
            startLightSensor();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(Constants.TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTSHelper.destroy();
        mPreferences.destroy();
        if (wifiHeartbeat != null) {
            wifiHeartbeat.cancel(true);
            wifiHeartbeat = null;
        }
        unbindService(mConnection);
        unregisterReceiver(mScreenReceiver);
        mIsBound = false;
        Log.i(Constants.TAG, "onDestroy");
    }

    // ------------------------- Handle Inputs / Broadcasts --------------------------

    /**
     * Broadcast a message on intentName
     * @param intentName intent name
     * @param msg String message to send
     */
    private void broadcastMessage(String intentName, String msg) {
        Intent intent = new Intent(intentName);
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // -------------------------- SCREEN WAKE / SLEEP ---------------------------------

    @SuppressWarnings("deprecation")
    protected void wakeScreen() {
        Log.i(Constants.TAG, "wakeScreen() called");
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        mWakeLock.acquire(WAKELOCK_TIMEOUT);
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
            displayView(item.toString().toLowerCase(Locale.US));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(DEBUG)
            Log.i("item selected", item.toString());
        displayView(item.toString().toLowerCase(Locale.US));
        return true;
    }

    /**
     * Handles which fragment will be displayed to the user
     * @param viewName the name of the view to be displayed
     */
    public void displayView(String viewName){
        Fragment fragment = null;
        Log.i("displayView", "status:" + mirrorSleepState + " command:\"" + viewName + "\"");
        // If sleeping, ignore commands except WAKE and NIGHT_LIGHT
        if (mirrorSleepState == SLEEPING || mirrorSleepState == LIGHT_SLEEP) {
            if (!viewName.equals(Constants.WAKE) && !viewName.equals(Constants.NIGHT_LIGHT)) return;
        }

        switch (viewName) {
            case Constants.CALENDAR:
                fragment = new CalendarFragment();
                break;
            case Constants.CAMERA:
                if(mPreferences.isCameraEnabled()) {
                    fragment = new CameraFragment();
                }
                else {
                    Toast.makeText(this, "Camera Disabled. Please say 'Enable Camera' to change this setting.", Toast.LENGTH_LONG).show();
                }
                break;
            case Constants.FACEBOOK:
                fragment = new FacebookFragment();
                break;
            case Constants.GALLERY:
                fragment = new GalleryFragment();
                break;
            case Constants.HELP:
                mHelpFragment = HelpFragment.newInstance(getCurrentFragment());
                mHelpFragment.show(getFragmentManager(), "HelpFragment");
                break;
            case Constants.HIDE_HELP:
                if (mHelpFragment != null) {
                    // call dismiss on fragment?
                    mHelpFragment.dismiss();
                    mHelpFragment = null;
                }
                break;
            case Constants.NEWS:
                fragment = new NewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("url", mDefaultURL);
                fragment.setArguments(bundle);
                break;
            case Constants.NIGHT_LIGHT:
            case Constants.LIGHT:
                stopLightSensor();
                stopWifiHeartbeat();
                if (mirrorSleepState == SLEEPING) {
                    mCurrentFragment = Constants.NIGHT_LIGHT;
                    wakeScreen();
                    return;
                } else {
                    mirrorSleepState = AWAKE;
                    fragment = new LightFragment();
                }
                break;
            case Constants.QUOTES:
                fragment = new QuotesFragment();
                break;
            case Constants.SETTINGS:
            case Constants.OPTIONS:
                fragment = new SettingsFragment();
                break;
            case Constants.SLEEP:
                fragment = new OffFragment();
                mirrorSleepState = LIGHT_SLEEP;
                break;
            case Constants.TWITTER:
                fragment = new TwitterFragment();
                break;
            case Constants.WAKE:
                if (mirrorSleepState == LIGHT_SLEEP) {
                    mirrorSleepState = AWAKE;
                    displayView(mCurrentFragment);
                } else {
                    // displayView will be called again from onStart() with the fragment to show
                    wakeScreen();
                    return;
                }
                break;
            case Constants.WEATHER:
                fragment = new WeatherFragment();
                break;

            case Constants.MAKEUP:
                fragment =new MakeupFragment();
                break;
            default:
                // The command isn't one of the view swap instructions,
                // so broadcast the viewName (our input) to any listeners.
                broadcastMessage("inputAction", viewName);
                break;
        }

        // If we're changing fragments set the wake state and do the transaction
        if(fragment != null){
            if(DEBUG) {
                Log.i("displayView", "Displaying: " + viewName);
            }
            // TODO: change this to Preferences.acknowledgeWithVoice / FX?
            playSound(R.raw.celeste_a);
            //startTTS(viewName);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);

            if (!isFinishing()) {
                ft.commit();
                // Any command != SLEEP sets stores value as last visible frag
                if ( !viewName.equals(Constants.SLEEP) ) {
                    mCurrentFragment = viewName;
                }
            } else {
                Log.e("Fragments", "commit skipped. isFinishing() returned true");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    /**
     * Gets the fragment currently being viewed. If the mirror in SLEEP or LIGHT_SLEEP,
     * this will return the value of the previously-displayed fragment.
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
    public void speechIconHandler(boolean flag){
        if(flag) {
            mSpeechIcon.setVisibility(View.VISIBLE);
        } else {
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
        Log.i("VR", "handleVoiceCommand:"+input);
        // if voice is disabled, ignore everything except "start listening" command
        if (!mPreferences.isVoiceEnabled()) {
            if (voiceInput.equals(Preferences.CMD_VOICE_ON) ) {
                broadcastMessage("inputAction", voiceInput);
            }
            return;
        }

        if(voiceInput.contains(Constants.NIGHT_LIGHT)) {
            voiceInput = Constants.NIGHT_LIGHT;
        }

        if(voiceInput.contains(Constants.SLEEP)) {
            voiceInput = Constants.SLEEP;
        }

        // Some silliness to solve "weather" showing up too many times
        if(voiceInput.contains(Constants.WEATHER)) {
            if (voiceInput.contains("english")) {
                voiceInput = Preferences.CMD_WEATHER_ENGLISH;
            } else if (voiceInput.contains("metric")) {
                voiceInput = Preferences.CMD_WEATHER_METRIC;
            }
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
            case Constants.GO_BACK:
                voiceInput = Constants.BACK;
                break;
            case Constants.GO_TO_SLEEP:
                voiceInput = Constants.SLEEP;
                break;
            case Constants.HIDE_HELP:
            case Constants.HIDE:
                voiceInput = Constants.HIDE_HELP;
                break;
            case Constants.OPTIONS:
                voiceInput = Constants.SETTINGS;
                break;
            case Constants.SHOW_HELP:
                voiceInput = Constants.HELP;
                break;
            case Constants.WAKE_UP:
                voiceInput = Constants.WAKE;
                break;
        }

        displayView(voiceInput);
    }

    public void initSpeechRecognition() {
        try {
            Log.i("VR", "startSpeechRecognition()");
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
        if(mTTSHelper.isSpeaking()) return;
        try {
            Log.i("VR", "startSpeechRecognition()");
            Message msg = Message.obtain(null, VoiceService.START_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the current speech recognition object
     */
    public void stopSpeechRecognition(){
        try {
            Log.i("VR", "stopSpeechRecognition()");
            Message msg = Message.obtain(null, VoiceService.STOP_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
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
     * @param phrase the phrase to speak
     */
    public void startTTS(final String phrase){
        Thread mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTTSHelper.speakText(phrase);
                    //Thread.sleep(2000);
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
     * Callback from RemoteServerAsyncTask when a command is received from the remote control.
     * @param command String: received command
     */
    public void handleRemoteCommand(String command) {
        if (mPreferences.isRemoteEnabled())
            displayView(command);
        else {
            Log.i("Remote", "Disabled. ignored:\"" + command + "\"");
        }

    }

    // calls the P2pManager to refresh peer list
    public void discoverPeers() {
        mWifiManager.discoverPeers(mWifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (DEBUG)
                    Log.i("Wifi", "Peer discovery successful");
            }

            @Override
            public void onFailure(int reasonCode) {
                if (DEBUG)
                    Log.i("Wifi", "discoverPeers failed: " + reasonCode);
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
     * @param info
     */
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // make this the group owner and start the server to listen for commands
        if(DEBUG)
            Log.i("Wifi", "Connection info: " + info.toString());
        mWifiInfo = info;
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 15;
        if (info.groupFormed && info.isGroupOwner) {
            if(DEBUG)
                Log.i("Wifi", "onConnectionInfo is starting server...");
            startRemoteServer();
        } else if (info.groupFormed){
            Log.i("Wifi", "group exists, mirror is not owner");
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
            discoverPeers();
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

    // OnStop, start a thread that keeps the wifip2p connection alive by pinging every 60 seconds
    public void startWifiHeartbeat() {
        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(1);

        final Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                discoverPeers();
                Log.i("Wifi", "Heartbeat: discoverPeers()" );
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
        mLightIsOff = false;
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopLightSensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do something here if sensor accuracy changes
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float currentLight = event.values[0];
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if(DEBUG) Log.i("LightSensor", Float.toString(currentLight) );
            if(currentLight < .1 ){
                mLightIsOff = true;
                Log.i("LightSensor", "light is off");
            }
            if (currentLight > 3 && mLightIsOff ){
                // the sensor sees some light. turn on the screen!
                displayView(Constants.WAKE);
            }
        }
    }
}