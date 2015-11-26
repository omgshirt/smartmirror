package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

public class FacebookFragment extends Fragment {

    //LoginButton btnLoginButton;
    //CallbackManager mCBManager;
    private WebView webview;
    private String curURL;
    private static final String SCROLLUP="up";
    private static final String SCROLLDOWN="down";

    public void init(String url) {

        curURL = url;

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        //mCBManager = CallbackManager.Factory.create();
        View view = inflater.inflate(R.layout.facebook_fragment, container, false);
        init("https://m.facebook.com/");

        webview = (WebView) view.findViewById(R.id.facebook_webview);
        if (curURL != null) {

            webview.getSettings().setJavaScriptEnabled(true);

            webview.setWebViewClient(new webClient());

            webview.loadUrl(curURL);

        }
        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("News", "Got message:\"" + message +"\"");
            switch (message) {
                case SCROLLUP:
            Log.d("News", "Got message:\"" + message + "\"");
            if(message.contains(MainActivity.mSCROLLUP))
                webview.scrollTo(0, -(int) 0.5*(((int)webview.getScale() * webview.getContentHeight())-webview.getHeight()));
            else
                webview.scrollTo(0, (int) 0.5*(((int)webview.getScale() * webview.getContentHeight())-webview.getHeight()));

            /*switch (message) {
                case MainActivity.mSCROLLUP:
                    Log.i(" is it ", message);
                    webview.scrollBy(0, -1000);
                    break;
                case SCROLLDOWN:
                    Log.i(" is it ", message);
                    webview.scrollBy(0, +1000);
                    break;
            }
        }
    };

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private class webClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return false;

        }

    }

/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCBManager.onActivityResult(requestCode, resultCode, data);
    }*/


}
