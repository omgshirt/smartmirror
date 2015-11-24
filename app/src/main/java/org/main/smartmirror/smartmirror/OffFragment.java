package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

/**
 * This fragment will be all black simulating the "Off" feature
 */

public class OffFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.off_fragment, container, false);

        Random random = new Random();
        String voice = "";
        float rand = random.nextFloat();
        if (rand < .1)
            voice = "Time for a nap";
        else if (rand < .3)
            voice = "bye";
        else if (rand < .5)
            voice = "see you later";

        ((MainActivity)getActivity()).startTTS(voice);

        return view;
    }
}
