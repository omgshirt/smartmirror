package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsBodyFragment extends Fragment {

    TextView mTxtBody;
    TextView mTxtHeadline;
    ScrollView mScrollView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_body_fragment, container, false);

        // Initialize Items
        mTxtBody = (TextView) view.findViewById(R.id.txtNewsBody);
        mTxtHeadline = (TextView) view.findViewById(R.id.txtHeadline);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        mTxtBody.setText("");
        mTxtHeadline.setText("");

        try {
            mTxtBody.setText(Html.fromHtml(NewsFragment.mArticleFullBody));
            mTxtHeadline.setText(NewsFragment.mHeadline);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Check your internet connection",
                    Toast.LENGTH_LONG).show();
            Log.i("NEWS BODY", "cannot draw, check your internet connection");
        }

        return view;
    }


    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message.contains(Constants.SCROLL_DOWN))
                mScrollView.scrollBy(0, -((int) 0.3 * ((int) getResources().getDisplayMetrics().density * mScrollView.getHeight()) - mScrollView.getHeight()));
            else if (!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
                mScrollView.scrollBy(0, (int) 0.3 * ((int) getResources().getDisplayMetrics().density * mScrollView.getHeight()) - mScrollView.getHeight());
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


}
