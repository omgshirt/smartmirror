package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Method;

public class HeadphoneAudioCanceller extends BroadcastReceiver {

    /**
     * Written in {@link android.media.AudioSystem} (hidden API)
     */
    private static final int DEVICE_STATE_UNAVAILABLE = 0;
    //
    // private static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 0x800;
    private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
    private static final int DEVICE_OUT_WIRED_HEADSET = 0x4;

    /**
     * Intent actions
     */
    public static final String INTENT_ACTION_ANALOG_AUDIO_DOCK_PLUG = "android.intent.action.ANALOG_AUDIO_DOCK_PLUG";
    public static final String MEDIA_ACTION_ANALOG_AUDIO_DOCK_PLUG = "android.media.action.ANALOG_AUDIO_DOCK_PLUG";


    private final Context context;

    /**
     * Constructor, initialize and attach this BroadcastReceiver to the specified context
     *
     * @param context the context
     */
    public HeadphoneAudioCanceller(@NonNull Context context) {
        this.context = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        context.registerReceiver(this, filter);
    }

    /**
     * Must be called on Activity.onDestroy()
     */
    public void teardown() {
        context.unregisterReceiver(this);
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(false);
    }

    private Class<?> audioSystem;

    /**
     * Obtains AudioSystem class
     * @return class object
     */
    private Class<?> getAudioSystem() {
        try {
            if (audioSystem == null) {
                audioSystem = Class.forName("android.media.AudioSystem");
            }
        } catch (Exception ignored) {
        }

        return audioSystem;
    }

    /**
     * Set the device connection state
     *
     * @param device device kind id
     * @param state DEVICE_STATE_AVAILABLE or DEVICE_STATE_UNAVAILABLE
     * @param deviceAddress device address
     */
    private void setDeviceConnectionState(int device, int state, @NonNull String deviceAddress) {
        Class<?> audioSystem = getAudioSystem();

        try {
            Method method = audioSystem.getMethod("setDeviceConnectionState", Integer.TYPE, Integer.TYPE, String.class);
            method.invoke(audioSystem, device, state, deviceAddress);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // detected a headset plug status changes
        if (AudioManager.ACTION_HEADSET_PLUG.equals(action)) {
            Bundle extras = intent.getExtras();
            Log.i(Constants.TAG, "DOCK_PLUG state :: " + extras.getInt("state"));

            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            if (extras.getInt("state") == 1) {
                // Disable wired headset and headphones
                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                am.setSpeakerphoneOn(true);

                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADPHONE, DEVICE_STATE_UNAVAILABLE, "");
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "");
            } else {
                am.setMode(AudioManager.MODE_NORMAL);
                am.setSpeakerphoneOn(false);
            }
        }
    }
}
