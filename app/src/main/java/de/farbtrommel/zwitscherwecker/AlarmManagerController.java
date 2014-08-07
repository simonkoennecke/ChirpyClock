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


public class AlarmManagerController implements SharedPreferences.OnSharedPreferenceChangeListener{
    final private Context _context;

    /**
     * toast
     */
    private Toast toast;
    /**
     * Alarm Settings
     */
    AlarmSettings _alarmSettings;

    public AlarmManagerController(Context context, AlarmSettings alarmSettings) {
        _context = context;
        _alarmSettings = alarmSettings;
    }

    /**
     * Setup Alarm clock
     */
    public void setAlarm(){
        if(!_alarmSettings.getBuzzerStatus() || (_alarmSettings.getRepeat() && _alarmSettings.getWeekdays().equals("0000000")))//Alarm clock is off
            return;

        AlarmManager am=(AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(_context, AlarmManagerReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(_context, 0, intent, 0);
        //TODO getNextAlarmTime doesn't return the right thing
        am.set(AlarmManager.RTC_WAKEUP, getNextAlarmTime(), pi); // Millisec * Second * Minute
    }

    /**
     * Clear the alarm clock
     */
    public void clearAlarm(){
        Intent intent = new Intent(_context, AlarmManagerReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(_context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    /**
     * Calculate the next alarm time and return the timestamp
     * @return timestamp of the next ring time
     */
    private long getNextAlarmTime(){
        final Calendar calendar = new GregorianCalendar();

        int dayOfTheWeek  =  calendar.get(Calendar.DAY_OF_WEEK) - 1;//weekday
        int dayOfTheMonth = calendar.get(Calendar.DATE);//day of month

        //Alarm time today pasted?
        int _time[] = _alarmSettings.getTime();
        if(calendar.get(Calendar.HOUR_OF_DAY) >= _time[0] && calendar.get(Calendar.MINUTE) > _time[1]){
            dayOfTheWeek += 1; //ring at next day
            dayOfTheMonth += 1;
        }

        String weekdays = (_alarmSettings.getRepeat())?_alarmSettings.getWeekdays():"1111111";
        //convert string
        //Sunday = 1, Monday = 2, .., Saturday=7
        weekdays = "x"+((weekdays.charAt(6)=='1')?"1":"0")+weekdays;

        //Find next valid Weekday
        while(true){
            if(weekdays.charAt(dayOfTheWeek) == '1')
                break;
            dayOfTheWeek = (dayOfTheWeek % 7) +1;
            dayOfTheMonth += 1;
        }
        GregorianCalendar wakeTime = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfTheMonth, _time[0], _time[1], 0);

        return wakeTime.getTimeInMillis();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("weekdays") ||s.equals("status") || s.equals("hour") || s.equals("minute") || s.equals("repeat")){
            clearAlarm();
            setAlarm();
            Log.d("AlarmManager",_alarmSettings.toString());

            //Toast Notifications
            try {toast.cancel();} catch (NullPointerException e){ }
            if(_alarmSettings.getRepeat() && _alarmSettings.getWeekdays().equals("0000000")){
                toast = Toast.makeText(_context, _context.getResources().getString(R.string.alarm_repeat_without_a_weekday), Toast.LENGTH_LONG);
                toast.show();
            }
            else if(s.equals("status") && !_alarmSettings.getBuzzerStatus()){
                toast = toast.makeText(_context, _context.getResources().getString(R.string.alarm_turned_off), Toast.LENGTH_SHORT);
                toast.show();
            }
            else if(s.equals("status")){
                toast = toast.makeText(_context, _context.getResources().getString(R.string.alarm_turned_on), Toast.LENGTH_LONG);
                toast.show();
            }

        }

    }
}
