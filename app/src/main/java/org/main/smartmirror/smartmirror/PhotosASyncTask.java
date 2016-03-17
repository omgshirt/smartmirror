package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;

import com.squareup.picasso.Picasso;


public class PhotosASyncTask extends AsyncTask<String, Void, String> {


    protected String doInBackground(String[] params) {
        try {
            //Picasso.with(MainActivity.getContextForApplication()).load(PhotosFragment.mImageUrlList.get(0)).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);
        } catch (Exception e) {e.printStackTrace();}
        return "success";
    }

    protected void onPostExecute(String message) {

    }


}