package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    private ArrayList<String> mImageFileNames;
    private ArrayList<String> mImageList;
    private Drawable mImageDrawable;
    private ImageView mGalleryItem;
    private Runnable mRunnable;
    private String mArtist;
    private String mTitle;
    private String mYear;
    private TextView mImageTitle;
    private TextView mImageArtist;
    private TextView mImageYear;
    private Timer mTimer;
    private TimerTask mTimerTask;

    /**
     * Taken from Apache Commons IO https://commons.apache.org/proper/commons-io/xref/org/apache/commons/io/FilenameUtils.html
     * Gets the base name, minus the full path and extension, from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash and before the last dot is returned.
     <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */

    public static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    public static String getBaseName(final String filename) {
        return removeExtension(getName(filename));
    }
    public static String getName(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    public static String removeExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }
    public static int indexOfLastSeparator(final String filename) {
        if (filename == null) {
            return -1;
        }
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static int indexOfExtension(final String filename) {
        if (filename == null) {
            return -1;
        }
        final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    public void startExtensionRemoval(){
        for(int i=0; i<mImageList.size(); i++) {
            mImageFileNames.add(getBaseName(mImageList.get(i)));
            mImageFileNames.set(i, mImageFileNames.get(i).replace('_', ' '));
            mImageFileNames.set(i, mImageFileNames.get(i).replace('-', '\n'));
        }

    }

    // ---------------------------- FIle Extension Removal -------------------------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // start the timer
        mTimer = new Timer();

        // initialize the runnable that will handle the task
        mRunnable = new Runnable() {
            @Override
            public void run() {
                makeRandomImage(mImageList.size());
                mGalleryItem.setImageDrawable(mImageDrawable);
                mImageTitle.setText(mTitle);
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
        mImageFileNames = new ArrayList<>();

        // remove image extensions
        startExtensionRemoval();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);
        // initialize the ImageView
        mImageTitle = (TextView) view.findViewById(R.id.image_facts);
        mGalleryItem = (ImageView) view.findViewById(R.id.gallery_item);

        // initialize the timer task that will run on the UI
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 5000);

        return view;
    }

    @Override
    public void onStop(){
        super.onStop();
        mTimerTask.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mTimerTask.cancel();
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
        mTitle = mImageFileNames.get(randomNumber);
        Log.i("IMG", mTitle);
        try {
            InputStream is = getContext().getAssets().open("gallery/" + mImageList.get(randomNumber));
            mImageDrawable = Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
