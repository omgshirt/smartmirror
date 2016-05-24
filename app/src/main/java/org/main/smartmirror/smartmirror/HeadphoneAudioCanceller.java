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

/**
 * When headphones are plugged in, force audio output to use HDMI instead (whether available or not).
 * Based on reflection, this class could break at any time if Google decides to adjust the lower level
 * API accessed by AudioSystem.setDeviceConnectionState(int, int, String, String)
 */
public class HeadphoneAudioCanceller extends BroadcastReceiver {

    private static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DEVICE_STATE_AVAILABLE = 1;
    //
    // private static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 0x800;
    private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
    private static final int DEVICE_OUT_WIRED_HEADSET = 0x4;

    public static final String DEVICE_OUT_WIRED_HEADSET_NAME = "headset";
    public static final String DEVICE_OUT_WIRED_HEADPHONE_NAME = "headphone";


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
        filter.addAction(AudioManager.ACTION_HDMI_AUDIO_PLUG);
        context.registerReceiver(this, filter);
    }

    /**
     * Must be called on Activity.onDestroy()
     */
    public void teardown() {
        context.unregisterReceiver(this);
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
     * @param deviceName device name
     */
    private void setDeviceConnectionState(int device, int state, @NonNull String deviceAddress, @NonNull String deviceName) {
        Class<?> audioSystem = getAudioSystem();

        try {
            Method method = audioSystem.getMethod("setDeviceConnectionState", Integer.TYPE, Integer.TYPE, String.class, String.class);
            int status = (int)method.invoke(audioSystem, device, state, deviceAddress, deviceName);
            Log.i(Constants.TAG, "audioDevice status :: " + status);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(Constants.TAG, "Audio action rec :: " + action);

        // detected a headset plug status changes
        if (AudioManager.ACTION_HEADSET_PLUG.equals(action)) {
            Bundle extras = intent.getExtras();
            Log.i(Constants.TAG, "DOCK_PLUG headset state :: " + extras.getInt("state"));
            if (extras.getInt("state") == 1) {
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "", DEVICE_OUT_WIRED_HEADSET_NAME);
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADPHONE, DEVICE_STATE_UNAVAILABLE, "", DEVICE_OUT_WIRED_HEADPHONE_NAME);
            }
        }
        // detected an HDMI plug connected or disconnected
        else if (AudioManager.ACTION_HDMI_AUDIO_PLUG.equals(action)) {
            Bundle extras = intent.getExtras();
            Log.i(Constants.TAG, "DOCK_PLUG HDMI state :: " + extras.getInt("state"));
            if (extras.getInt("state") == 1) {
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "", DEVICE_OUT_WIRED_HEADSET_NAME);
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADPHONE, DEVICE_STATE_UNAVAILABLE, "", DEVICE_OUT_WIRED_HEADPHONE_NAME);
            }
        }
    }
}
