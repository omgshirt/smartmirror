package org.main.smartmirror.smartmirror;

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

public class CalendarUtil {

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
                //If calendar name is what is stored in preferences, then add event to be displayed
                 if (cursorNames.getString(0).equals(Preferences.getUserAccountName())) {

                    java.util.Date startT = new java.util.Date(cursor.getLong(3));
                    DateFormat formatter = new SimpleDateFormat("h:mm a");
                    String dateFormatted = formatter.format(startT);
                    java.util.Date endT = new java.util.Date(cursor.getLong(4));
                    DateFormat formatterEnd = new SimpleDateFormat("h:mm a");
                    String dateFormattedEnd = formatterEnd.format(endT);
                    nameOfEvent.add("\n" + cursor.getString(1) + "\n" + dateFormatted + " - " + dateFormattedEnd
                            + "\n" + cursorNames.getString(0) + "\n" + cursor.getString(0));

                    cursor.moveToNext();
                    cursorNames.moveToNext();
                    ArrayAdapter<String> arrayAdapter =
                            new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nameOfEvent);
                    // Set The Adapter
                    listView.setAdapter(arrayAdapter);
                }
                else{ //If calendar name isn't what is in preferences, move to next
                    cursor.moveToNext();
                    cursorNames.moveToNext();
                }
            }
        }
        return nameOfEvent;
    }
}