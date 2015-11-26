package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment that displays the inspirational quotes
 */
public class QuotesFragment extends Fragment {

    private TextView mquote;
    private TextView mName;
    private TextView mTitle;
    private Typeface mQuoteFont;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private static final String TAG = "QuotesFragment";
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new timer
        mTimer=new Timer();

        // get the assets
        AssetManager assetManager = getContext().getAssets();
        // Loading Font Face
        mQuoteFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush.ttf");
        InputStream input;
        final String[] parts,names,parts_no_name;
        byte[] buffer;
        //setting the fade in an out
        mFadeIn = new AlphaAnimation(0.0f, 1.0f);
        mFadeIn.setDuration(3000);

        mFadeOut = new AlphaAnimation(1.0f, 0.0f);
        mFadeOut.setDuration(3000);
        String text = null;
        try {
            input = assetManager.open("quotes");
            int size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            text = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //break the string to parts by lines
        parts = text.split("\n");
        names=new String[parts.length];
        parts_no_name=new String[parts.length];

        //takes the name of the speaker into another array to put in the second textView
        //finds the index of where '-' occurs
        for (int x=0;x<parts.length;x++) {


                int index = parts[x].indexOf('-');
                String speaker_names = parts[x].substring(index);
                names[x] = speaker_names;
                parts_no_name[x] = parts[x].replaceAll(names[x], "");


        }


        //Runnable thread portion
          mRunnable = new Runnable() {
              @Override
              public void run() {

                  Random quoteRandomizer = new Random();
                  int random_number = quoteRandomizer.nextInt(parts.length);
                  mquote.setText(parts_no_name[random_number]);
                  mName.setText(names[random_number]);
                  mquote.startAnimation(mFadeIn);
                  mName.setAnimation(mFadeIn);

              }


          };
        mTimerTask= new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(mRunnable);
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quotes_fragment, container, false);
        // read the string into a table object
        mquote = (TextView) view.findViewById(R.id.quote_settings_content);
        mName = (TextView) view.findViewById(R.id.quote_settings_content2);
        mTitle= (TextView) view.findViewById(R.id.quote_settings_title);
        // Applying font
        mquote.setTypeface(mQuoteFont);
        mTitle.setTypeface(mQuoteFont);
        mTimer.scheduleAtFixedRate(mTimerTask,0,10000);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "OnDestroy");
        mTimer.cancel();

    }

    public void onStop(){
        super.onStop();
        Log.e(TAG,"OnStop");
    }
    @Override
    public void onPause()
    {
        super.onPause();
        Log.e(TAG,"OnPause");
    }


}
