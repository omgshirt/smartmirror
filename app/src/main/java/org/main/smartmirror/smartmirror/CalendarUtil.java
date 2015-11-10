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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarUtil extends Activity {

    /*
    public static ListView listView = new ListView(null);
    */
    private static final String TAG = CalendarUtil.class.getSimpleName();
    private static final String DEBUG_TAG = "MyActivity";
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };

    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;

    public static void runQuery(Context context){
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
// Submit the query and get a Cursor object back.
        try {
            cur = cr.query(uri, EVENT_PROJECTION, selection, null, null);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }


    public static void printCalendars(Context context) {
        runGenericQuery(context, "Calendars", CalendarContract.Calendars.CONTENT_URI);
    }

    public static void printEvents(Context context) {
        runGenericQuery(context, "Events", CalendarContract.Events.CONTENT_URI);
    }


    public static void printEventInstances(Context context) {
         //Specify the date range you want to search for recurring
         //event instances
        Calendar beginTime = Calendar.getInstance();

        beginTime.set(2015, Calendar.NOVEMBER, 9);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2015, Calendar.NOVEMBER, 10);
        long endMillis = endTime.getTimeInMillis();

        Cursor cursor = null;
        ContentResolver cr = context.getContentResolver();

        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {"207"};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        /*
        cursor =  cr.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs, null);
        */
        cursor =  cr.query(builder.build(), null, null, null, null);
        cursor.moveToFirst();


        String cursorContent = DatabaseUtils.dumpCursorToString(cursor);
        while(cursor.moveToNext()){
            String title = null;
            long eventID = 0;
            long beginVal = 0;

            eventID = cursor.getLong(PROJECTION_ID_INDEX);
            beginVal = cursor.getLong(PROJECTION_BEGIN_INDEX);
            title = cursor.getString(PROJECTION_TITLE_INDEX);
            System.out.println("In while loop: " + "Event ID:  " + eventID);

            Log.i(DEBUG_TAG, "Event:   " + eventID);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i(DEBUG_TAG, "Date:  " + formatter.format(calendar.getTime()));
        }

        if (cursor != null) {
            cursor.close();
        }

        Log.i(TAG, "-------Instances cursorContent: \n" + cursorContent);
    }

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

        beginTime.set(2015, month, day, hour, min);
        long startMillis = beginTime.getTimeInMillis();
        long endMillis = startMillis + 36000000 ;

        Cursor cursor = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {"x@gmail.com", "com.google",
                "x@gmail.com"};

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

//        fetching calendars id
        nameOfEvent.clear();
        /*
        startDates.clear();
        endDates.clear();
        descriptions.clear();
        */
        for (int i = 0; i < CNames.length; i++) {

            nameOfEvent.add(cursor.getString(1));
            /*
            startDates.add(String.valueOf(cursor.getLong(3)));
            endDates.add(String.valueOf(cursor.getLong(4)));
            descriptions.add(cursor.getString(2));
            */
            CNames[i] = cursor.getString(1);
            cursor.moveToNext();

            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,nameOfEvent );
            // Set The Adapter
            listView.setAdapter(arrayAdapter);
        }
        return nameOfEvent;
    }

    private static Cursor runGenericQuery(Context context, String queryName, Uri queryUri) {
        // Run query
        Cursor cursor;
        ContentResolver cr = context.getContentResolver();

        // Submit the query and get a Cursor object back.
        cursor = cr.query(queryUri, null, null, null, null);

        String cursorContent = DatabaseUtils.dumpCursorToString(cursor);

        if (cursor != null) {
            cursor.close();
        }



        Log.i(TAG, "------- " + queryName + " cursorContent: \n" + cursorContent);
        return cursor;
    }

}
