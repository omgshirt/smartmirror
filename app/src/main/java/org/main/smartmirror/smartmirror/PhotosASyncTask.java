package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//android.os.AsyncTask<Params, Progress, Result>
public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    String userID = "smartmirrortesting";
    String albumID = "SmartMirror";
    int numPhotos = 1;
    ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    String imageUrl;

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"/albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=photo&max-results="+numPhotos;
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID;



    @Override
    protected String doInBackground(String[] params) {

        try {
            getXmlFromUrl(getUserPhotos);

        }catch (Exception e) {
            Log.i("ERR ", e.toString());

        }

        return "SUCCESS";

    }

    @Override
    protected void onPostExecute(String message) {
        try {
            Picasso.with(MainActivity.getContextForApplication()).load(mImageUrlList.get(0)).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);
        } catch (Exception e) {e.printStackTrace();}

    }

    private void getXmlFromUrl(final String query) {
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

    public void traverse(Node node) {
        NodeList list = node.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i);
            traverse(currentNode);
        }


        if (node.getNodeName().equals("media:content")) {
            Element durationElement = (Element) node;
            System.out.println(durationElement.getAttribute("url"));
            imageUrl = durationElement.getAttribute("url");
            Log.i("PHOTO URL", imageUrl);
            mImageUrlList.add(Uri.parse(imageUrl));
        }


    }

}
