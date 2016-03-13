package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.UserFeed;

import java.net.URL;


public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    String userName = "smartmirrortesting@gmail.com";
    PicasawebService myService = new PicasawebService("smartmirror");

    protected String doInBackground(String[] params) {
        try {
            myService.setUserCredentials("smartmirrortesting@gmail.com", "smartmirrort");
        } catch (Exception e) {
            Log.i("PHOTOS", e.toString());
        }
        //listOfAlbums();

        return "success";
    }

    protected void onPostExecute(String message) {

    }

    public void listOfAlbums() {
        try {
            URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/smartmirrortesting@gmail.com?kind=album");

            UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);

            for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
                //System.out.println("PHOTOS " + myAlbum.getTitle().getPlainText());
                Log.i("PHOTOS", myAlbum.getTitle().getPlainText());
            }
        } catch (Exception e ) {
            Log.i("PHOTOS", e.toString());}

    }
}
