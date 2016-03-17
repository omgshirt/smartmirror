package org.main.smartmirror.smartmirror;

import android.os.AsyncTask;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PhotosASyncTask extends AsyncTask<String, Void, String> {
    String userID = "smartmirrortesting";
    String albumID = "SmartMirror";
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+userID;
    String imageUrl;
    String samplePhoto = "https://lh3.googleusercontent.com/-c7yXykzq6uw/VuXZevOmjhE/AAAAAAAAALw/HdMLGlY50d8//SmartMirror";

    protected String doInBackground(String[] params) {
        try {
            getXmlFromUrl(getUserPhotos);
        } catch (Exception e) {e.printStackTrace();}

        return "success";
    }

    protected void onPostExecute(String message) {
        try {
            Picasso.with(MainActivity.getContextForApplication()).load(imageUrl).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);
        } catch (Exception e) {e.printStackTrace();}
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

        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i);
            traverse(currentNode);

        }

        if (node.getNodeName().equals("media:content")) {
            Element durationElement = (Element) node;
            System.out.println(durationElement.getAttribute("url"));
            imageUrl = durationElement.getAttribute("url");

            //new PhotosASyncTask().execute();
        }
    }


}