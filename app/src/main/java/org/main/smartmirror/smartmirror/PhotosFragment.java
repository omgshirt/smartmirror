package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gdata.client.photos.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PhotosFragment extends Fragment {

    ImageView mPhotoFromPicasa;
    URL albumPostUrl;
    private PicasawebService service;
    Handler mHandler;
    String userID = "smartmirrortesting";
    String albumID;
    int numPhotos = 1;

    ArrayList<String> names = new ArrayList<>();

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=photo&max-results="+numPhotos;
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        getXmlFromUrl(getUserPhotos);

        return view;
    }

    public void renderPhoto(JSONObject json) {

    }


    private void getXmlFromUrl(final String query) {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(query);
                    URLConnection conn = url.openConnection();

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(conn.getInputStream());

                    NodeList nodes = doc.getElementsByTagName("author");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element element = (Element) nodes.item(i);
                        NodeList title = element.getElementsByTagName("name");
                        Element line = (Element) title.item(0);
                        names.add(line.getTextContent());
                        Log.i("Node ", names.toString());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
