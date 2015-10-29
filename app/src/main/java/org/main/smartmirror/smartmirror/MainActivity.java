package org.main.smartmirror.smartmirror;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String NEWS = "News";
    private final String CALENDAR = "Calendar";
    private final String WEATHER = "Weather";
    private final String SPORTS = "Sports";
    private final int RESULT_SPEECH = 1;
    private TextToSpeach mTextToSpeach;
    private Thread mSpeechTread;
    private Messenger mMessenger = null;
    private boolean mBound;
    private ServiceConnection mVoiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTextToSpeach = new TextToSpeach(this);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DisplayView(item.getItemId());
        return true;
    }

    public void DisplayView(int viewId){
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {
            case R.id.nav_news:
                fragment = new NewsFragment();
                title = NEWS;
                break;
            case R.id.nav_calendar:
                fragment = new CalendarFragment();
                title = CALENDAR;
                break;
            case R.id.nav_weather:
                fragment = new WeatherFragment();
                title = WEATHER;
                break;
            case R.id.nav_sports:
                fragment = new SportsFragment();
                title = SPORTS;
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
        String voiceInput = null;
        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            voiceInput = matches.get(0);
        }
        if(voiceInput != null) {
            if (voiceInput.contains("show")) {
                if (voiceInput.contains(NEWS.toLowerCase())) {
                    StartVoice(NEWS);
                    DisplayView(R.id.nav_news);
                } else if (voiceInput.contains(CALENDAR.toLowerCase())) {
                    StartVoice(CALENDAR);
                    DisplayView(R.id.nav_calendar);
                } else if (voiceInput.contains(WEATHER.toLowerCase())) {
                    StartVoice(WEATHER);
                    DisplayView(R.id.nav_weather);
                } else if (voiceInput.contains(SPORTS.toLowerCase())) {
                    StartVoice(SPORTS);
                    DisplayView(R.id.nav_sports);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, VoiceService.class), mVoiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mVoiceConnection);
            mBound = false;
        }
    }

    public void StartVoiceRecognitionActivity(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast tstNoSupport = Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
            tstNoSupport.show();
        }
    }

    private void StartVoice(final String word){
        final String response = "showing " + word;
        mSpeechTread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTextToSpeach.SpeakText(response);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSpeechTread.start();
    }
}
