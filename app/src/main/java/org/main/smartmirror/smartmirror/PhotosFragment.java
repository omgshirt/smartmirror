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

import org.json.JSONObject;

import java.util.ArrayList;


public class PhotosFragment extends Fragment implements CacheManager.CacheListener{

    public static ImageView mPhotoFromPicasa;
    public static ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    public static ArrayList<String> mAlbumIdList = new ArrayList<String>();

    public static CacheManager mCacheManager = null;
    public static final String PHOTO_CACHE = "photo cache";
    public static final int DATA_UPDATE_FREQUENCY = 86400000;

    PhotosASyncTask newASync = new PhotosASyncTask(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mCacheManager = CacheManager.getInstance();
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        //new PhotosASyncTask(getActivity()).execute();
        return view;
    }

    public void startPhotosUpdate() {
        Log.i(Constants.TAG, "starting photos update");
        new PhotosASyncTask(getActivity()).execute();
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
            newASync.renderPhotos();
            if (mCacheManager.isExpired(PHOTO_CACHE)) {
                Log.i(Constants.TAG, PHOTO_CACHE + " expired. Refreshing...");
                startPhotosUpdate();
            }
        }
    }

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
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
    }


    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(PHOTO_CACHE)) startPhotosUpdate();
        Log.i("PHOTO CACHE", "updating expired cache" + cacheName);

    }

    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startTwitterUpdate() will refresh the views.
    }

}
