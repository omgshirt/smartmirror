package org.main.smartmirror.smartmirror;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
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
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private final boolean DEBUG=true;
    private final String NEWS = "News";
    private final String CALENDAR = "Calendar";
    private final String WEATHER = "Weather";
    private final String LIGHT = "Light";
    private final String SETTINGS = "Settings";
    private final String OFF="Off";
    private TTSHelper mTTSHelper;
    private static Context mContext; // Hold the app context
    private Preferences mPreferences;
    /*private String mDefaultURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3Asports&" +
            "begin_date=20151028&end_date=20151028&sort=newest&fl=headline%2Csnippet&page=0&api-key=";*/
    /*private String mSportsURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=sports&sort=newest&api-key=";
    private String mTechURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=technology&sort=newest&api-key=";*/
    private String mDefaultURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";
    private String mPreURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3A";
    private String mPostURL = "&sort=newest&api-key=";
    private String mNewsDesk;
    private String mNewsDefault = "http://api.nytimes.com/svc/search/v2/articlesearch.json?fq=news_desk%3AU.S.&sort=newest&api-key=";
    private String mNYTURL = mPreURL + mNewsDesk + mPostURL;

    // WiFiP2p
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDeviceList mDeviceList;
    private WifiP2pInfo mInfo;
    private BroadcastReceiver mWifiReceiver;
    private IntentFilter mIntentFilter;
    public final static int PORT = 8888;
    public final static int SOCKET_TIMEOUT = 500;
    private String mOwnerIP;

    //Speech
    private Messenger mMessenger = new Messenger(new IHandler());
    private boolean mIsBound;
    private Messenger mService;

    // used to esbalish a service connection
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
        // Get authorization on >= 23
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite( getApplicationContext() )) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, 1);
            }
            if(!Settings.System.canWrite(getApplicationContext() )) {
                Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
                startActivityForResult(intent, 1);
            }
        }*/

        // initialize TTS
        mTTSHelper = new TTSHelper(this);

        // Initialize WiFiP2P services
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        discoverPeers();

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

        // start with weather displayed
        displayView(WEATHER);
    }

    @Override
    public void onResume(){
        super.onResume();
        mPreferences.setAppBrightness(this);
        mWifiReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mWifiReceiver, mIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mWifiReceiver);
    }

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
        stopTTS();                                      // shut down any pending TTS
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
                title= SETTINGS;
                break;
            case OFF:
                fragment = new OffFragment();
                title = OFF;
                break;
        }
        if(fragment != null){
            if(DEBUG)
                Log.i("Fragments", "Displaying " + viewName);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            if (!isFinishing() ) {
                ft.commit();
            }
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

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
                        Log.i("news desk: ", Arrays.toString(urlArr));
                        Log.i("I heard: ", voiceInput);
                    }
                }
            }
            if(DEBUG)
                Log.i("I heard: ", voiceInput);
            if (voiceInput.contains(mNewsDesk.toLowerCase())) {
                startTTS(mNewsDesk);
                displayView(NEWS);
            } else if (voiceInput.contains(CALENDAR.toLowerCase())) {
                startTTS(CALENDAR);
                displayView(CALENDAR);
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
            } else if(voiceInput.contains(OFF.toLowerCase())){
                displayView(OFF);
            }
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Didn't catch that",
                    Toast.LENGTH_LONG).show();
            startTTS("Speak proper English or bugger off you bloody yank");
        }
    }
    
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
                    Thread.sleep(2000);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTSHelper.destroy();
        mPreferences.destroy();
        unbindService(mConnection);
        mIsBound=false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, VoiceService.class), mConnection, BIND_AUTO_CREATE);
        mIsBound=true;
    }

    public static Context getContextForApplication() {
        return mContext;
    }


    // calls the P2pManager to refresh peer list
    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
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
        mDeviceList = peers;
    }


    /** called when a connection is made to this device
     *
     * @param info
     */
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // make this the group owner and start the server to listen
        Toast.makeText(this, "Remote Connected", Toast.LENGTH_SHORT).show();
        if(DEBUG)
            Log.i("Wifi", "Connection info: " + info.toString());
        mInfo = info;
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 15;
        if (info.groupFormed && info.isGroupOwner) {
            if(DEBUG)
                Log.i("Wifi", "onConnectionInfo is starting server...");
            new RemoteServerAsyncTask(this).execute();
        } else if (info.groupFormed){
        }
    }
}
