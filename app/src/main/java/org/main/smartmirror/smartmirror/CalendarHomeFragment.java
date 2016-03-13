package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that handles all the events for today
 * Lives in content_main.xml
 */
public class CalendarHomeFragment extends Fragment {

    private Preferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mPreferences.getGmailLoggedIn()) {
            hideCalendarHome();
        } else {
            showCalendarHome();
        }
    }

    private void hideCalendarHome() {
        // set view gone
    }

    private void showCalendarHome() {
        // set view visible
    }
}
