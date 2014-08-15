package de.farbtrommel.zwitscherwecker;

import android.app.AlarmManager;
import android.content.Context;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class AlarmController implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Context mContext;

    /**
     * Toast instance.
     */
    private Toast mToast;
    /**
     * Alarm Settings.
     */
    SettingsStorage mSettingsStorage;

    public AlarmController(Context context, SettingsStorage settingsStorage) {
        mContext = context;
        mSettingsStorage = settingsStorage;
    }

    /**
     * Setup Alarm clock.
     */
    public void setAlarm() {
        if (!mSettingsStorage.getBuzzerStatus() || (mSettingsStorage.getRepeat()
                && mSettingsStorage.getWeekdays().equals("0000000"))) //Alarm clock is off
            return;

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        long time = getNextAlarmTime();
        mSettingsStorage.setNextAlarmTime(time);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    /**
     * Clear the alarm clock.
     */
    public void clearAlarm() {
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    /**
     * Calculate the next alarm time and return the timestamp.
     * @return timestamp of the next ring time
     */
    private long getNextAlarmTime() {
        final Calendar calendar = new GregorianCalendar();

        int dayOfTheWeek  =  calendar.get(Calendar.DAY_OF_WEEK) - 1; //weekday
        int dayOfTheMonth = calendar.get(Calendar.DATE); //day of month

        //Alarm time today pasted?
        int[] time = mSettingsStorage.getTime();
        if (//ok the alarm is for the next day, when
           calendar.get(Calendar.HOUR_OF_DAY) > time[0] || // the hour is past
          (calendar.get(Calendar.HOUR_OF_DAY) == time[0]
                  && calendar.get(Calendar.MINUTE) > (time[1] - 1)) //same hour but the min. in past
        ) {
            dayOfTheWeek += 1; //ring on next day
            dayOfTheMonth += 1;
        }

        String weekdays = (mSettingsStorage.getRepeat()) ? mSettingsStorage.getWeekdays() : "1111111";
        //convert string
        //Sunday = 1, Monday = 2, .., Saturday=7
        weekdays = "" + ((weekdays.charAt(6) == '1') ? "1" : "0") + weekdays;

        //Find next valid Weekday
        while (true) {
            if (weekdays.charAt(dayOfTheWeek) == '1')
                break;
            dayOfTheWeek = (dayOfTheWeek % 7) + 1;
            dayOfTheMonth += 1;
        }
        GregorianCalendar wakeTime = new GregorianCalendar(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), dayOfTheMonth, time[0], time[1], 0);
        return wakeTime.getTimeInMillis();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("weekdays") || s.equals("status") || s.equals("hour")
                || s.equals("minute") || s.equals("repeat")) {
            clearAlarm();
            setAlarm();
            Log.d("AlarmManager", mSettingsStorage.toString());

            //Toast Notifications
            try {
                mToast.cancel();
            } catch (NullPointerException e) {
            }

            if (mSettingsStorage.getRepeat() && mSettingsStorage.getWeekdays().equals("0000000")) {
                mToast = Toast.makeText(mContext,
                        mContext.getResources().getString(R.string.alarm_repeat_without_a_weekday),
                        Toast.LENGTH_LONG);
                mToast.show();
            } else if (s.equals("status") && !mSettingsStorage.getBuzzerStatus()) {
                mToast = mToast.makeText(mContext,
                        mContext.getResources().getString(R.string.alarm_turned_off),
                        Toast.LENGTH_SHORT);
                mToast.show();
            } else if (s.equals("status")) {
                mToast = mToast.makeText(mContext,
                        mContext.getResources().getString(R.string.alarm_turned_on),
                        Toast.LENGTH_LONG);
                mToast.show();
            }
        }

    }
}
