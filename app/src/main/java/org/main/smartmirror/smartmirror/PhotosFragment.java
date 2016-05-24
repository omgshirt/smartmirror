package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class PhotosFragment extends Fragment implements CacheManager.CacheListener{

    public static ImageView mPhotoFromPicasa;
    public static ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    public static ArrayList<String> mAlbumIdList = new ArrayList<String>();

    public static CacheManager mCacheManager = null;
    public static final String PHOTO_CACHE = "photo cache";
    public static final int DATA_UPDATE_FREQUENCY = 86400000;

    private PhotosASyncTask mAsyncTask;
    private Preferences mPreference;
    private static TimerTask mTimerTask;
    private Timer mTimer;
    private Runnable mRunnable;
    private int mCurrentPhoto = 0;

    // length of time to display image (in ms)
    private int imageDisplayTime = 20000;
    static MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
        if (!mPreference.isLoggedInToGmail()) {
            removePhotos();
        }
        mainActivity = new MainActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mCacheManager = CacheManager.getInstance();
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        return view;
    }

    public void startPhotosUpdate() {
        Log.i(Constants.TAG, "starting photos update");
        mAsyncTask = new PhotosASyncTask(getActivity(), mPreference.getUserId(), mPreference.getUsername());
        mAsyncTask.execute();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Check for any cached photos data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.

        if (!mCacheManager.containsKey(PHOTO_CACHE)) {
            Log.i(Constants.TAG, PHOTO_CACHE + " does not exist, creating");
            startPhotosUpdate();
        } else {
            //new PhotosASyncTask(getActivity(), mPreference.getUserId(), mPreference.getUsername()).execute();
            renderPhotos();
            if (mCacheManager.isExpired(PHOTO_CACHE)) {
                Log.i(Constants.TAG, PHOTO_CACHE + " expired. Refreshing...");
                startPhotosUpdate();
            }
        }
    }

    /**
     * When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     * We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     * voice recognition, the remote control, etc.
     */
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
        mCacheManager.registerCacheListener(PHOTO_CACHE, this);
        Log.i("PHOTO CACHE", "register photos");
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCacheManager.unRegisterCacheListener(PHOTO_CACHE, this);
        Log.i("PHOTO CACHE", "unregister photos");
        try {
            mTimerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            mTimerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mTimerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(PHOTO_CACHE)) {
            try {
                //mAsyncTask.cancel(true);
                mTimerTask.cancel();
                mImageUrlList.clear();
                mAlbumIdList.clear();
                startPhotosUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Log.i("PHOTO CACHE", "updating expired cache" + cacheName);

    }

    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startPhotosUpdate() will refresh the views.
    }

    /**
     * Removes photos fragment, speaks error, and displays error message
     */
    private void removePhotos() {
        ((MainActivity) getActivity()).removeFragment(Constants.PHOTOS);
        ((MainActivity) getActivity()).displayNotSignedInFragment(Constants.PHOTOS, true);
        ((MainActivity) getActivity()).speakText(getResources().getString(R.string.speech_not_logged_in_err));
    }

    public void renderPhotos() {
        try {
            mTimer = new Timer();
            // initialize the runnable that will handle the task
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    //Log.i("PHOTOS Fragment ","Load photos");
                    try {
                        Picasso.with(getActivity()).load(mImageUrlList.
                                get(mCurrentPhoto)).fit().centerInside().into(mPhotoFromPicasa);
                        mCurrentPhoto++;
                        if (mCurrentPhoto > mImageUrlList.size() - 1) {
                            mCurrentPhoto = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            // initialize the timer task that will run on the UI
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //Log.i("PHOTOS Fragment ","Creating new timer task");
                    /*if (getActivity() == null)
                        return;*/
                    mainActivity.runOnUiThread(mRunnable);
                }
            };
            mTimer.scheduleAtFixedRate(mTimerTask, 0, imageDisplayTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}