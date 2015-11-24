package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the assets
        AssetManager assetManager = getContext().getAssets();
        // Loading Font Face
        mQuoteFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush.ttf");
        InputStream input;
        final String[] parts;
        try {
            input = assetManager.open("quotes");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            String text = new String(buffer);
            //break the string to parts by lines
            parts = text.split("\n");
            //Runnable thread portion
            Runnable quote_runnable=new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Random quoteRandomizer = new Random();
                                    int random_number = quoteRandomizer.nextInt(176);
                                    mquote.setText(parts[random_number].replaceAll("[0-9]", ""));
                                }
                            });
                            Thread.sleep(5000L);
                        }
                    } catch (InterruptedException iex) {}
                }
            };
            Thread quote_thread=new Thread(quote_runnable);
            quote_thread.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
