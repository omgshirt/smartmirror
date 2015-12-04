package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Brian on 12/4/2015.
 *
 * Handles system intents related to ACTION_SCREEN_ON and ACTION_SCREEN_OFF.
 * These are broadcast when the screen times out or the user puts the device to sleep.
 */
public class ScreenReceiver extends BroadcastReceiver {
    public static boolean screenIsOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenIsOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenIsOn = true;
        }
    }
}
