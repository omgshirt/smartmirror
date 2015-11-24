package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
 * Created by Jesus on 11/18/2015.
 */
public class QuoteFragment extends Fragment {

   String[] parts;
    InputStream input;
    TextView mquote;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AssetManager assetManager = getContext().getAssets();

        View view = inflater.inflate(R.layout.quotes_fragment, container, false);

        //setting the fade in an out
       final Animation in = new AlphaAnimation(0.0f,1.0f);
        in.setDuration(3000);

        final Animation out =new AlphaAnimation(1.0f,0.0f);
        out.setDuration(3000);



        try {
            input = assetManager.open("Quotes");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            //break the string to parts by lines
              parts = text.split("\n");
            // read the string into a table object


            mquote = (TextView) view.findViewById(R.id.quote_settings_content);
            TextView mtitle= (TextView) view.findViewById(R.id.quote_settings_title);


            // Loading Font Face
            Typeface custom_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush.ttf");


            // Applying font
          mquote.setTypeface(custom_font);
            mtitle.setTypeface(custom_font);











            //Runnable thread portion


            Runnable quote_runnable=new Runnable() {
                @Override



                public void run()

                {


                    try {
                        while (true) {


                            getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Random quoteRandomizer = new Random();
                                                                int random_number = quoteRandomizer.nextInt(176);
                                                                mquote.setText(parts[random_number].replaceAll("[0-9]", ""));
                                                                mquote.startAnimation(in);

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

        return view;
    }
}



