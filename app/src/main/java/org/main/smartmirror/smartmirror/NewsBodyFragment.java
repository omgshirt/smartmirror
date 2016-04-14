package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsBodyFragment extends Fragment {

    private TextView mTxtBody;
    private TextView mTxtHeadline;
    private ScrollView mScrollView;

    public static NewsBodyFragment newInstance(String headline, String body){
        Bundle args = new Bundle();
        args.putString("headline", headline);
        args.putString("body", body);
        NewsBodyFragment fragment = new NewsBodyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_body_fragment, container, false);

        // Initialize Items
        mTxtBody = (TextView)view.findViewById(R.id.txtNewsBody);
        mTxtHeadline = (TextView)view.findViewById(R.id.txtHeadline);
        mScrollView = (ScrollView)view.findViewById(R.id.scrollView);

        try {
            mTxtBody.setText(getArguments().getString("body"));
            mTxtHeadline.setText(getArguments().getString("headline"));
        } catch (Exception e) {((MainActivity) getActivity()).showToast(getString(R.string.news_err),
                Gravity.CENTER, Toast.LENGTH_LONG);}

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
                sl.scrollScrollView(message,mScrollView);
            }
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
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }
}
