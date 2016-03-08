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


public class FacebookFragment extends Fragment {

    private Preferences mPreference;
    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facebook_fragment, container, false);
        webview = (WebView) view.findViewById(R.id.facebook_webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new webClient());
        webview.loadUrl(Constants.FACEBOOK_URL);
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
            Log.d("Facebook ", "Got message:\"" + message + "\"");
            if (message.contains(Constants.SCROLL_DOWN))
                webview.scrollBy(0, -((int) 0.3 * ((int) getResources().getDisplayMetrics().density * webview.getContentHeight()) - webview.getHeight()));
            else if (!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
                webview.scrollBy(0, (int) 0.3 * ((int) getResources().getDisplayMetrics().density * webview.getContentHeight()) - webview.getHeight());

        }
    };

    /**
     * When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     * We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     * voice recognition, the remote control, etc.
     */
    @Override
    public void onResume() {
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
}
