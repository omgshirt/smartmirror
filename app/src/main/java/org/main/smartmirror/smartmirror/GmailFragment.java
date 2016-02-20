package org.main.smartmirror.smartmirror;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//import com.google.api.services.gmail.model.*;
//import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.ListThreadsResponse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Gmail fragment should get messages one by one

public class GmailFragment extends Fragment {

    public static List<String> threadList = new ArrayList<>();
    public static ArrayList<String> labelLR = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    public static ListView listView;
    public static TextView textView;
    GoogleAccountCredential mCredential;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static String PREF_ACCOUNT_NAME = "";

    //SCOPES
    //private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS };
    //private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.MAIL_GOOGLE_COM, GmailScopes.GMAIL_MODIFY, GmailScopes.GMAIL_INSERT, GmailScopes.GMAIL_COMPOSE};
    //private static final String[] SCOPES = {"https://mail.google.com/", "https://www.googleapis.com/auth/gmail.readonly/", "https://www.googleapis.com/auth/gmail.modify/"};
    private static final String[] SCOPES = {"oauth2:googleapis.com/auth/gmail.readonly"};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gmail_fragment, container, false);
        textView = (TextView)view.findViewById(R.id.gmailTitle);
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        listView = (ListView)view.findViewById(R.id.gmailMessageList);

        PREF_ACCOUNT_NAME = Preferences.getUserAccountName();

        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        mCredential.setSelectedAccountName(PREF_ACCOUNT_NAME);
        arrayAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, labelLR);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        }
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != getActivity().RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
        }
    }

    private void refreshResults() {
        if (isDeviceOnline()) {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                 return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        //THIS IS THE METHOD THAT IS CAUSING PROBLEMS: Dont delete commented code unless we fix
        private List<String> getDataFromApi() throws IOException {
            // Get the labels in the user's account.
            String user = "me";
           // String query = "is:sent";
           // List<String> labelId = new ArrayList<>();
            //labelId.add("INBOX");
            textView.setText("Unread Threads");
            Log.i(Constants.TAG, "Before Thread Response Call");
            //THE TWO LINES BELOW (one statement) is causing issues. The log.i statement "After thread..." never gets called.
            ListThreadsResponse threadResponse =
                    mService.users().threads().list(user).setMaxResults(Long.valueOf(10)).execute();
            List<Thread> threads = threadResponse.getThreads();
            for (Thread thread : threads) { System.out.println("Thread ID: " + thread.getId()); }
            //ListLabelsResponse lR = mService.users().labels().list(user).execute();
            //ArrayList<String> labels = new ArrayList<>();
            Log.i(Constants.TAG, "After Thread Response Call");
            //List<Thread> threads = new ArrayList<Thread>();
           // threads = threadResponse.getThreads();
//            while(threadResponse.getThreads()!=null){
//                threads.addAll(threadResponse.getThreads());
//                if(threadResponse.getNextPageToken() != null){
//                    String pageToken = threadResponse.getNextPageToken();
//                    threadResponse = mService.users().threads().list(user).setPageToken(pageToken).execute();
//                }
//                else{
//                    break;
//                }
//            while(lR.getLabels()!=null){
//                labels.addAll(lR.getLabels());
//                if(threadResponse.getNextPageToken() != null){
//                    String pageToken = threadResponse.getNextPageToken();
//                    threadResponse = mService.users().threads().list(user).setPageToken(pageToken).execute();
//                }
//                else{
//                    break;
//                }
          //  }
//            for(Thread thread : threads){
//                threadList.add(thread.toPrettyString());
//                System.out.println(thread.toPrettyString());
//            }

//            for (Label label : lR.getLabels()) {
//                //if(label.getName().equals("INBOX")) {
//                    Label labelCount = mService.users().labels().get(user, label.getId()).execute();
//                    labelLR.add(label.getName() + " " + labelCount.getThreadsUnread());
////                String id = label.getId();
////                List<String> labeli = new ArrayList<>();
////                labeli.add(id);
////                ListThreadsResponse threadResponse =
////                    mService.users().threads().list(user).setLabelIds(labeli).execute();
//                Log.i(Constants.TAG, "AFTER BOTH");
//                    //textView.setText("Unread Mail: " + Integer.toString(labelCount.getThreadsUnread()));
//                //}
//            }
//
//            labelLR.add("TESTING THIS S");

//            ArrayAdapter<String> arrayAdapter =
//                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, labels);
//            ArrayAdapter<String> arrayAdapter =
//                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, threadList);
            // Set The Adapter
            //listView.setAdapter(arrayAdapter);

//            for (Thread thread : threadResponse.getThreads()) {
//                if(thread.size()!=0) {
//                    Thread threadCount = mService.users().threads().get(user, thread.getId()).execute();
//                    threads.add(thread.getId() + " " + threadCount.getMessages());
//                    textView.setText("Thread Test: ");
//                    //getThread(mService, PREF_ACCOUNT_NAME, label.getId());
//                }
//            }
            return threadList;
            //return threads; changed from List<String> to void
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (output == null || output.size() == 0) {
            } else {
                output.add(0, "Data retrieved using the Gmail API:");
            }
        }
    }
}