package org.main.smartmirror.smartmirror;

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

    /**
     * Mira tells the user how many unread emails are in their inbox
     */
    private void sayUnreadEmails() {
        // TODO: get the # of unread mails from gmailHomeFragment
        //String msg = "You have " + mActivity.getUnreadEmailCount() + " unread emails.";
        //mActivity.speakText(msg);
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
        if (mLastWakeTime - mLastSleepTime > 60000) {
            sayTimeGreeting();
            sayUnreadEmails();
        }
    }
}
