package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private Preferences mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
        if (!mPreference.isLoggedInToGmail()) {
            removeCalendar();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
        LinearLayout calendarLayout = (LinearLayout) rootView.findViewById(R.id.calendar_layout);

        // read events for the next five days
        List<CalendarEvent> events = CalendarUtil.getCalendarEvents(getContext(), CalendarUtil.LOOK_AHEAD);

        // Display error if no events found
        if (events.size() == 0) {
            TextView noEvents = ErrorMessageFactory.buildErrorMessage(getContext(),
                    getContext().getString(R.string.calendar_items_err));
            calendarLayout.addView(noEvents);
            return rootView;
        }

        Calendar calendar = Calendar.getInstance();
        int prevEventDay = -1;

        for (int i = 0; i < events.size() && i < 7; i++) {
            CalendarEvent event = events.get(i);
            // create a copy of calendar_item xml.
            View eventLayout = inflater.inflate(R.layout.calendar_item, container, false);

            // Check if this event happens on the same day as previous event, if not, display Date info
            calendar.setTime(event.start);
            int thisEventDay = calendar.get(Calendar.DAY_OF_YEAR);
            if (thisEventDay != prevEventDay) {
                TextView txtEventDate = (TextView) eventLayout.findViewById(R.id.event_date);
                txtEventDate.setText(CalendarUtil.getCalendarHeader(event.start));
                txtEventDate.setGravity(Gravity.CENTER_HORIZONTAL);
                txtEventDate.setVisibility(View.VISIBLE);

                View lineDivider = eventLayout.findViewById(R.id.calendar_day_divider);
                lineDivider.setVisibility(View.VISIBLE);

                prevEventDay = thisEventDay;
            }

            // Set event time
            TextView txtEventTime = (TextView) eventLayout.findViewById(R.id.event_time);
            String eventTime = event.startString + " - " + event.endString;
            txtEventTime.setText(eventTime);

            // Set event description
            TextView txtEventDesc = (TextView) eventLayout.findViewById(R.id.event_description);
            txtEventDesc.setText(event.description);

            //Log.i(Constants.TAG, event.description + " :: day :: " + thisEventDay);

            // Set event location, if it exists
            if (!event.location.isEmpty()) {
                TextView txtEventLoc = (TextView) eventLayout.findViewById(R.id.event_location);
                txtEventLoc.setText(event.location);
                txtEventLoc.setVisibility(View.VISIBLE);
            }

            // Add 10dp marginTop. This sucks...
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 0, 4);
            eventLayout.setLayoutParams(llp);

            // Add this event to calendarLayout
            calendarLayout.addView(eventLayout);
        }
        return rootView;
    }

    /**
     * Speaks the error, displays the error message and removes the calendar
     */
    private void removeCalendar() {
        ((MainActivity) getActivity()).removeFragment(Constants.CALENDAR);
        ((MainActivity) getActivity()).displayNotSignedInFragment(Constants.CALENDAR, true);
        ((MainActivity) getActivity()).speakText(getResources().getString(R.string.speech_not_logged_in_err));
    }
}
