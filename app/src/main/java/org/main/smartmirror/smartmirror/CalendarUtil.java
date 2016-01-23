package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarUtil extends Activity {

    public static String DateToStr;

    public static void setCalendarHeader(String dateToString){
        DateToStr = dateToString;
    }
    public static String getCalendarHeader(){
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat();
        DateToStr = format.format(curDate);

        format = new SimpleDateFormat("E, dd MMM yyyy");
        DateToStr = format.format(curDate);
        setCalendarHeader(DateToStr);
        return DateToStr;
    }

    public static  ArrayList<String> readCalendarEvent(Context context, ListView listView) {

        //Day, Month, Year, Hour, Minute formatting
        ArrayList nameOfEvent = new ArrayList();
        Calendar beginTime = Calendar.getInstance();

        // Is this necessary? beginTime is already set to current time on construction...

        /*
        DateFormat sdfy = new SimpleDateFormat("yyyy");
        int year = Integer.valueOf(sdfy.format(beginTime.getTime()));
        DateFormat sdfm = new SimpleDateFormat("MM");
        int month = Integer.valueOf(sdfm.format(beginTime.getTime())) - 1;
        DateFormat sdfd = new SimpleDateFormat("dd");
        int day = Integer.valueOf(sdfd.format(beginTime.getTime()));
        DateFormat sdfhr = new SimpleDateFormat("HH");
        int hour = Integer.valueOf(sdfhr.format(beginTime.getTime()));
        DateFormat sdfmin = new SimpleDateFormat("mm");
        int min = Integer.valueOf(sdfmin.format(beginTime.getTime()));
        beginTime.set(year, month, day, hour, min);
        */

        long startMillis = beginTime.getTimeInMillis();
        long endMillis = startMillis + 36000000 ;
        Cursor cursor = null;
        ContentResolver cr = context.getContentResolver();

        //For event information
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        cursor = cr.query(builder.build(),
                new String[]{"calendar_id", "title", "description",
                        "dtstart", "dtend", "eventLocation"}, null,
                null, "dtstart ASC");
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        //For Calendar account names (email accounts)
        Cursor cursorNames = null;
        ContentResolver crNames = context.getContentResolver();

        Uri.Builder builderNames = CalendarContract.Calendars.CONTENT_URI.buildUpon();
        ContentUris.appendId(builderNames, startMillis);
        ContentUris.appendId(builderNames, endMillis);
        cursorNames = crNames.query(builder.build(),
                new String[]{"calendar_displayName"}, null,
                null, null);

        cursorNames.moveToFirst();

        //fetching calendars id
        nameOfEvent.clear();

        if(cursor.getCount()==0){
            nameOfEvent.add("No Events");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nameOfEvent);
            // Set The Adapter
            listView.setAdapter(arrayAdapter);
        }
        else {

            for (int i = 0; i < CNames.length; i++) {
                java.util.Date startT = new java.util.Date(cursor.getLong(3));
                DateFormat formatter = new SimpleDateFormat("h:mm a");
                String dateFormatted = formatter.format(startT);
                java.util.Date endT = new java.util.Date(cursor.getLong(4));
                DateFormat formatterEnd = new SimpleDateFormat("h:mm a");
                String dateFormattedEnd = formatterEnd.format(endT);
                nameOfEvent.add("\n" + cursor.getString(1)+ "\n" + dateFormatted + " - " + dateFormattedEnd
                        + "\n" + cursorNames.getString(0)  );
                //ID of Calendar
//                if(cursorNames.getString(0)!=null) {
//                    nameOfEvent.add("Calendar: " + cursorNames.getString(0));
//                }
//                //Name of event
//                if(cursor.getString(1)!=null) {
//                    nameOfEvent.add("Event Name: " + cursor.getString(1));
//                }
//                //Location of event
//                if(cursor.getString(5)!=null) {
//                    nameOfEvent.add("Location: " + cursor.getString(5));
//                }
//                //Description of Event
//                if(cursor.getString(2)!=null) {
//                    nameOfEvent.add("Description: " + cursor.getString(2));
//                }
                //Start Time
//                if(cursor.getString(3) !=null) {
//                    java.util.Date startT = new java.util.Date(cursor.getLong(3));
//                    nameOfEvent.add("Start Time: " + startT);
//                }
//                //End Time
//                if(cursor.getString(4)!=null) {
//                    java.util.Date endT = new java.util.Date(cursor.getLong(4));
//                    nameOfEvent.add("End Time: " + endT);
//                    System.out.println("END TIME TEST: " + endT);
//                }

                cursor.moveToNext();
                cursorNames.moveToNext();
                ArrayAdapter<String> arrayAdapter =
                        new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nameOfEvent);
                // Set The Adapter
                listView.setAdapter(arrayAdapter);
            }
        }
        return nameOfEvent;
    }
}