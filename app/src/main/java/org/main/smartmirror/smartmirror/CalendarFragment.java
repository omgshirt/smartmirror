package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    //Add events as list items
    private ListView listView;
    private TextView calendarHeader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
        Log.i(Constants.TAG, Preferences.getUserAccountName() + "In onCreateView in CalendarFragment");
        listView = (ListView) rootView.findViewById(R.id.listViewNames);

        String calHeader = CalendarUtil.getCalendarHeader();
        calendarHeader = (TextView) rootView.findViewById(R.id.calendarTitle);
        calendarHeader.setText(calHeader);

        //ArrayList eventNames;
        List<String> eventNames = CalendarUtil.readCalendarEvent(getActivity(), listView);
        System.out.println(eventNames);
        return rootView;
    }
}
