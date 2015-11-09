package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 11/7/2015.
 */


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//TODO: Add checks in CalendarUtil.java to check for all day events. Currently shows next days' all day event for current day

public class CalendarFragment extends Fragment {

    public TextView mOutputText;
    private LinearLayout activityLayout;
    private ListView listView;
   // Context context;


//    public static CalendarFragment newInstance() {
//        return new CalendarFragment();
//    }

//    public CalendarFragment() {
//    }

//    public static int getTitleResource() {
//        return TITLE_RESOURCE;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
//        activityLayout = new LinearLayout(getActivity());
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        activityLayout.setLayoutParams(lp);
//        activityLayout.setOrientation(LinearLayout.VERTICAL);
//        activityLayout.setPadding(16, 16, 16, 16);
//        TextView x = new TextView(getActivity());
//        x.setText("My Events");
//        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        activityLayout.addView(x);
//        mOutputText = new TextView(getActivity());
//        mOutputText.setLayoutParams(tlp);
//        mOutputText.setPadding(16, 16, 16, 16);
//        mOutputText.setVerticalScrollBarEnabled(true);
//        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        //CalendarUtil.printCalendars(getActivity());
        //CalendarUtil.printEvents(getActivity());
        listView = (ListView)rootView.findViewById(R.id.listViewNames);
        //CalendarUtil.printEventInstances(getActivity());

        //CalendarUtil.runQuery(context);

       // List<String> eventNamesList = new ArrayList<String>();

        ArrayList eventNames;
        eventNames = CalendarUtil.readCalendarEvent(getActivity(),listView );
        System.out.println(eventNames);
        //String[] eventNameString = new String[eventNames.size()];
//        for(int i = 0; i < eventNames.size(); i++){
//            eventNameString[i] = eventNames.toArray(new String[eventNames.size()]);
//        }
//        ArrayAdapter<String> eventNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, eventNameString);
//
////        for(int i =0; i < eventNames.size(); i++){
////            eventNamesList.add(eventNames);
////        }
//
//        for(int i = 0; i < eventNames.size(); i++){
//            TextView eventName = new TextView(getActivity());
//            eventName.setText(eventNames.get(1).toString());
//            listView.addView(eventName);
//        }

        return rootView;
    }

}
