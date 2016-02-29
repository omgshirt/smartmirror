package org.main.smartmirror.smartmirror;

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

public class CalendarUtil {

    public static String dateToStr;
    public static final int ONE_DAY = 86400000;
    public static final int FIVE_DAYS = ONE_DAY * 5;
    public static final int TEN_HOURS = 36000000;

    public static void setCalendarHeader(String dateToString) {
        dateToStr = dateToString;
    }

    public static String getCalendarHeader() {
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat();
        dateToStr = format.format(curDate);

        format = new SimpleDateFormat("E, dd MMM yyyy", Locale.US);
        dateToStr = format.format(curDate);
        setCalendarHeader(dateToStr);
        return dateToStr;
    }

    public static List<String> readCalendarEvent(Context context, ListView listView) {

        //Day, Month, Year, Hour, Minute formatting
        //List<String> nameOfEvent = new ArrayList<>();

        //Calendar beginTime = ;

        long startMillis = Calendar.getInstance().getTimeInMillis();
        long endMillis = startMillis + FIVE_DAYS;

        ContentResolver cr = context.getContentResolver();

        //For event information
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        Cursor cursor = cr.query(builder.build(),
                new String[]{"calendar_id", "title", "description",
                        "dtstart", "dtend", "eventLocation"}, null,
                null, "dtstart ASC");


        // fetching calendars name
        //String CNames[] = new String[cursor.getCount()];

        //For Calendar account names (email accounts)
        Cursor cursorNames;
        ContentResolver crNames = context.getContentResolver();

        Uri.Builder builderNames = CalendarContract.Calendars.CONTENT_URI.buildUpon();
        ContentUris.appendId(builderNames, startMillis);
        ContentUris.appendId(builderNames, endMillis);
        cursorNames = crNames.query(builder.build(),
                new String[]{"calendar_displayName"}, null,
                null, null);

        assert cursor != null;
        cursor.moveToFirst();
        assert cursorNames != null;
        cursorNames.moveToFirst();

        //fetching calendars id - DON'T NEED
        //nameOfEvent.clear();

        // Moved adapter code here so we don't create a new one for each item
        List<String> eventList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, eventList);
        listView.setAdapter(arrayAdapter);

        if (cursor.getCount() == 0) {
            eventList.add("No Events Found");
        } else {
            // populate the adapter
            do {
                //If calendar name is what is stored in preferences, then add event to be displayed
                if (cursorNames.getString(0).equals(Preferences.getUserAccountName())) {

                    String eventName = cursor.getString(1);

                    // Start and end times
                    Date startT = new Date(cursor.getLong(3));
                    DateFormat formatter = new SimpleDateFormat("h:mm a");
                    String startTime = formatter.format(startT);

                    Date endT = new Date(cursor.getLong(4));
                    DateFormat formatterEnd = new SimpleDateFormat("h:mm a");
                    String endTime = formatterEnd.format(endT);

                    // Set times to bold
                    SpannableString eventTime = new SpannableString(startTime + " - " + endTime);
                    eventTime.setSpan(new StyleSpan(Typeface.BOLD), 0, eventTime.length(), 0);
                    /*
                    eventList.add("\n" + cursor.getString(1) + "\n" + startTime + " - " + endTime
                            + "\n" + cursorNames.getString(0) + "\n" + cursor.getString(0));
                    */

                    eventList.add("\n" + eventTime + "\n" + eventName + "\n");

                    Log.i(Constants.TAG, Arrays.toString(cursor.getColumnNames()));

                    //cursor.moveToNext();
                    //cursorNames.moveToNext();

                } else { //If calendar name isn't what is in preferences, move to next
                    //cursor.moveToNext();
                    //cursorNames.moveToNext();
                }
            } while (cursor.moveToNext());
        }
        return eventList;
    }
}