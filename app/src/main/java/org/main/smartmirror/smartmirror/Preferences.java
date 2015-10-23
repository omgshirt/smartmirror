package org.main.smartmirror.smartmirror;

/**
 * Created by Brian on 10/22/2015.
 *
 * This is a singleton class to hold preferences for the application
 * Get the instance of it by using the getInstance() method
 */
public class Preferences {

    private static Preferences mPreferences = null;
    private static double volume = 1;

    private Preferences() {

    }

    public Preferences getInstance() {
        if (mPreferences == null) {
            mPreferences = new Preferences();
        }
        return mPreferences;
    }

}
