package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        View rootView = inflater.inflate(R.layout.calendar_home_fragment, container, false);
        LinearLayout homeCalendarLayout = (LinearLayout) rootView.findViewById(R.id.calendar_home_fragment_layout);
        // create a copy of calendar_item xml.
        View todaysEventsLayout = inflater.inflate(R.layout.calendar_item, container, false);
        // We only want today's events
        TextView txtEventDate = (TextView) todaysEventsLayout.findViewById(R.id.event_date);
        txtEventDate.setText("Today's Events");
        txtEventDate.setGravity(Gravity.CENTER_HORIZONTAL);

        View lineDivider = todaysEventsLayout.findViewById(R.id.line_divider);
        // read events for the current day
        List<CalendarEvent> events = CalendarUtil.getCalendarEvents(getContext(), CalendarUtil.ONE_DAY);

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < events.size() && i < 7; i++) {
            CalendarEvent event = events.get(i);
            lineDivider.setVisibility(View.VISIBLE);
            txtEventDate.setVisibility(View.VISIBLE);
            // Check if this event happens on the same day as previous event, if not, display Date info
            calendar.setTime(event.start);

            // Set event time
            TextView txtEventTime = (TextView) todaysEventsLayout.findViewById(R.id.event_time);
            String eventTime = event.startString + " - " + event.endString;
            txtEventTime.setText(eventTime);

            // Set event description
            TextView txtEventDesc = (TextView) todaysEventsLayout.findViewById(R.id.event_description);
            txtEventDesc.setText(event.description);

            // Set event location, if it exists
            if (!event.location.isEmpty()) {
                TextView txtEventLoc = (TextView) todaysEventsLayout.findViewById(R.id.event_location);
                txtEventLoc.setText(event.location);
                txtEventLoc.setVisibility(View.VISIBLE);
            }

            // Add 10dp marginTop. This sucks...
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 0, 4);
            todaysEventsLayout.setLayoutParams(llp);

            // Add this event to calendarLayout


            homeCalendarLayout.addView(todaysEventsLayout);
        }

        return rootView;
    }
}
