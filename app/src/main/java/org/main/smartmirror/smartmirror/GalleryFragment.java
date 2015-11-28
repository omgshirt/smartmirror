package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment that displays images located on this application.
 * These images cycle for a predefined number of seconds.
 */
public class GalleryFragment extends Fragment {

    private ArrayList<String> mImageList;
    private Drawable mImageDrawable;
    private ImageView mGalleryItem;
    private Runnable mRunnable;
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // instantiate the timer object
        mTimer = new Timer();

        // initialize the runnable that will handle the task
        mRunnable = new Runnable() {
            @Override
            public void run() {
                makeRandomImage(mImageList.size());
                mGalleryItem.setImageDrawable(mImageDrawable);
            }
        };
        // initialize the timer task that will run on the UI
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        // get the assets
        String[] imageGalleryNames = null;
        AssetManager assetManager = getContext().getAssets();
        try {
            imageGalleryNames = assetManager.list("gallery");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // initialize the image list to be used throughout
        mImageList = new ArrayList<>(Arrays.asList(imageGalleryNames));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);
        // initialize the ImageView
        mGalleryItem = (ImageView) view.findViewById(R.id.gallery_item);
        // start the timer
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 5000);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // kill the timer
        mTimer.cancel();
    }

    /**
     * Method that handles the randomization of the image and assigns a drawable
     * to be painted on the UI
     * @param num the random number seed
     */
    public void makeRandomImage(int num){
        //TODO make sure that the images are truly random (they don't repeat)
        Random imageRandomizer = new Random();
        int randomNumber = imageRandomizer.nextInt(num);
        try {
            InputStream is = getContext().getAssets().open("gallery/" + mImageList.get(randomNumber));
            mImageDrawable = Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("News", "Got message:\"" + message + "\"");
            switch(message){
                case MainActivity.BACK:
                    getFragmentManager().popBackStack();
                    break;

            }

        }
    };

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    @Override
    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }
}
