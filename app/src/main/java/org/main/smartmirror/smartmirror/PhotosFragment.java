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

    ImageView mPhotoFromPicasa;
    URL albumPostUrl;
    Handler mHandler;
    String userID = "smartmirrortesting";
    String albumID = "SmartMirror";
    int numPhotos = 1;
    ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    String[] imageList = new String[10];
    ListView photosFeed;

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"/albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=photo&max-results="+numPhotos;
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        //mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);
        photosFeed = (ListView) view.findViewById(R.id.list_photos);


        getXmlFromUrl(getUserPhotos);
        //renderPhoto();

        return view;
    }

    public void renderPhoto() {
        //Picasso.with(getActivity()).load(mImageUrlList.get(0)).fit().centerInside().into(mPhotoFromPicasa);
        //new PhotosASyncTask().execute();

        ArrayList<CustomListViewObject> objects = new ArrayList<CustomListViewObject>();
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), objects);
        try {
            for(int j = 0; j < 10; j++){
                CustomListViewObject co = new CustomListViewObject(null,null,mImageUrlList.get(j), null);
                objects.add(co);
                customAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {Log.i("PHOTOS", e.toString());}
        photosFeed.setAdapter(customAdapter);
    }


    private void getXmlFromUrl(final String query) {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(query);
                    URLConnection conn = url.openConnection();

                    // get xml from api
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(conn.getInputStream());
                    traverse(doc.getDocumentElement());



                    /*NodeList nodes = doc.getElementsByTagName("author");
                    //Log.i("Node ", nodes.toString());
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element element = (Element) nodes.item(i);
                        NodeList title = element.getElementsByTagName("name");
                        Element line = (Element) title.item(0);
                        //names.add(line.getTextContent());
                        //Log.i("Node ", names.toString());
                    }*/
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void traverse(Node node) {
        NodeList list = node.getChildNodes();
        String imageUrl;
        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i);
            traverse(currentNode);

        }

        for (int j = 0; j < list.getLength(); j++) {
            if (node.getNodeName().equals("media:content")) {
                Element durationElement = (Element) node;
                System.out.println(durationElement.getAttribute("url"));
                imageUrl = durationElement.getAttribute("url");
                mImageUrlList.add(j, Uri.parse(imageUrl));
                Log.i("PHOTO URL", mImageUrlList.get(j).toString());
                imageList[j] = imageUrl;
            }
        }
        /*try {
            //Picasso.with(getContext()).load(imageList[0]).fit().centerInside().into(mPhotoFromPicasa);
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable(){
                @Override
                public void run() {
                    Picasso.with(getContext()).load(mImageUrlList.get(0)).fit().centerInside().into(mPhotoFromPicasa);
                }
            });
        } catch (Exception e) {e.printStackTrace();}*/

    }


}
