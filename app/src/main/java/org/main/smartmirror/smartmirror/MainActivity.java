package org.main.smartmirror.smartmirror;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static Context mContext; // Hold the app context
    private Preferences mPreferences;
    private String mSpeechText;
    private String[] mFragments = {"news","calendar","weather","sports","settings","preferences","light"};
    private TextToSpeach mTts;
    private int RESULT_SPEECH = 1;
    private Thread mSpeechThread;


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

        mTts = new TextToSpeach(this);
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
        displayView(R.id.nav_weather);
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
            displayView(id);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId){
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        if (mTts != null) mTts.stop();      // shut down any pending TTS

        switch (viewId) {
            case R.id.nav_news:
                fragment = new NewsFragment();
                title = "News";
                break;
            case R.id.nav_calendar:
                fragment = new CalendarFragment();
                title = "Calendar";
                break;
            case R.id.nav_light:
                fragment = new LightFragment();
                title = "Night Light";
                break;
            case R.id.nav_weather:
                fragment = new WeatherFragment();
                title = "Weather";
                break;
            case R.id.nav_sports:
                fragment = new SportsFragment();
                title = "Sports";
                break;
            case R.id.action_settings:
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                title= "Settings";
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        String word = "";
        Fragment fragment = null;
        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSpeechText = matches.get(0);
        }
        word = mSpeechText;
        Log.i("VOICE", word);           // print voice results to LogCat
        String response;

        if(word.contains("show")) {
            response = "showing ";
            if (word.contains(mFragments[0])) {
                startVoice(response + mFragments[0]);
                displayView(R.id.nav_news);
            } else if (word.contains(mFragments[1])) {
                startVoice(response + mFragments[1]);
                displayView(R.id.nav_calendar);
            } else if (word.contains(mFragments[2])) {
                startVoice(response + mFragments[2]);
                displayView(R.id.nav_weather);
            } else if (word.contains(mFragments[3])) {
                startVoice(response + mFragments[3]);
                displayView(R.id.nav_sports);
            } else if (word.contains(mFragments[4]) || word.contains(mFragments[5]) ) {
                startVoice(response + mFragments[4]);
                displayView(R.id.action_settings);
            } else if (word.contains(mFragments[6]) ) {
                startVoice(response + mFragments[6]);
                displayView(R.id.nav_light);
            }
        }
    }

    public void StartVoiceRecognitionActivity(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
            t.show();
            //try text input here if voice not available
            Log.d("TEXTTOSPEECH", "voice to text in voice to text: " + mSpeechText);

        }
    }

    public void startVoice(final String phrase){
        if (mTts != null) {
            mTts.stop();
        }
        mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTts.speakText(phrase);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSpeechThread.start();
    }

    protected void onDestroy() {
        if (mTts != null) {
            mTts.stop();
        }
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        mPreferences.destroy();
        super.onDestroy();
    }

    public static Context getContextForApplication() {
        return mContext;
    }

}
