package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Fragment that displays the calendar evnts
 */
public class CalendarFragment extends Fragment {

    public TextView mOutputText;
    private LinearLayout activityLayout;
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
