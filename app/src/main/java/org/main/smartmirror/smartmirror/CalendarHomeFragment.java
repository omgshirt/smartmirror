package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Fragment that handles all the events for today
 * Lives in content_main.xml
 */
public class CalendarHomeFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_home_fragment, container, false);

        LinearLayout calendarHomeLayout = (LinearLayout) rootView.findViewById(R.id.calendar_home_fragment_layout);
        // create a copy of calendar_item xml.
        LinearLayout eventLayout = (LinearLayout) inflater.inflate(R.layout.calendar_item, calendarHomeLayout, true);

        // read events for the current day
        List<CalendarEvent> events = CalendarUtil.getCalendarEvents(getContext(), CalendarUtil.ONE_DAY);

        for (int i = 0; i < events.size() && i < 5; i++) {
            CalendarEvent event = events.get(i);

            // Set event time
            TextView txtEventTime = (TextView) eventLayout.findViewById(R.id.event_time);
            String eventTime = event.startString + " to " + event.endString;
            txtEventTime.setText(eventTime);

            // Set event description
            TextView txtEventDesc = (TextView) eventLayout.findViewById(R.id.event_description);
            txtEventDesc.setText(event.description);

            // Set event location, if it exists
            if (!event.location.isEmpty()) {
                TextView txtEventLoc = (TextView) eventLayout.findViewById(R.id.event_location);
                txtEventLoc.setText(event.location);
                txtEventLoc.setVisibility(View.VISIBLE);
            }

            // Add 10dp marginTop. This sucks...
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 0, 6);
            eventLayout.setLayoutParams(llp);
        }

        if (events.size() == 0) {
            rootView.setVisibility(View.GONE);
        }

        return rootView;
    }
}
