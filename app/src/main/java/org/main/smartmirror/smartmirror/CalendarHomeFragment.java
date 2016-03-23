package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.List;

/**
 * Fragment that handles all the events for today
 * Lives in content_main.xml
 */
public class CalendarHomeFragment extends Fragment {

    private Preferences mPreferences;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.calendar_home_fragment, container, false);

        LinearLayout homeCalendarLayout = (LinearLayout) mRootView.findViewById(R.id.calendar_home_layout);
        // read events for the next five days
        List<CalendarEvent> events = CalendarUtil.getCalendarEvents(getContext(), CalendarUtil.ONE_DAY);
        if (events.size() == 0) {
            hideCalendarHome();
            // I don't need this I think
            // return mRootView;
        }

        Calendar calendar = Calendar.getInstance();

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mPreferences.isLoggedInToGmail()) {
            hideCalendarHome();
        } else {
            showCalendarHome();
        }
    }

    private void hideCalendarHome() {
        // set view gone
        mRootView.setVisibility(View.GONE);
    }

    private void showCalendarHome() {
        // set view visible
        mRootView.setVisibility(View.VISIBLE);
    }
}
