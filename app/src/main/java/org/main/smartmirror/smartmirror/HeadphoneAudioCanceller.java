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

    private static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DEVICE_STATE_AVAILABLE = 1;
    //
    // private static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 0x800;
    private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
    private static final int DEVICE_OUT_WIRED_HEADSET = 0x4;
    private static final int DEVICE_OUT_HDMI = 0x400;

    private static final int FORCE_HDMI_SYSTEM_AUDIO_ENFORCED = 12;
    private static final int FOR_HDMI_SYSTEM_AUDIO = 5;

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

    private void setForceUse(int usage, int config) {
        Class<?> audioSystem = getAudioSystem();

        try {
            Method method = audioSystem.getMethod("setForceUse", Integer.TYPE, Integer.TYPE);
            int status = (int)method.invoke(audioSystem, usage, config);
            Log.i(Constants.TAG, "setForceUse status :: " + status);
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
            Log.i(Constants.TAG, "DOCK_PLUG state :: " + extras.getInt("state"));

            //AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            if (extras.getInt("state") == 1) {
                // Disable wired headset. This call doesn't stop audio from playing over the headset, and
                // may not be necessary.
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "", "");

                // Force audio out via the HDMI whenever a headset is plugged in.
                setForceUse(FOR_HDMI_SYSTEM_AUDIO, FORCE_HDMI_SYSTEM_AUDIO_ENFORCED);
                //setDeviceConnectionState(DEVICE_OUT_HDMI, DEVICE_STATE_AVAILABLE, "", "");
            }
        } else if (AudioManager.ACTION_HDMI_AUDIO_PLUG.equals(action)) {
            Bundle extras = intent.getExtras();
            Log.i(Constants.TAG, "DOCK_PLUG state :: " + extras.getInt("state"));
            if (extras.getInt("state") == 1) {
                // Disable wired headset
                setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "", "");
            }
        }
    }
}
