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

public class LightSleepFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.off_fragment, container, false);

        Random random = new Random();
        String voice = "";
        float rand = random.nextFloat();
        if (rand < .1)
            voice = "Will I dream?";
        else if (rand < .25)
            voice = "bye";
        else if (rand < .4)
            voice = "goodbye";
        else if (rand < .5)
            voice = "see you later";
        else if (rand < .7)
            voice = "I'll charge up for a bit";
        ((MainActivity)getActivity()).startLightSensor();
        ((MainActivity)getActivity()).startTTS(voice);
        return view;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        ((MainActivity)getActivity()).stopLightSensor();
    }
}
