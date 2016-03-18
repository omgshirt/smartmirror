package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;



public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    String userName = "smartmirrortesting@gmail.com";
    private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";



    protected String doInBackground(String[] params) {

        return "success";
    }

    protected void onPostExecute(String message) {

    }

}
