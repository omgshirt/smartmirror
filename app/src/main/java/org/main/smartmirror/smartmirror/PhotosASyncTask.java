package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.Scopes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.GphotoFeed;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    String userName = "smartmirrortesting@gmail.com";
    private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";
    private PicasawebService service;


    protected String doInBackground(String[] params) {

        return "success";
    }

    protected void onPostExecute(String message) {
        try {
            getAlbums(userName);
        } catch (Exception e) {
            Log.i("PHOTOS", e.toString());
        }
    }

   /* public void listOfAlbums() {
        try {
            URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/smartmirrortesting@gmail.com?kind=album");

            UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);

            for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
                //System.out.println("PHOTOS " + myAlbum.getTitle().getPlainText());
                Log.i("PHOTOS", myAlbum.getTitle().getPlainText());
            }
        } catch (Exception e ) {
            Log.i("PHOTOS", e.toString());}

    }*/

    /**
     * Constructs a new client with the given username and password.
     */
    public PhotosASyncTask(PicasawebService service, String uname,
                           String passwd) {
        this.service = service;

        if (uname != null && passwd != null) {
            try {
                service.setUserCredentials("smartmirrortesting@gmail.com", "smartmirrort");
            } catch (AuthenticationException e) {
                throw new IllegalArgumentException(
                        "Illegal username/password combination.");
            }
        }
    }

    public <T extends GphotoFeed> T getFeed(String feedHref,
                                            Class<T> feedClass) throws IOException, ServiceException {
        System.out.println("Get Feed URL: " + feedHref);
        return service.getFeed(new URL(feedHref), feedClass);
    }

    public List<AlbumEntry> getAlbums(String username) throws IOException,
            ServiceException {

        String albumUrl = API_PREFIX + username;
        UserFeed userFeed = getFeed(albumUrl, UserFeed.class);

        List<GphotoEntry> entries = userFeed.getEntries();
        List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
        for (GphotoEntry entry : entries) {
            GphotoEntry adapted = entry.getAdaptedEntry();
            if (adapted instanceof AlbumEntry) {
                albums.add((AlbumEntry) adapted);
            }
        }
        return albums;
    }
}
