package org.main.smartmirror.smartmirror;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;


/**
 * Fragment displays several music streaming stations, the currently selected station and its
 * status (drwPlay / drwPause / stop)
 */
public class MusicFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String GENRE = "genre";
    private static final String TAG = "SmartMirror music";

    private HashMap<String, ImageView> mStationIcons;
    private MediaPlayer mMediaPlayer;
    private String mGenre;
    private HashMap<String, String> mUrlMap;

    private TextView txtStreamInfo;
    private AudioManager audioManager;

    public MusicFragment() {
    }

    public static MusicFragment NewInstance(String command) {
        Bundle args = new Bundle();
        String stationGenre = GetGenreFromCommand(command);
        Log.i(TAG, "creating new MusicStreamFragment :: " + stationGenre);
        args.putString(GENRE, stationGenre);
        MusicFragment msf = new MusicFragment();
        msf.setArguments(args);
        return msf;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d(Constants.TAG, "MusicStreamFragment got message:\"" + message + "\"");
            switch (message) {
                case Constants.PAUSE:
                    pauseStream();
                    break;
                case Constants.PLAY:
                    playStream();
                    break;
                case Constants.STOP:
                    stopStream();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.music_fragment, container, false);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "failed to get audio focus");
        }

        // configure the StreamInfo marquee. Doesn't work properly when set via XML...
        txtStreamInfo = (TextView) view.findViewById(R.id.stream_info);
        txtStreamInfo.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtStreamInfo.setSingleLine(true);
        txtStreamInfo.setMarqueeRepeatLimit(-1);

        // Create the mapping of genre -> URL from data in arrays.xml
        String[] stationNames = getResources().getStringArray(R.array.station_names);
        String[] stationUrls = getResources().getStringArray(R.array.station_urls);

        mGenre = getArguments().getString(GENRE);

        // set up the station list using R.layout.station_name
        mUrlMap = new HashMap<>(10);
        mStationIcons = new HashMap<>(10);

        for (int i = 0; i < stationNames.length; i++) {
            String genre = ConvertStationNameToGenre(stationNames[i]);
            mUrlMap.put(genre, stationUrls[i]);

            LinearLayout layStationInfo = createStationLayout(inflater, container, stationNames[i]);
            view.addView(layStationInfo);

            // save a reference to the icon so we can change it later
            ImageView icon = (ImageView) layStationInfo.findViewById(R.id.station_play_icon);
            mStationIcons.put(genre, icon);
        }

        // drwPlay a station if one has been selected
        if (!mGenre.isEmpty()) {
            setStatusIconVisibility();
            initMediaPlayer();
        } else {
            // create a media player to prevent crashes on callbacks
            mMediaPlayer = new MediaPlayer();
        }

        return view;
    }

    public LinearLayout createStationLayout(LayoutInflater inflater, ViewGroup container, String name) {
        LinearLayout stationLayout = (LinearLayout) inflater.inflate(R.layout.station_layout, container, false);
        TextView stationName = (TextView) stationLayout.findViewById(R.id.station_name);
        stationName.setText(name);

        return stationLayout;
    }

    // strips extra info from station labels, returning the genre
    public static String ConvertStationNameToGenre(String stationName) {
        int index = stationName.indexOf(":");
        return stationName.substring(0, index).toLowerCase();
    }

    // strip the "play " prefix from music or remote commands, return genre name
    public static String GetGenreFromCommand(String command) {
        int index = command.lastIndexOf(" ");
        return command.substring(index + 1, command.length());
    }

    /**
     * Open a music stream based on the provided command. Closes any existing MediaPlayer instances
     * and immediately starts the selected stream. Ignored if music is currently streaming
     *
     * @param command the mirror command, e.g. "play rock"
     */
    public void changeToStation(String command) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) return;
        startStation(command);
    }

    /** Like changeToStation, but allows changes if the media player is playing.
     *
     * @param command station to play
     */
    public void startStation(String command) {
        mGenre = GetGenreFromCommand(command);
        setStatusIconVisibility();
        setIconDrawables(R.drawable.play);
        tearDownMediaPlayer();
        initMediaPlayer();
    }

    public void pauseStream() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setIconDrawables(R.drawable.pause);
            setStatusIconVisibility();
            txtStreamInfo.setText(R.string.stream_pause);
        }
    }

    public void stopStream() {
        if (mMediaPlayer != null) {
            tearDownMediaPlayer();
            setIconDrawables(R.drawable.pause);
            mGenre = "";
            setStatusIconVisibility();
            txtStreamInfo.setText(R.string.stream_stopped);
        }
    }

    public void playStream() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            ((MainActivity)getActivity()).setVoiceCommandMode(true);
            mMediaPlayer.start();
            setIconDrawables(R.drawable.play);
            setStatusIconVisibility();
            txtStreamInfo.setText(R.string.stream_playing);
        }
    }


    // sets the status icon to visible for the currently selected stream
    public void setStatusIconVisibility() {
        for (String key : mStationIcons.keySet()) {
            ImageView img = mStationIcons.get(key);
            if (key.equals(mGenre)) {
                img.setVisibility(View.VISIBLE);
            } else {
                img.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setIconDrawables(int resId) {
        for (ImageView iv : mStationIcons.values()){
            iv.setImageResource(resId);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        txtStreamInfo.setText(R.string.stream_stopped);
        txtStreamInfo.setSelected(true);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        stopStream();
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void tearDownMediaPlayer() {
        if (mMediaPlayer == null) return;

        if (mMediaPlayer.isPlaying()) {
            Log.i(TAG, "stopping music");
            mMediaPlayer.stop();
        }

        ((MainActivity)getActivity()).setVoiceCommandMode(false);
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * Initialize media player and start playback when initialized.
     */
    public void initMediaPlayer() {
        Log.i(TAG, "opening stream for: " + mGenre);
        try {
            txtStreamInfo.setText(R.string.stream_preparing);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getUrlFromGenre());
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            txtStreamInfo.setText(R.string.stream_not_found);
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            txtStreamInfo.setText(R.string.stream_not_found);
            Log.i(TAG, "illegal state exception - probably thrown by DataSource url");
        }
    }

    /**
     * Callback for MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "media player error code: " + what + " extra: " + extra);
        return true;
    }

    /**
     * Callback for MediaPlayer.OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        ((MainActivity)getActivity()).setVoiceCommandMode(true);
        txtStreamInfo.setText(R.string.stream_playing);
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
