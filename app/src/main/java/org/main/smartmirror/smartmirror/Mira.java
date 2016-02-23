package org.main.smartmirror.smartmirror;

import java.util.Random;

/**
 * Mira singleton.
 */
public class Mira {

    private MainActivity mActivity;
    private static Mira mira;

    private Mira(MainActivity activity) {
        mActivity = activity;
    }

    public static Mira getInstance(MainActivity activity) {
        if (mira == null) {
            mira = new Mira(activity);
        }
        return mira;
    }

    /**
     * Speak a random message about sleeping
     */
    public void saySleepMessage() {
        Random random = new Random();
        String voice;
        float rand = random.nextFloat();
        if (rand < .15)
            voice = "Will I dream?";
        else if (rand < .3)
            voice = "bye";
        else if (rand < .5)
            voice = "goodbye";
        else if (rand < .7)
            voice = "see you later";
        else if (rand < .9)
            voice = "I'll charge up for a bit";
        else
            voice = "it will be good to be rid of you for a while";
        mActivity.speakText(voice);
    }
}
