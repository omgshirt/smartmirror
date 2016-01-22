package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by Brian on 10/26/2015.
 *
 * Sets the system and window brightness
 */
public class ScreenBrightnessHelper {
    
    public void setScreenBrightness(Activity activity, int brightness) {
        // set the system brightness and the window's brightness
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness / (float)255;
        activity.getWindow().setAttributes(lp);
    }
}
