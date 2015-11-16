package org.main.smartmirror.smartmirror;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    // Globals, prefs, debug flags
    private final boolean DEBUG = true;

    private static Context mContext;
    private Preferences mPreferences;

    // Constants
    public static final String TAG = "SmartMirror";
    public static final String CALENDAR = "Calendar";
    public static final String CAMERA = "Camera";
    public static final String FACEBOOK = "Facebook";
    public static final String LIGHT = "Light";
    public static final String NEWS = "News";
    public static final String MUSIC = "Music";
    public static final String SETTINGS = "Settings";
    public static final String SLEEP = "Sleep";
    public static final String TRAFFIC = "Traffic";
    public static final String TWITTER = "Twitter";
    public static final String OFF = "Off";
    public static final String ON = "On";
    public static final String WAKE = "Wake";
    public static final String WEATHER = "Weather";
    public static final int SLEEPING = 0;
    public static final int LIGHT_SLEEP = 1;
    public static final int AWAKE = 2;

    // News
    private String mDefaultURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";
    private String mPreURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3A";
    private String mPostURL = "&sort=newest&api-key=";
    private String mNewsDesk;
    private String mNewsDefault = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";
    private String mNYTURL = mPreURL + mNewsDesk + mPostURL;

    // Sleep state & wakelocks
    // mirrorSleepState can be SLEEPING, LIGHT_SLEEP or AWAKE
    private int mirrorSleepState;
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

    // used to establish a service connection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);

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

    // handles the messages from Service to this
    public class IHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case VoiceService.RESULT_SPEECH:
                    String result = msg.getData().getString("result");
                    if(DEBUG)
                        Log.i("MAIN", result);
                    speechResult(result);
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
        mPreferences = Preferences.getInstance();

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

        // Set up view and nav drawer
        setContentView(R.layout.activity_main);
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
        Log.i(TAG, "onStart");
        bindService(new Intent(this, VoiceService.class), mConnection, BIND_AUTO_CREATE);
        mIsBound=true;
        mirrorSleepState = AWAKE;
        // if there's a fragment pending to display, show it
        if (mCurrentFragment != null) {
            displayView(mCurrentFragment);
        } else {
            // on first run mCurrentFragment isn't set: start with weather displayed
            displayView(WEATHER);
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        stopWifiHeartbeat();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
        mPreferences.setAppBrightness(this);
        mWifiReceiver = new WiFiDirectBroadcastReceiver(mWifiManager, mWifiChannel, this);
        registerReceiver(mWifiReceiver, mWifiIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
        startWifiHeartbeat();
        unregisterReceiver(mWifiReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mirrorSleepState = SLEEPING;
        Log.i(TAG, "onStop");
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
        mIsBound=false;
        Log.i(TAG, "onDestroy");
    }

    // -------------------------- SCREEN WAKE / SLEEP ---------------------------------

    protected void wakeScreen() {
        Log.i(TAG, "wakeScreen() called");
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
            displayView(item.toString());
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(DEBUG)
            Log.i("item selected", item.toString());
        displayView(item.toString());
        return true;
    }

    /**
     * Handles which fragment will be displayed to the user
     * @param viewName the name of the view to be displayed
     */
    public void displayView(String viewName){
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        // If sleeping, save viewName and wake screen.
        // displayView will be called again from onStart() with the fragment to show
        if (mirrorSleepState == SLEEPING) {
            if (!viewName.equals(SLEEP)) {
                mCurrentFragment = viewName;
                mirrorSleepState = AWAKE;
                wakeScreen();
            }
            return;
        }

        switch (viewName) {
            case NEWS:
                fragment = new NewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("url", mDefaultURL);
                fragment.setArguments(bundle);
                title = NEWS;
                break;
            case CALENDAR:
                fragment = new CalendarFragment();
                title = CALENDAR;
                break;
            case LIGHT:
                fragment = new LightFragment();
                title = LIGHT;
                break;
            case WEATHER:
                fragment = new WeatherFragment();
                title = WEATHER;
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                title = SETTINGS;
                break;
            case CAMERA:
                fragment = new CameraFragment();
                title = CAMERA;
                break;
            case WAKE:
                if (mirrorSleepState == LIGHT_SLEEP) {
                    mirrorSleepState = AWAKE;
                    displayView(mCurrentFragment);
                }
                return;
            case SLEEP:
                if (mirrorSleepState == AWAKE) {
                    fragment = new OffFragment();
                    mirrorSleepState = LIGHT_SLEEP;
                    title = SLEEP;
                } else { return; }
                break;
        }

        stopTTS();

        if(fragment != null){
            if(DEBUG)
                Log.i("Fragments", "Displaying " + viewName);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            if (!isFinishing()) {
                ft.commit();
                // Any command besides SLEEP or WAKE now sets the state to AWAKE
                if ( !(viewName.equals(SLEEP) || viewName.equals(WAKE)) ) {
                    mirrorSleepState = AWAKE;
                    mCurrentFragment = viewName;
                }
                Log.i(TAG, "mCurrentFragment:" + mCurrentFragment);
            } else {
                Log.i("Fragments", "commit skipped. isFinishing() returned true");
            }
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
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

    // ----------------------- SPEECH RECOGNITION --------------------------

    /**
     * Handles the result of the speech input
     * @param voiceInput the command the user gave
     */
    public void speechResult(String voiceInput) {
        try {
            String[] urlArr = getResources().getStringArray(R.array.nyt_news_desk);
            int i = 0;
            while (i < urlArr.length) {
                if (voiceInput.contains(urlArr[i].toLowerCase())) {
                    mNewsDesk = urlArr[i];
                    mNYTURL = mPreURL + mNewsDesk + mPostURL;
                    mDefaultURL = mNYTURL;
                    if(DEBUG)
                        Log.i("voice news desk: ", urlArr[i]);
                    break;
                } else {
                    i++;
                    if(DEBUG) {
                        //Log.i("news desk: ", Arrays.toString(urlArr));
                        //Log.i("I heard: ", voiceInput);
                    }
                }
            }
            if(DEBUG)
                Log.i("I heard: ", voiceInput);
            if (voiceInput.contains(CALENDAR.toLowerCase())) {
                startTTS(CALENDAR);
                displayView(CALENDAR);
            }else if(voiceInput.contains(CAMERA.toLowerCase())){
                startTTS(CAMERA);
                displayView(CAMERA);
            } else if (voiceInput.contains(WEATHER.toLowerCase())) {
                startTTS(WEATHER);
                displayView(WEATHER);
            } else if (voiceInput.contains(LIGHT.toLowerCase())) {
                startTTS(LIGHT);
                displayView(LIGHT);
            } else if (voiceInput.contains(SETTINGS.toLowerCase())) {
                startTTS(SETTINGS);
                displayView(SETTINGS);
            } else if (voiceInput.contains(NEWS.toLowerCase())) {
                startTTS(NEWS);
                mDefaultURL = mNewsDefault;
                displayView(NEWS);
            } else if(voiceInput.contains(OFF.toLowerCase()) || voiceInput.contains(SLEEP.toLowerCase())){
                displayView(SLEEP);
            } else if (voiceInput.contains(mNewsDesk.toLowerCase())) {
                startTTS(mNewsDesk);
                displayView(NEWS);
            }
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Didn't catch that",
                    Toast.LENGTH_LONG).show();
            startTTS("Didn't catch that");
        }
    }

    /**
     * Start the speech recognizer
     */
    public void startSpeechRecognition(){
        try {
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
            Message msg = Message.obtain(null, VoiceService.STOP_SPEECH);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
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
        if (mTTSHelper != null) {
            mTTSHelper.stop();
        }
    }

    // ------------------------------  WIFI P2P  ----------------------------------


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
        }
    }

    // Start a server socket: this will listen to commands from the remote control
    public void startRemoteServer() {
        mServerTask = new RemoteServerAsyncTask(this);
        mServerTask.execute();
    }

    // OnStop, start a thread that keeps the wifip2p connection alive by pinging every 60 seconds
    private void startWifiHeartbeat() {
        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(1);

        final Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                discoverPeers();
                Log.i("Wifi", "Heartbeat: discoverPeers()" );
            }
        };
        wifiHeartbeat = scheduler.scheduleAtFixedRate(heartbeatTask, 60, 60,
                TimeUnit.SECONDS);
    }

    // Stop the heartbeat thread
    public void stopWifiHeartbeat() {
        if (wifiHeartbeat != null) {
            wifiHeartbeat.cancel(true);
        }
    }
}
