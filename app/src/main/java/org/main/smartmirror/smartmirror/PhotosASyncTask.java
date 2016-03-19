package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    String userID = "smartmirrortesting"; // user ID
    String uID = "118328364730024898386"; // user ID??
    String albumID = "6261649979025559057"; // smart mirror album
    //String albumID = "6263091469173478818"; // profile pics
    int numPhotos = 4;
    String imageUrl;
    int currentPhoto = 0;

    public static ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();

    private TimerTask mTimerTask;
    private Timer mTimer;
    private Runnable mRunnable;
    private Activity activity;

    String getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + uID;
    String getPhotosInAlbum = "https://picasaweb.google.com/data/feed/api/user/"+userID+"/albumid/"+albumID;
    String getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/"+uID+"?kind=photo&max-results="+numPhotos;
    String getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/"+uID;
    //String getAlbums = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=album";



    @Override
    protected String doInBackground(String[] params) {

        try {
            getXmlFromUrl(getPhotosInAlbum);

        }catch (Exception e) {
            Log.i("ERR ", e.toString());

        }
        return "SUCCESS";
    }

    @Override
    protected void onPostExecute(String message) {

        try {

            mTimer = new Timer();

            // initialize the runnable that will handle the task
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Picasso.with(MainActivity.getContextForApplication()).load(mImageUrlList.get(currentPhoto)).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);
                    currentPhoto++;
                    if (currentPhoto > mImageUrlList.size()-1) currentPhoto = 0;
                }
            };

            // initialize the timer task that will run on the UI
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //getActivity().runOnUiThread(mRunnable);
                    activity.runOnUiThread(mRunnable);

                }
            };
            mTimer.scheduleAtFixedRate(mTimerTask, 0, 5000);


        } catch (Exception e) {e.printStackTrace();}

    }

    public PhotosASyncTask(Activity activity) {

        this.activity = activity;
    }

    private String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            te.printStackTrace();
        }
        return sw.toString();
    }



    private void getXmlFromUrl(final String query) {
        try {
            URL url = new URL(query);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());
            String xmlString = nodeToString(doc.getDocumentElement());
            Log.i("FULL XML", xmlString);
            traverse(doc.getDocumentElement());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void traverse(Node node) {
        NodeList nodeList = node.getChildNodes();


        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            traverse(currentNode);
        }


        if (node.getNodeName().equals("media:content")) {
            Element durationElement = (Element) node;
            //System.out.println(durationElement.getAttribute("url"));
            imageUrl = durationElement.getAttribute("url");
            Log.i("PHOTO URL", imageUrl);
            mImageUrlList.add(Uri.parse(imageUrl));
            //Log.i("PHOTO", mImageUrlList.toString());
        }
    }

}
