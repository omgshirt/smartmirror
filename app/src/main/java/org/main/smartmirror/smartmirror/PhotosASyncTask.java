package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PhotosASyncTask extends AsyncTask<String, Void, String> {

    //String userID = "smartmirrortesting"; // user ID
    //String uID = "118328364730024898386"; // user ID??
    //String albumID = "6261649979025559057"; // smart mirror album
    //String albumID = "6263091469173478818"; // profile pics

    private int numPhotos = 4;
    private int currentPhoto = 0;

    private String userID;
    private String uID;

    private String getAlbums;
    private String getPhotosInAlbumPreUrl;
    private String getLatestPhotos;
    private String getUserPhotos;
    //String getAlbums = "https://picasaweb.google.com/data/feed/api/user/"+userID+"?kind=album";

    private String imageUrl;

    private TimerTask mTimerTask;
    private Timer mTimer;
    private Runnable mRunnable;
    private Activity activity;
    private Boolean isTaskCancelled = false;


    public PhotosASyncTask(Activity activity, String uid, String username) {
        this.activity = activity;
        this.uID = uid;
        this.userID = username;
        setURIs();
    }

    private void setURIs() {
        getAlbums = "https://picasaweb.google.com/data/feed/api/user/" + userID;
        getPhotosInAlbumPreUrl = "https://picasaweb.google.com/data/feed/api/user/" + userID + "/albumid/";
        getLatestPhotos = "https://picasaweb.google.com/data/feed/api/user/" + uID + "?kind=photo&max-results=" + numPhotos;
        getUserPhotos = "https://picasaweb.google.com/data/feed/api/user/" + uID;
    }


    @Override
    protected String doInBackground(String[] params) {
        try {
            Log.i("PHOTOS ", "getting albums");
            traverseForAlbums(getXmlFromUrl(getAlbums));
            for (int i = 0; i < PhotosFragment.mAlbumIdList.size(); i++) {
                String newPhotosUrl = getPhotosInAlbumPreUrl + PhotosFragment.mAlbumIdList.get(i);
                traverseForPhotos(getXmlFromUrl(newPhotosUrl));
            }
            updatePhotosCache(PhotosFragment.mImageUrlList);

        } catch (Exception e) {
            Log.i("ERR ", e.toString());

        }
        return "SUCCESS";
    }

    @Override
    protected void onPostExecute(String message) {
        new PhotosFragment().renderPhotos();
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

    private Document getXmlFromUrl(final String query) {
        Document doc = null;
        try {
            URL url = new URL(query);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(conn.getInputStream());
            //String xmlString = nodeToString(doc.getDocumentElement());
            //Log.i("FULL XML", xmlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public void traverseForPhotos(Node node) {
        NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            traverseForPhotos(currentNode);
        }

        if (node.getNodeName().equals("media:content")) {
            Element durationElement = (Element) node;
            imageUrl = durationElement.getAttribute("url");
            //Log.i("PHOTO URL", imageUrl);
            PhotosFragment.mImageUrlList.add(Uri.parse(imageUrl));
        }
    }

    public void traverseForAlbums(Node node) {
        NodeList nodeList = node.getChildNodes();
        String albumID;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            traverseForAlbums(currentNode);
        }

        if (node.getNodeName().equals("gphoto:id")) {
            albumID = node.getTextContent();
            //Log.i("ALBUM ID node", albumID);
            PhotosFragment.mAlbumIdList.add(albumID);
            //Log.i("ALBUM IDs", PhotosFragment.mAlbumIdList.toString());

        }
    }

    /*public void renderPhotos() {
        if(!isCancelled()) {
            try {
                mTimer = new Timer();
                // initialize the runnable that will handle the task
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!isCancelled()) {
                                Picasso.with(MainActivity.getContextForApplication()).load(PhotosFragment.mImageUrlList.
                                        get(currentPhoto)).fit().centerInside().into(PhotosFragment.mPhotoFromPicasa);
                            } else if (isCancelled()) {
                                isTaskCancelled = true;
                                mTimerTask.cancel();
                                cancel(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(isCancelled()) {
                            Log.i("Cancelled?", isTaskCancelled.toString());
                            mTimerTask.cancel();
                            cancel(true);
                        }
                        else if (!isCancelled()) {
                            currentPhoto++;
                            if (currentPhoto > PhotosFragment.mImageUrlList.size() - 1) {
                                currentPhoto = 0;
                            }
                            Log.i("Cancelled?", isTaskCancelled.toString());
                        }

                    *//*currentPhoto++;
                    if (currentPhoto > PhotosFragment.mImageUrlList.size() - 1) {
                        if (!isCancelled()) {
                            currentPhoto = 0;
                            Log.i("Cancelled?", isTaskCancelled.toString());
                        } else if (isCancelled()) {
                            Log.i("Cancelled?", isTaskCancelled.toString());
                            mTimerTask.cancel();
                            cancel(true);
                        }
                    }*//*

                    }
                };

                // initialize the timer task that will run on the UI
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(isCancelled()) {
                            mTimerTask.cancel();
                        }
                        else {
                            activity.runOnUiThread(mRunnable);
                        }
                    }
                };
                mTimer.scheduleAtFixedRate(mTimerTask, 0, 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }*/

    private void updatePhotosCache(List<Uri> data) {
        PhotosFragment.mCacheManager.addCache(PhotosFragment.PHOTO_CACHE, data, PhotosFragment.DATA_UPDATE_FREQUENCY);
        Log.i("PHOTOS CACHE", "updating " + PhotosFragment.PHOTO_CACHE);
    }


}