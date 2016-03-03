package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
    Handles retrieval of calendar items and formats them to CalendarEvents
 */
public class CalendarUtil {

    public static String dateToStr;
    public static final int ONE_DAY = 86400000;
    public static final int FIVE_DAYS = ONE_DAY * 5;
    public static final int TEN_HOURS = 36000000;

    public static final int CALENDAR_ID = 0;
    public static final int EVENT_NAME = 1;
    public static final int EVENT_DESC = 2;
    public static final int EVENT_START = 3;
    public static final int EVENT_END = 4;
    public static final int EVENT_LOC = 5;
    public static final int ACCOUNT_NAME = 6;

    public static void setCalendarHeader(String dateToString) {
        dateToStr = dateToString;
    }

    /**
     * Format the given date object to "Thursday, Jan 5, 2016"
     * @param curDate date object to format
     * @return String
     */
    public static String getCalendarHeader(Date curDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMMM d", Locale.US);
        dateToStr = formatter.format(curDate);
        setCalendarHeader(dateToStr);
        return dateToStr;
    }

    /**
     * Return all CalendarEvents between now and endTimeMillis
     * @param context context
     * @param endTimeMillis how long into the future to look for events
     * @return List of events. Returns empty list if no items found.
     */
    public static List<CalendarEvent> getCalendarEvents(Context context, long endTimeMillis) {

        List<CalendarEvent> results = new ArrayList<>();

        long startTimeMillis = Calendar.getInstance().getTimeInMillis();
        endTimeMillis += startTimeMillis;
        if (endTimeMillis <= startTimeMillis) return results;               // sanity check for time

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startTimeMillis);
        ContentUris.appendId(builder, endTimeMillis);

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(builder.build(),
                new String[]{"calendar_id", "title", "description",
                        "dtstart", "dtend", "eventLocation", "calendar_displayName"}, null,
                null, "dtstart ASC");
        assert cursor != null;
        cursor.moveToFirst();

        // get the time format from preferences - this will follow 12 or 24 hour setting.
        DateFormat formatter = new SimpleDateFormat(Preferences.getInstance((Activity)context)
                .getTimeFormat(), Locale.US);

        if (cursor.getCount() > 0) {
            do {
                //If calendar name is what is stored in preferences, then add event to be displayed
                if (cursor.getString(ACCOUNT_NAME).equals(Preferences.getUserAccountName())) {

                    String eventName = cursor.getString(EVENT_NAME);
                    String location = cursor.getString(EVENT_LOC);

                    Date startT = new Date(cursor.getLong(EVENT_START));
                    String startTime = formatter.format(startT);

                    Date endT = new Date(cursor.getLong(EVENT_END));
                    String endTime = formatter.format(endT);

                    CalendarEvent event = new CalendarEvent();
                    event.start = new Date(cursor.getLong(EVENT_START));
                    event.end = new Date(cursor.getLong(EVENT_END));
                    event.startString = startTime;
                    event.endString = endTime;
                    event.description = eventName;
                    event.location = location;
                    event.account = cursor.getString(ACCOUNT_NAME);
                    event.calendarId = cursor.getInt(CALENDAR_ID);
                    results.add(event);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    public static List<String> readCalendarEvent(Context context, ListView listView) {

        long startMillis = Calendar.getInstance().getTimeInMillis();
        long endMillis = startMillis + FIVE_DAYS;

        ContentResolver cr = context.getContentResolver();

        //For event information
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        Cursor cursor = cr.query(builder.build(),
                new String[]{"calendar_id", "title", "description",
                        "dtstart", "dtend", "eventLocation", "calendar_displayName"}, null,
                null, "dtstart ASC");

        assert cursor != null;
        cursor.moveToFirst();

        List<String> eventList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, eventList);
        listView.setAdapter(arrayAdapter);

        if (cursor.getCount() == 0) {
            eventList.add("No Events Found");
        } else {
            // populate the adapter
            do {
                Log.i(Constants.TAG, "account :: " + cursor.getString(ACCOUNT_NAME));
                //If calendar name is what is stored in preferences, then add event to be displayed
                if (cursor.getString(ACCOUNT_NAME).equals(Preferences.getUserAccountName())) {

                    String eventName = cursor.getString(EVENT_NAME);

                    // Start and end times
                    Date startT = new Date(cursor.getLong(EVENT_START));
                    DateFormat formatter = new SimpleDateFormat("h:mm a");
                    String startTime = formatter.format(startT);

                    Date endT = new Date(cursor.getLong(EVENT_END));
                    DateFormat formatterEnd = new SimpleDateFormat("h:mm a");
                    String endTime = formatterEnd.format(endT);

                    // Set times to bold
                    SpannableString eventTime = new SpannableString(startTime + " - " + endTime);
                    eventTime.setSpan(new StyleSpan(Typeface.BOLD), 0, eventTime.length(), 0);

                    eventList.add("\n" + eventTime + "\n" + eventName + "\n");

                    Log.i(Constants.TAG, Arrays.toString(cursor.getColumnNames()));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return eventList;
    }
}