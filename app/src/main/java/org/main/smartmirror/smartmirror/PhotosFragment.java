package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.net.URL;

import com.google.gdata.client.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;


public class PhotosFragment extends Fragment {

    ImageView mPhotoFromPicasa;
    URL albumPostUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        new PhotosASyncTask().execute();

        return view;
    }

    public void renderPhoto(JSONObject json) {

    }

    /*public void uploadPhoto() {
        try {
            albumPostUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" + userName + "/albumid/SmartMirror");
            PhotoEntry myPhoto = new PhotoEntry();
            myPhoto.setTitle(new PlainTextConstruct("Puppies FTW"));
            myPhoto.setDescription(new PlainTextConstruct("Puppies are the greatest."));
            myPhoto.setClient("myClientName");

            MediaFileSource myMedia = new MediaFileSource(new File("/home/liz/puppies.jpg"), "image/jpeg");
            myPhoto.setMediaSource(myMedia);

            PhotoEntry returnedPhoto = myService.insert(albumPostUrl, myPhoto);
        } catch (Exception e) {
            Log.i("PHOTOS", e.toString());
        }

    }

    public void listOfAlbums() {
        try {
            URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/"+userName+"?kind=album");

            UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);

            for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
                System.out.println("PHOTOS " + myAlbum.getTitle().getPlainText());
            }
        } catch (Exception e ) {Log.i("PHOTOS", e.toString());}

    }*/


}
