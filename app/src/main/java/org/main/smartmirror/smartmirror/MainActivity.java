package org.main.smartmirror.smartmirror;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    private String[] mFragments = {"news","calendar","weather","sports","settings","preferences"};
    private TextToSpeech mTts;
    private int RESULT_SPEECH = 1;
    private Thread mSpeechThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load any application preferences. If prefs do not exist, set them to defaults
        mContext = getApplicationContext();
        mPreferences = Preferences.getInstance();

        mTts = new TextToSpeech(this);
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

        // start with weather displayed
        displayView(R.id.nav_weather);
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
       /* int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
    }

    public void displayView(int viewId){
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {
            case R.id.nav_news:
                fragment = new NewsFragment();
                title = "News";
                break;
            case R.id.nav_calendar:
                fragment = new CalendarFragment();
                title = "Calendar";
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
        String word;
        Fragment fragment = null;
        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSpeechText = matches.get(0);
        }
        word = mSpeechText;
        Log.i("VOICE", word);           // print voice results to LogCat

        if(word.contains("show")) {
            if (word.contains(mFragments[0])) {
                startVoice(mFragments[0]);
                displayView(R.id.nav_news);
            } else if (word.contains(mFragments[1])) {
                startVoice(mFragments[1]);
                displayView(R.id.nav_calendar);
            } else if (word.contains(mFragments[2])) {
                startVoice(mFragments[2]);
                displayView(R.id.nav_weather);
            } else if (word.contains(mFragments[3])) {
                startVoice(mFragments[3]);
                displayView(R.id.nav_sports);
            } else if (word.contains(mFragments[4]) || word.contains(mFragments[5]) ) {
                startVoice(mFragments[4]);
                displayView(R.id.action_settings);
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

    private void startVoice(final String word){
        final String response = "showing " + word;
        mSpeechThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTts.speakText(response);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSpeechThread.start();
    }

    public static Context getContextForApplication() {
        return mContext;
    }
}
