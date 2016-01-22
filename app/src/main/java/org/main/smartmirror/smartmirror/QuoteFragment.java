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
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment that displays the inspirational quotes
 */
public class QuoteFragment extends Fragment {
    
    private ArrayList<String> mQuoteList;
    private ArrayList<String> mQuoteAuthor;
    private Runnable mRunnable;
    private TextView txtQuoteAuthor;
    private TextView txtQuoteContent;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Typeface mQuoteFont;

    // mAvailableQuotes holds quotes that have yet to be shown. Once all are used, list is refreshed.
    private int nextQuote = 0;
    private ArrayList<Integer> mAvailableQuotes;
    private final int fadeInTime = 3000;
    private final int fadeOutTime = 3000;
    private final int quoteDisplayLength = 30000;
    private final int totalDisplayTime = fadeInTime + quoteDisplayLength + fadeOutTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimer = new Timer();
        // Loading Font Face
        mQuoteFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/DancingScript-Regular.otf");

        // Set-up the fade in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInTime);

        // Set-up the fade out
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setStartOffset(fadeInTime + quoteDisplayLength);
        fadeOut.setDuration(fadeOutTime);

        // Create our animation
        final AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);

        // Get the quotes as an array list
        // mQuoteList = new ArrayList<>(Arrays.asList(getQuotes()));
        setUpQuotes();
        refreshAvailableQuotes();

        // Set the runnable
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // Set the Random quote in the Text View
                int randomQuote = mAvailableQuotes.get(nextQuote);
                txtQuoteContent.setText(mQuoteList.get(randomQuote));
                txtQuoteAuthor.setText(mQuoteAuthor.get(randomQuote));
                // Start the animation
                txtQuoteAuthor.startAnimation(animation);
                txtQuoteContent.startAnimation(animation);

                nextQuote = (++nextQuote) % mQuoteList.size();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quotes_fragment, container, false);

        mTimerTask  = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, totalDisplayTime);

        // Set-up the text views
        txtQuoteAuthor = (TextView) view.findViewById(R.id.quote_author);
        txtQuoteContent = (TextView) view.findViewById(R.id.quote_content);
        // Apply the font
        txtQuoteContent.setTypeface(mQuoteFont);
        return view;
    }

    /**
     * Method that handles loading the quotes from assets/quotes and saves
     * them into an array
     */
    public void setUpQuotes(){
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
        setUpArrayLists(quotes);
    }

    /**-
     * Sets up the two array lists that will contain the quote itself and
     * the author as two separate array lists
     * @param fullQuote the full quote with author
     */
    public void setUpArrayLists(String[] fullQuote){

        mQuoteList = new ArrayList<>();
        mQuoteAuthor = new ArrayList<>();
        //takes the name of the speaker into another array to put in the second textView
        //finds the index of where '\' occurs
        for (String quote : fullQuote) {
            int split = quote.indexOf("\\");
            mQuoteList.add(quote.substring(0, split-1));
            mQuoteAuthor.add(quote.substring(split+1));
        }
    }

    // randomize quote order
    private void refreshAvailableQuotes() {
        mAvailableQuotes = new ArrayList<>();
        for (int i = 0; i < mQuoteList.size(); i++) {
            mAvailableQuotes.add(i);
        }
        Collections.shuffle(mAvailableQuotes);
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("Quotes", "Got message:\"" + message + "\"");
            switch(message){
                case Constants.BACK:
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
    public void onPause() {
        super.onPause();
        mTimerTask.cancel();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimerTask.cancel();
    }
}
