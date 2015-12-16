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
    private ArrayList<String> mQuotesAuthor;
    private Runnable mRunnable;
    private TextView mQuoteAuthor;
    private TextView mQuoteContent;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Typeface mQuoteFont;

    private final int fadeInTime = 2500;
    private final int fadeOutTime = 2500;
    private final int quoteDisplayLength = 6000;
    private final int totalDisplayTime = fadeInTime + quoteDisplayLength + fadeOutTime;

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
        // mQuotesList = new ArrayList<>(Arrays.asList(getQuotes()));
        setUpQuotes();

        // Set the runnable
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // Set the Random quote in the Text View
                getRandomQuote(mQuotesList.size());
                // Start the animation
                mQuoteAuthor.startAnimation(animation);
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
        mQuoteAuthor = (TextView) view.findViewById(R.id.quote_author);
        mQuoteContent = (TextView) view.findViewById(R.id.quote_content);
        // Apply the font
        mQuoteContent.setTypeface(mQuoteFont);
        // Start the timer
        mTimer.scheduleAtFixedRate(mTimerTask, 0, totalDisplayTime);
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
        String[] parts_no_name;
        String[] names;
        names=new String[fullQuote.length];
        parts_no_name=new String[fullQuote.length];
        //takes the name of the speaker into another array to put in the second textView
        //finds the index of where '-' occurs
        for (int x=0; x<fullQuote.length; x++) {
            int index = fullQuote[x].indexOf('-');
            String speaker_names = fullQuote[x].substring(index);
            names[x] = speaker_names;
            parts_no_name[x] = fullQuote[x].replaceAll(names[x], "");
        }
        mQuotesList = new ArrayList<>(Arrays.asList(parts_no_name));
        mQuotesAuthor = new ArrayList<>(Arrays.asList(names));
    }

    /**
     * Method that handles the picking a random quote based on
     * a given number
     * @param num the random number seed
     */
    public void getRandomQuote(int num){
        //TODO make sure that the quotes are trully random (they don't repeat)
        Random quoteRandomizer = new Random();
        int randNum = quoteRandomizer.nextInt(num);
        mQuoteContent.setText(mQuotesList.get(randNum));
        mQuoteAuthor.setText(mQuotesAuthor.get(randNum));
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
    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // kill the timer
        mTimer.cancel();
    }
}
