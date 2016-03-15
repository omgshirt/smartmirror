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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;

import com.google.gdata.client.photos.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PhotosFragment extends Fragment {

    ImageView mPhotoFromPicasa;
    URL albumPostUrl;
    private PicasawebService service;
    Handler mHandler;
    String userID;
    String albumID;
    int numPhotos;

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=photo&max-results="+numPhotos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        //new PhotosASyncTask(service,"smartmirrortesting@gmail.com", "smartmirrort").execute();




        return view;
    }

    public void renderPhoto(JSONObject json) {

    }

    // Get picasa json
    private void updatePicasa(final String query) {
        new Thread() {
            public void run() {
                final JSONObject json = FetchURL.getJSON(query);
                if (json == null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            ((MainActivity) getActivity()).showToast(getString(R.string.photos_err),
                                    Gravity.CENTER, Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            try {;
                                Log.i("PICASA ", json.toString());
                                //renderNews(json);
                            } catch (Exception e) {Log.i("picasa ", e.toString());}

                        }
                    });
                }
            }
        }.start();
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

                    /*NodeList nodes = doc.getElementsByTagName(*//*tag from xml file*//*);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element element = (Element) nodes.item(i);
                        NodeList title = element.getElementsByTagName(*//*item within the tag*//*);
                        Element line = (Element) title.item(0);
                        phoneNumberList.add(line.getTextContent());
                    }*/
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
