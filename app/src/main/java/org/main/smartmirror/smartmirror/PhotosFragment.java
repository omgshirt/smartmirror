package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class PhotosFragment extends Fragment {

    public static ImageView mPhotoFromPicasa;
    URL albumPostUrl;
    Handler mHandler;
    String userID = "smartmirrortesting";
    String albumID = "SmartMirror";
    int numPhotos = 1;
    ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    String[] imageList = new String[10];
    String imageUrl;

    String samplePhoto = "https://lh3.googleusercontent.com/-c7yXykzq6uw/VuXZevOmjhE/AAAAAAAAALw/HdMLGlY50d8//SmartMirror";

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"/albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=photo&max-results="+numPhotos;
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        renderPhoto();

        return view;
    }

    public void renderPhoto() {
        Picasso.with(getActivity()).load(samplePhoto).fit().centerInside().into(mPhotoFromPicasa);
        //new PhotosASyncTask().execute();
    }




}
