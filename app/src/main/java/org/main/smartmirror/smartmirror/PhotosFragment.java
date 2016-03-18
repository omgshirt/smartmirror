package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class PhotosFragment extends Fragment {

    public static ImageView mPhotoFromPicasa;
    int currentPhoto = 0;
    Handler mTimerHandler = new Handler();
    int mDelay = 5000; //milliseconds
    PhotosASyncTask newASync = new PhotosASyncTask();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        new PhotosASyncTask().execute();
        //updatePhoto();
        return view;
    }

    public void updatePhoto() {
        while (currentPhoto < 3) {
            mTimerHandler.postDelayed(new Runnable(){
                public void run(){
                    System.out.println("CYCLING NEXT PHOTO");
                    //Picasso.with(MainActivity.getContextForApplication()).load(PhotosASyncTask.mImageUrlList.get(currentPhoto)).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);

                    //newASync.onPostExecute(Integer.toString(currentPhoto));
                    currentPhoto++;
                    if(currentPhoto > 3) currentPhoto = 0;
                    mTimerHandler.postDelayed(this, mDelay);
                }
            }, mDelay);
        }
    }

}
