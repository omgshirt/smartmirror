package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Fragment that displays the inspirational quotes
 */
public class QuotesFragment extends Fragment {

    private TextView mquote;
    private TextView mTitle;
    private Typeface mQuoteFont;
    private Animation mFadeIn;
    private Animation mFadeOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the assets
        AssetManager assetManager = getContext().getAssets();
        // Loading Font Face
        mQuoteFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush.ttf");
        InputStream input;
        final String[] parts;
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
        //Runnable thread portion
        Runnable quoteRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Random quoteRandomizer = new Random();
                                int random_number = quoteRandomizer.nextInt(176);
                                mquote.setText(parts[random_number]);
                                mquote.startAnimation(mFadeIn);
                            }
                        });
                        Thread.sleep(8000L);
                    }
                } catch (InterruptedException iex) {

                }
            }
        };
        Thread quoteThread = new Thread(quoteRunnable);
        quoteThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quotes_fragment, container, false);
        // read the string into a table object
        mquote = (TextView) view.findViewById(R.id.quote_settings_content);
        mTitle= (TextView) view.findViewById(R.id.quote_settings_title);
        // Applying font
        mquote.setTypeface(mQuoteFont);
        mTitle.setTypeface(mQuoteFont);
        return view;
    }

}
