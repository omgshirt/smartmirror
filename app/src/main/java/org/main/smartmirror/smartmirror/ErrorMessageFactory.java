package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Creates and returns a TextView containing the given String.
 * Primarily use for building an error dialog in the middle of the display area
 */
public class ErrorMessageFactory {
    public static TextView buildErrorMessage(Context context, String message) {
        TextView tv = new TextView(context);
        tv.setText(message);
        tv.setTextSize(22);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setPadding(0, 40, 0, 0);
        return tv;
    }
}
