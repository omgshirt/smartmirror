package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String NEWS = "News";
    private final String CALENDAR = "Calendar";
    private final String WEATHER = "Weather";
    private final String SPORTS = "Sports";
    private final String LIGHT = "Light";
    private final String SETTINGS = "Settings";
    private TTSHelper mTextToSpeech;
    private static Context mContext; // Hold the app context
    private Preferences mPreferences;
    private Thread mSpeechThread;
    private SpeechRecognizer mSpeechRecognizer;
    private SpeechRecognitionListener mRecognitionListener;
    private boolean mIsSpeaking;

    //5 second timer
    private CountDownTimer mCountDown = new CountDownTimer(12000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("Millis",": " + (millisUntilFinished/1000));
            Log.d("misSpeaking", " " + mIsSpeaking);
        }

        @Override
        public void onFinish() {
            if(mIsSpeaking == true){
                mCountDown.start();
            }
            else{
                launchSpeech();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load any application preferences. If prefs do not exist, set them to defaults
        mContext = getApplicationContext();

        // check for permission to write system settings on API 23 and greater.
        // Get authorization on >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite( getApplicationContext() )) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, 1);
            }
        }

        mPreferences = Preferences.getInstance();

        mTextToSpeech = new TTSHelper(this);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        mRecognitionListener = new SpeechRecognitionListener();
        super.onCreate(savedInstanceState);
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
                //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION    // commented out to keep nav buttons for testing
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        try {
            getSupportActionBar().hide();
        } catch (Exception e) {

        }

        // start with weather displayed
        displayView(WEATHER);
        mCountDown.start();
    }

    @Override
    public void onResume(){
        super.onResume();
        mPreferences.setAppBrightness(this);
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
        displayView(item.toString());
        return true;
    }

    public void displayView(String viewName){
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        if (mTextToSpeech != null) mTextToSpeech.Stop();      // shut down any pending TTS

        switch (viewName) {
            case NEWS:
                fragment = new NewsFragment();
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
            case SPORTS:
                fragment = new SportsFragment();
                title = SPORTS;
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                title= SETTINGS;
                break;
        }
        if(fragment != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void voiceResult(String voiceInput){
        if(voiceInput == null)
            Log.d("VoiceInput", "is NULL");
        else
            Log.d("VoiceInput", "is NOT NULL");
        if(voiceInput != null) {
            if (voiceInput.contains("show")) {
                if (voiceInput.contains(NEWS.toLowerCase())) {
                    startVoice(NEWS);
                    displayView(NEWS);
                } else if (voiceInput.contains(CALENDAR.toLowerCase())) {
                    startVoice(CALENDAR);
                    displayView(CALENDAR);
                } else if (voiceInput.contains(WEATHER.toLowerCase())) {
                    startVoice(WEATHER);
                    displayView(WEATHER);
                } else if (voiceInput.contains(SPORTS.toLowerCase())) {
                    startVoice(SPORTS);
                    displayView(SPORTS);
                } else if (voiceInput.contains(LIGHT.toLowerCase())) {
                    startVoice(LIGHT);
                    displayView(LIGHT);
                } else if (voiceInput.contains(SETTINGS.toLowerCase())) {
                    startVoice(SETTINGS);
                    displayView(SETTINGS);
                }
            }
        }
    }

    /**
     * launches the speech recognition intent with the custon listener
     */
    public void launchSpeech(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mSpeechRecognizer.startListening(intent);
    }

    public void startVoice(final String phrase){
        if (mTextToSpeech != null) {
            mTextToSpeech.Stop();
        }
        mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTextToSpeech.speakText(phrase);
                    mIsSpeaking=true;
                    Thread.sleep(2000);
                    mIsSpeaking=false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSpeechThread.start();
    }

    protected void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.Stop();
        }
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        mPreferences.destroy();
        mSpeechRecognizer.destroy();
        mRecognitionListener=null;
        mCountDown.cancel();
        super.onDestroy();
    }

    public static Context getContextForApplication() {
        return mContext;
    }
    //TODO Make this a separate java class
    public class SpeechRecognitionListener implements RecognitionListener {

        private String mSpokenCommand;

        @Override
        public void onError(int error) {
            if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH){
                launchSpeech();
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(matches != null) {
                setSpokenCommand(matches.get(0));
                voiceResult(getSpokenCommand());
            }
            mCountDown.start();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        public void setSpokenCommand(String command){
            mSpokenCommand = command;
        }

        public String getSpokenCommand(){
            return mSpokenCommand;
        }
    }
}
