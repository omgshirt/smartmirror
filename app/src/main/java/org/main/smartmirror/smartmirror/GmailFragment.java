package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GmailFragment extends Fragment {

    public List<String> messageList = new ArrayList<>();
    public TextView textViewTitle;
    public TextView textViewTo;
    public String mTo;
    public TextView textViewFrom;
    public String mFrom;
    public TextView textViewSubject;
    public String mSubject;
    public String mBody;

    public ScrollView scrollViewBody;
    public TextView textViewBody;

    public View subjBodyDiv;

    public LinearLayout fromSubBody;

    private MakeRequestTask mRequestTask;

    GoogleAccountCredential mCredential;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    //SCOPES - Note: When adding/deleting scopes, it is necessary to reauthorize by:
    //               1. Remove SmartMirror from Google Account by going to Connected Apps and Services
    //               2. Run Google Account Picker (Navin uses the OldCalendarFragment code)
    //Otherwise, changes won't take place.
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_INSERT
    };

    private Preferences mPreference;

    OnNextMessageListener mCallback;

    //Interface for updating Gmail Unread Count
    public interface OnNextMessageListener {
        void onNextCommand();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnNextMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNextMessageListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.gmail_fragment, container, false);

        textViewTitle = (TextView)view.findViewById(R.id.gmailTitle);
        textViewTo = (TextView)view.findViewById(R.id.messageTo);
        textViewFrom = (TextView)view.findViewById(R.id.messageFrom);
        textViewSubject = (TextView)view.findViewById(R.id.messageSubject);
        scrollViewBody = (ScrollView) view.findViewById(R.id.scroll_view_body);
        textViewBody = (TextView) view.findViewById(R.id.messageBody);
        subjBodyDiv = view.findViewById(R.id.subject_body_divider);
        subjBodyDiv.setVisibility(View.GONE);
        fromSubBody = (LinearLayout) view.findViewById(R.id.gmail_view);



        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(mPreference.getGmailAccount(), null));

        mCredential.setSelectedAccountName(mPreference.getGmailAccount());

        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message.contains(Constants.SCROLL_DOWN) || message.contains(Constants.SCROLL_UP)) {
                VoiceScroll sl = new VoiceScroll();
                sl.scrollScrollView(message,scrollViewBody);
            }
            else if(message.contains(Constants.NEXT)){
                refreshResults();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRequestTask.cancel(true);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
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
            mRequestTask = new MakeRequestTask(mCredential);
            mRequestTask.execute();
        }else{
            Log.i(Constants.TAG, "Error in GmailFragment");
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

            List<String> labelsToRemove = new ArrayList<>();
            labelsToRemove.add("UNREAD");

            ListMessagesResponse messageResponse =
                    mService.users().messages().list(user).setQ(query).setMaxResults(Long.valueOf(1)).execute();

            if(messageResponse.size()==1){
                mTo = getResources().getString(R.string.no_new_messages);
            }else {

                List<Message> messages = messageResponse.getMessages();

                ModifyMessageRequest mods = new ModifyMessageRequest()
                        .setRemoveLabelIds(labelsToRemove);

                for (Message message : messages) {

                    Message message2 = mService.users().messages().get(user, message.getId()).execute();

                    Message message1 = mService.users().messages().modify(user, message.getId(), mods).execute();

                    int headerSize = message2.getPayload().getHeaders().size();

                    //Get who message is from
                    for (int i = 0; i < headerSize; i++) {
                        if (message2.getPayload().getHeaders().get(i).getName().equals("From")) {
                            mFrom =  message2.getPayload().getHeaders().get(i).getValue();
                        }
                    }
                    //Get subject of message
                    for (int j = 0; j < headerSize; j++) {
                        if (message2.getPayload().getHeaders().get(j).getName().equals("Subject")) {
                            mSubject = message2.getPayload().getHeaders().get(j).getValue();
                        }
                    }
                    //Get who message is to
                    for (int k = 0; k < headerSize; k++) {
                        if (message2.getPayload().getHeaders().get(k).getName().equals(("To"))) {
                            mTo = message2.getPayload().getHeaders().get(k).getValue();
                        }
                    }
                    //Get the body of the message
                    byte[] bodyBytes = Base64.decodeBase64(message2.getPayload().getParts().get(0).getBody().getData().trim()); // get body
                    mBody = new String(bodyBytes, "UTF-8");
                    messageList.add(mBody);
                }
            }
            return messageList;
        }

        @Override
        protected void onPreExecute() {
            messageList.clear();
        }

        @Override
        protected void onPostExecute(List<String> output) {

            if (!isAdded()) return;

            textViewTitle.setText(getResources().getString(R.string.inbox));

            if(mTo.equals(getResources().getString(R.string.no_new_messages))){
                textViewTo.setText(mTo);
                fromSubBody.setVisibility(View.GONE);
                mCallback.onNextCommand();
            }else {
                fromSubBody.setVisibility(View.VISIBLE);
                String to = "To: " + mTo;
                String from = "From: " + mFrom;
                String subject = "Subject: " + mSubject;
                textViewTo.setText(to);
                textViewFrom.setText(from);
                textViewSubject.setText(subject);
                textViewBody.setText(mBody);
                mCallback.onNextCommand();
            }
        }
    }
}