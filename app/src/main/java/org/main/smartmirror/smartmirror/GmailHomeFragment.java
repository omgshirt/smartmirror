package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GmailHomeFragment extends Fragment {

    public TextView textView;
    public ImageView mailIcon;
    public View lineDiv;
    GoogleAccountCredential mCredential;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public int numUnreadPrimary;
    public int numUnreadPrevious;
    private Preferences mPreference;
    private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.MAIL_GOOGLE_COM };

    private static ScheduledFuture<?> unreadCountScheduler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gmail_home_fragment, container, false);
        textView = (TextView)view.findViewById(R.id.num_unread);
        mailIcon = (ImageView) view.findViewById(R.id.mail_icon);
        lineDiv = view.findViewById(R.id.line_div_short);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mPreference.getGmailAccount());
        mCredential.setSelectedAccountName(mPreference.getGmailAccount());

        if (savedInstanceState == null ) {
            lineDiv.setVisibility(View.GONE);
        }

        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(1);

        final Runnable messageCountUpdater = new Runnable() {
            @Override
            public void run() {
                numUnreadPrevious = numUnreadPrimary;
                new MakeRequestTask(mCredential).execute();
            }
        };

        // set a thread to check update in gmailUnreadCount.
        if (unreadCountScheduler == null) {
            unreadCountScheduler = scheduler.scheduleAtFixedRate(messageCountUpdater, 10, 60, TimeUnit.SECONDS);
        }

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
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
        private List<String> getDataFromApi() throws IOException {
            // Get the labels in the user's account.
            String user = "me";
            String query = "in:inbox is:unread category:primary";

            ListMessagesResponse messageResponse =
                    mService.users().messages().list(user).setQ(query).execute();

            if(messageResponse.size() == 1){
                numUnreadPrimary = 0;
            }else {
                List<Message> messages = messageResponse.getMessages();
                numUnreadPrimary = messages.size();
            }

            return  new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if(numUnreadPrevious < numUnreadPrimary) {
                speakNewNotifications();
                numUnreadPrevious = numUnreadPrimary;
            }
            displayEmailCount();
        }
    }

    public void updateUnreadCount(){
        numUnreadPrimary--;
        numUnreadPrevious--;
        displayEmailCount();
    }

    public void displayEmailCount() {
        if (numUnreadPrimary > 0) {
            mailIcon.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            lineDiv.setVisibility(View.VISIBLE);
            String text = getResources().getString(R.string.gmail_home_inbox);
            textView.setText(String.format(text, numUnreadPrimary));
        }
        else{
            lineDiv.setVisibility(View.GONE);
            mailIcon.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }
    }

    public int getUnreadCount() {
        return numUnreadPrimary;
    }

    private void speakNewNotifications() {
        int numNewMessages = numUnreadPrimary - numUnreadPrevious;
        String text = " You have " + numNewMessages + " new messages.";
        String textSingle = " You have " + numNewMessages + " new message.";

        if (!text.equals("") && numNewMessages==1) {
            speakText(textSingle);
        }
        else if(!text.equals("")){
            speakText(text);
        }
    }

    private void speakText(String text) {
        ((MainActivity) getActivity()).speakText(text);
    }

}