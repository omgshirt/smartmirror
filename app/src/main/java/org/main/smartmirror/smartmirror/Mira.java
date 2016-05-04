package org.main.smartmirror.smartmirror;

import android.util.Log;

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

/**
 * Mira singleton.
 */
public class Mira {

    private MainActivity mActivity;
    private static Mira mira;
    private static Preferences mPreferences;
    private long mLastSleepTime = 0;
    private long mLastWakeTime = 0;

    private Mira(MainActivity activity) {
        mActivity = activity;
    }

    public static Mira getInstance(MainActivity activity) {
        if (mira == null) {
            mira = new Mira(activity);
            mPreferences = Preferences.getInstance(activity);
        }
        return mira;
    }

    /**
     * Speak a random message about sleeping
     */
    private void saySleepMessage() {
        Random random = new Random();
        String voice;
        float rand = random.nextFloat();
        if (rand < .15)
            voice = "Will I dream?";
        else if (rand < .3)
            voice = "Very well.";
        else if (rand < .5)
            voice = "Good night.";
        else if (rand < .7)
            voice = "See you soon.";
        else if (rand < .9)
            voice = "I'll charge up for a bit.";
        else
            voice = "Finally, some rest.";
        mActivity.speakText(voice);
    }


    /**
     * Speak a greeting message based on the current time of day
     */
    private void sayTimeGreeting() {
        int hour = GregorianCalendar.getInstance().get(GregorianCalendar.HOUR_OF_DAY);
        String msg;
        String userName = mPreferences.getUserFirstName();

        if (hour < 12 ) {
            msg = mActivity.getResources().getString(R.string.mira_greet_morning);
        } else if (hour < 19) {
            msg = mActivity.getResources().getString(R.string.mira_greet_afternoon);
        } else {
            msg = mActivity.getResources().getString(R.string.mira_greet_night);
        }

        mActivity.speakText(String.format( msg, userName ));
     }


    public void sayCurrentTime() {
        GregorianCalendar calendar = new GregorianCalendar();
        String strMinute, strHour;

        int hourMode = Calendar.HOUR_OF_DAY;
        if (mPreferences.isTimeFormat12hr()) {
            hourMode = Calendar.HOUR;
        }
        strHour = Integer.toString(calendar.get(hourMode));

        int minute = calendar.get(Calendar.MINUTE);
        strMinute = Integer.toString(minute);

        // handle times > :00 and < :10
        if (minute > 0 && minute < 10) {
            strMinute = ":0" + strMinute;
        } else if (minute == 0) {
            strMinute = " ";
        } else {
            strMinute = ":" + strMinute;
        }

        // add AM / PM as necessary
        if (mPreferences.isTimeFormat12hr()) {
            String AM_PM = " A M";
            if (calendar.get(Calendar.AM_PM) == 1) {
                AM_PM = " P M";
            }
            strMinute = strMinute + AM_PM;
        }

        String result = "the time is " + strHour + strMinute;
        Log.i(Constants.TAG, "time: " + result);
        mActivity.speakText(result);
    }

    /**
     * If logged in to a gmail account, Mira says the number of unread messages.
     */
    private void sayUnreadEmails() {

        if (mPreferences.isLoggedInToGmail()) {
            int unreadCount = mActivity.getUnreadCount();

            if (unreadCount > 0) {
                String plural = "";
                if (unreadCount > 1) plural = "s";
                String msg = String.format(Locale.US, "You have %d unread email%s.", unreadCount, plural);
                mActivity.speakText(msg);
            }
        }
    }

    /**
     * Called when the mirror is transitioning to sleep.
     * Track the time and say a message (if appropriate)
     */
    public void appSleeping() {
        mLastSleepTime = System.currentTimeMillis();
        saySleepMessage();
    }

    /**
     * Called when the mirror is moving from LIGHT_SLEEP or ASLEEP to AWAKE.
     * Track the time and respond with appropriate message.
     */
    public void appWaking() {
        mLastWakeTime = System.currentTimeMillis();

        // Say messages only if the mirror has been asleep for more than a minute
        if (mLastWakeTime - mLastSleepTime > 6) {
            sayTimeGreeting();
            sayUnreadEmails();
        }
    }

    public void appWakingSilently(){
        mLastWakeTime = System.currentTimeMillis();
    }
}
