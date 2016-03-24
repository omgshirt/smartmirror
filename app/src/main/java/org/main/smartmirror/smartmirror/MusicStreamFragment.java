package org.main.smartmirror.smartmirror;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Fragment displays several music streaming stations, the currently selected station and its
 * status (drwPlay / drwPause / stop)
 */
public class MusicStreamFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
            AudioManager.OnAudioFocusChangeListener {

    private static final String GENRE = "genre";
    private static final String TAG = "music streaming";

    private LinearLayout layMusicStream;
    private List<TextView> txtStationList;
    private MediaPlayer mMediaPlayer;
    private String mGenre;
    private HashMap<String, String> mUrlMap;
    private AudioManager audioManager;

    public MusicStreamFragment() {
    }

    public static MusicStreamFragment NewInstance(String command) {
        Bundle args = new Bundle();

        int index = command.lastIndexOf(" ");
        command = command.substring( index+1, command.length() );

        args.putString(GENRE, command);
        MusicStreamFragment msf = new MusicStreamFragment();
        msf.setArguments(args);
        return msf;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_stream_fragment, container, false);
        layMusicStream = (LinearLayout) view;

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "failed to get audio focus");
        }

        // Create the mapping of genre -> URL from data in arrays.xml
        String[] stationNames = getResources().getStringArray(R.array.station_names);
        String[] stationUrls = getResources().getStringArray(R.array.station_urls);
        txtStationList = new ArrayList<>();

        mGenre = getArguments().getString(GENRE);

        // set up the station list using R.layout.station_name
        mUrlMap = new HashMap<>(10);

        for(int i =0; i < stationNames.length; i++){
            String genre = convertStationNameToGenre(stationNames[i]);
            mUrlMap.put(genre, stationUrls[i]);
            TextView stationName = (TextView) View.inflate(getActivity(), R.layout.station_name, null);
            stationName.setText(stationNames[i]);
            txtStationList.add(stationName);
            layMusicStream.addView(stationName);
            if (genre.equals(mGenre)) {
                stationName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play, 0, 0, 0);
            } else {

                stationName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }

        // drwPlay a station if one has been selected
        if (!mGenre.isEmpty()) {
            initMediaPlayer();
        }

        return view;
    }

    public String convertStationNameToGenre(String stationName) {
        int index = stationName.indexOf(":");
        return stationName.substring(0, index).toLowerCase();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        tearDownMediaPlayer();
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    public void tearDownMediaPlayer(){
        if (mMediaPlayer == null) return;

        if (mMediaPlayer.isPlaying()) {
            Log.i(TAG, "stopping music");
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * Initialize media player and start playback when initialized.
     */
    public void initMediaPlayer() {
        Log.i(TAG, "opening stream for: " + mGenre);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getUrlFromGenre());
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (getActivity() instanceof MainActivity) {
                String msg = getResources().getString(R.string.stream_not_found);
                ((MainActivity) getActivity()).showToast(msg, Toast.LENGTH_LONG);
            }
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            Log.i(TAG, "illegal state exception - probably thrown by DataSource url");
        }
    }

    /**
     * Callback for MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "media player error code: " + what + " extra: " +extra);
        return true;
    }

    /**
     * Callback for MediaPlayer.OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared");
        mp.start();
    }

    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public String getUrlFromGenre() {
        return mUrlMap.get(mGenre);
    }
}
