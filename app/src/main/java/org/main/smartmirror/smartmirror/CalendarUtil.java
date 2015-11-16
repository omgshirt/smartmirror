package org.main.smartmirror.smartmirror;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarUtil extends Activity {

    public static  ArrayList<String> readCalendarEvent(Context context, ListView listView) {


        //Refer to link below for smymbol format:
        //http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        ArrayList nameOfEvent = new ArrayList();
        Calendar beginTime = Calendar.getInstance();
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
        //System.out.println("Name of Calendars: " + cursorNames.getString(0));
        //cursorNames.moveToNext();
        //System.out.println("Name of Calendars: " + cursorNames.getString(0));

        String CalNames[] = new String[cursorNames.getCount()];

        //fetching calendars id
        nameOfEvent.clear();

        //startDates.clear();
        //endDates.clear();
        //descriptions.clear();

        //TODO: THIS IS WHERE WE CHOOSE NUMBER OF CALENDARS TO DISPLAY. TRY TO DISPLAY ALL CALENDAR EVENTS WITHOUT DUPLICATES
        if(cursor.getCount()==0){
            nameOfEvent.add("No Events");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nameOfEvent);
            // Set The Adapter
            listView.setAdapter(arrayAdapter);
        }
        else {
            for (int i = 0; i < CNames.length; i++) {

                //FOR TESTING PURPOSES
                //Column Data
                System.out.println("Cursor 0: " + cursor.getString(0));
                System.out.println("Cursor 1: " + cursor.getString(1));
                System.out.println("Cursor 2: " + cursor.getString(2));
                System.out.println("Cursor 3: " + cursor.getString(3));
                System.out.println("Cursor 4: " + cursor.getString(4));
                System.out.println("Cursor 5: " + cursor.getString(5));
                //Columns
                System.out.println("Cursor 0: " + cursor.getColumnName(0));
                System.out.println("Cursor 1: " + cursor.getColumnName(1));
                System.out.println("Cursor 2: " + cursor.getColumnName(2));
                System.out.println("Cursor 3: " + cursor.getColumnName(3));
                System.out.println("Cursor 4: " + cursor.getColumnName(4));
                System.out.println("Cursor 5: " + cursor.getColumnName(5));

                //ID of Calendar
                if(cursorNames.getString(0)!=null) {
                    nameOfEvent.add("Calendar: " + cursorNames.getString(0));
                }
                //Name of event
                if(cursor.getString(1)!=null) {
                    nameOfEvent.add("Event Name: " + cursor.getString(1));
                }
                //Location of event
                if(cursor.getString(5)!=null) {
                    nameOfEvent.add("Location: " + cursor.getString(5));
                }
                //Description of Event
                if(cursor.getString(2)!=null) {
                    nameOfEvent.add("Description: " + cursor.getString(2));
                }
                //Start Time
                if(cursor.getString(3) !=null) {
                    java.util.Date startT = new java.util.Date(cursor.getLong(3));
                    nameOfEvent.add("Start Time: " + startT);
                }
                //End Time
                if(cursor.getString(4)!=null) {
                    java.util.Date endT = new java.util.Date(cursor.getLong(4));
                    nameOfEvent.add("End Time: " + endT);
                    System.out.println("END TIME TEST: " + endT);
                }

            /*
            startDates.add(String.valueOf(cursor.getLong(3)));
            endDates.add(String.valueOf(cursor.getLong(4)));
            descriptions.add(cursor.getString(2));
            */
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
