package org.main.smartmirror.smartmirror;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by Jesus on 11/18/2015.
 */
public class QuoteFragment extends Fragment {


    InputStream input;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AssetManager assetManager = getContext().getAssets();

        View view = inflater.inflate(R.layout.quotes_fragment, container, false);
        try {
            input = assetManager.open("Quotes");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            //break the string to parts by lines
            String[] parts = text.split("\n");
            // read the string into a table object

            TextView mquote = (TextView) view.findViewById(R.id.quote_settings_content);
            Random quoteRandomizer = new Random();
            int random_number = quoteRandomizer.nextInt(176);
            mquote.setText(parts[random_number]);



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return view;
    }
}



/*
    public View OncreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AssetManager assetManager =getContext().getAssets();

        View view = inflater.inflate(R.layout.quotes_fragment, container, false);
        try {
            input = assetManager.open("Quotes.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            //break the string to parts by lines
            String[] parts = text.split("\n");
            // read the string into a table object

           TextView mquote = (TextView)view.findViewById(R.id.quote_settings_content);
            Random quoteRandomizer=new Random();
           int random_number= quoteRandomizer.nextInt(176);
            mquote.setText(parts[random_number]);



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

  return view;
    }
}

*/

