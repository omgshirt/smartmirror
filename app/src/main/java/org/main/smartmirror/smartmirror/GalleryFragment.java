package org.main.smartmirror.smartmirror;


import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment that displays a gallery using images located on this application
 */
public class GalleryFragment extends Fragment {

    private ArrayList<String> mImageList;
    private Drawable mImageDrawable;
    private ImageView mGalleryItem;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Runnable mRunnable;

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
        Random imageRandomizer = new Random();
        int randomNumber = imageRandomizer.nextInt(num);
        try {
            InputStream is = getContext().getAssets().open("gallery/" + mImageList.get(randomNumber));
            mImageDrawable = Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
