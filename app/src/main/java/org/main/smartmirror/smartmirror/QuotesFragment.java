package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment that displays the inspirational quotes
 */
public class QuotesFragment extends Fragment {
    
    private ArrayList<String> mQuotesList;
    private Runnable mRunnable;
    private TextView mQuoteContent;
    private TextView mQuoteTitle;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Typeface mQuoteFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //instantiate the timer object
        mTimer = new Timer();
        // Loading Font Face
        mQuoteFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush.ttf");

        // Set-up the fade in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(5000);

        // Set-up the fade out
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setStartOffset(5000);
        fadeOut.setDuration(5000);

        // Create our animation
        final AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);

        // Get the quotes as an array list
        mQuotesList = new ArrayList<>(Arrays.asList(getQuotes()));

        // Set the runnable
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // Set the Random quote in the Text View
                mQuoteContent.setText(getRandomQuote(mQuotesList.size()));
                // Start the animation
                mQuoteContent.startAnimation(animation);
            }
        };

        // Set the timer task
        mTimerTask  = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quotes_fragment, container, false);
        // Set-up the text views
        mQuoteTitle= (TextView) view.findViewById(R.id.quote_title);
        mQuoteContent = (TextView) view.findViewById(R.id.quote_content);

        // Apply the font
        mQuoteContent.setTypeface(mQuoteFont);
        mQuoteTitle.setTypeface(mQuoteFont);
        // Start the timer
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 10000);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // kill the timer
        mTimer.cancel();
    }

    /**
     * Method that handles loading the quotes from assets/quotes and saves
     * them into an array
     * @return the quotes in a String array
     */
    public String[] getQuotes(){
        // get the quotes
        AssetManager assetManager = getContext().getAssets();
        byte[] buffer;
        InputStream input;
        String text = null;
        String[] quotes;
        try {
            input = assetManager.open("quotes");
            int size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            text = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //break the string to parts by lines
        quotes = text.split("\n");
        return quotes;
    }

    /**
     * Method that handles the picking a random quote based on
     * a given number
     * @param num the random number seed
     * @return the selected random quote string
     */
    public String getRandomQuote(int num){
        //TODO make sure that the quotes are trully random (they don't repeat)
        Random quoteRandomizer = new Random();
        int randNum = quoteRandomizer.nextInt(num);
        return mQuotesList.get(randNum);
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("News", "Got message:\"" + message + "\"");
            switch(message){
                case MainActivity.BACK:
                    getFragmentManager().popBackStack();
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

    @Override
    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }
}
