package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private ImageView mGalleryItem;
    private Runnable mRunnable;
    private Timer mTimer;
    private TimerTask mTimerTask;

    // length of time to show each image (in ms)
    private int imageDisplayTime = 20000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // start the timer
        mTimer = new Timer();

        // initialize the runnable that will handle the task
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Drawable drawable = makeRandomImage(mImageList.size());
                mGalleryItem.setImageDrawable(drawable);
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

        // initialize the timer task that will run on the UI
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, imageDisplayTime);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimerTask.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimerTask.cancel();
    }

    /**
     * Method that handles the randomization of the image and assigns a drawable
     * to be painted on the UI
     *
     * @param num the random number seed
     */
    public Drawable makeRandomImage(int num) {
        //TODO make sure that the images are truly random (they don't repeat) {Random images would repeat}
        Random imageRandomizer = new Random();
        int randomNumber = imageRandomizer.nextInt(num);
        try {
            InputStream is = getContext().getAssets().open("gallery/" + mImageList.get(randomNumber));
           return Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
