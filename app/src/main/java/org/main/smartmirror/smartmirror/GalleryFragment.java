package org.main.smartmirror.smartmirror;


import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

/**
 * Fragment that displays a gallery using images located on this application
 */
public class GalleryFragment extends Fragment {

    private ArrayList<String> mImageList;
    private Drawable mImageDrawable;
    private ImageView mGalleryItem;
    private int mRandomNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] imageGalleryNames = null;
        // get the assets
        AssetManager assetManager = getContext().getAssets();
        try {
            imageGalleryNames = assetManager.list("gallery");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mImageList = new ArrayList<>(Arrays.asList(imageGalleryNames));

        Random imageRandomizer = new Random();
        mRandomNumber = imageRandomizer.nextInt(mImageList.size());

        try {
            InputStream is = getContext().getAssets().open("gallery/" + mImageList.get(mRandomNumber));
            mImageDrawable = Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);
        mGalleryItem = (ImageView) view.findViewById(R.id.gallery_item);
        mGalleryItem.setImageDrawable(mImageDrawable);
        return view;
    }


}
