package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;

public class CalendarFragment extends Fragment {

    //Add events as list items
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
        listView = (ListView)rootView.findViewById(R.id.listViewNames);
        ArrayList eventNames;
        eventNames = CalendarUtil.readCalendarEvent(getActivity(),listView );
        System.out.println(eventNames);
        return rootView;
    }
}
