package de.farbtrommel.zwitscherwecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends FragmentActivity implements TimePicker.OnTimeChangedListener,
        TextWatcher, View.OnClickListener, InformationFragment.OnFragmentInteractionListener {

    protected SettingsStorage mSettingsStorage;

    protected AlarmController mAlarmManager;

    protected TimePicker mTimepicker;
    protected EditText mTxtLabel;
    protected Switch mSwitchStatus;
    protected CheckBox mCheckBox;
    protected LinearLayout mWeekdayContainer;
    protected ToggleButton[] mWeekdays;
    protected View mInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        mInfoFragment = (View) findViewById(R.id.info_fragment);
        mInfoFragment.setVisibility(View.INVISIBLE);


        //Connect to shared preferences store
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsStorage = new SettingsStorage(settings);
        mAlarmManager = new AlarmController(this, mSettingsStorage);
        settings.registerOnSharedPreferenceChangeListener(mAlarmManager);
        //Connect var and form element
        mTimepicker = (TimePicker) findViewById(R.id.timePicker);
        mTxtLabel = (EditText) findViewById(R.id.txtLabel);
        mSwitchStatus = (Switch) findViewById(R.id.switchStatus);
        mCheckBox = (CheckBox) findViewById(R.id.boxRepeat);
        mWeekdayContainer = (LinearLayout) findViewById(R.id.weekdays);
        mWeekdays = new ToggleButton[]{
                (ToggleButton) findViewById(R.id.btnDayMon),
                (ToggleButton) findViewById(R.id.btnDayTue),
                (ToggleButton) findViewById(R.id.btnDayWed),
                (ToggleButton) findViewById(R.id.btnDayThu),
                (ToggleButton) findViewById(R.id.btnDayFri),
                (ToggleButton) findViewById(R.id.btnDaySat),
                (ToggleButton) findViewById(R.id.btnDaySun)};

        // Load all information form shared preferences
        setSwitchStatus(mSettingsStorage.getBuzzerStatus());
        setTime(mSettingsStorage.getHour(), mSettingsStorage.getMinute());
        setLabel(mSettingsStorage.getLabel());
        setRepeat(mSettingsStorage.getRepeat());
        setWeekdays(mSettingsStorage.getWeekdays());

        if (Locale.getDefault().equals(Locale.GERMANY)) {
            mTimepicker.setIs24HourView(true);
        } else {
            mTimepicker.setIs24HourView(false);
        }


        //Setup listener
        mTimepicker.setOnTimeChangedListener(this);
        mTxtLabel.addTextChangedListener(this);
        mTxtLabel.setOnClickListener(this);
        mSwitchStatus.setOnClickListener(this);
        mCheckBox.setOnClickListener(this);
        for (ToggleButton btn : mWeekdays)
                btn.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Display the next alarm time
        displayNextAlarm();

        //Save alle Settings
        setSwitchStatus();
        setTime();
        setLabel();
        setRepeat();
        setWeekdays();
    }
    protected String getWeekday() {
        String str = "";
        for (ToggleButton btn : mWeekdays) {
            str += (btn.isChecked()) ? "1" : "0";
        }
        return str;
    }
    protected void setWeekdays() {
        setWeekdays(getWeekday());
    }
    protected void setWeekdays(String str) {
        for (int i = 0; i < 7; i++) {
            boolean bool = (str.charAt(i) == '1');
            if (!(bool == mWeekdays[i].isChecked()))
                mWeekdays[i].toggle();
        }
        mSettingsStorage.setWeekdays(str);
    }

    protected Boolean getRepeat() {
        return mCheckBox.isChecked();
    }
    protected void setRepeat() {
        setRepeat(getRepeat());
    }
    protected void setRepeat(boolean status) {
        if (!(mCheckBox.isChecked() == status))
            mCheckBox.toggle();

        if (mCheckBox.isChecked())
            mWeekdayContainer.setVisibility(View.VISIBLE);
        else
            mWeekdayContainer.setVisibility(View.INVISIBLE);

        mSettingsStorage.setRepeat(status);

    }
    protected Boolean getSwitchStatus() {
        return mSwitchStatus.isChecked();
    }
    protected void setSwitchStatus() {
        setSwitchStatus(getSwitchStatus());
    }
    protected void setSwitchStatus(boolean status) {
        if (!(status == mSwitchStatus.isChecked()))
            mSwitchStatus.toggle();

        mSettingsStorage.setStatus(status);
    }
    protected String getLabel() {
        return mTxtLabel.getText().toString();
    }
    protected void setLabel() {
        String label = getLabel();
        mSettingsStorage.setLabel(label);
    }
    protected void setLabel(String label) {
        if (!label.equals(mTxtLabel.toString()))
            mTxtLabel.setText(label);
    }
    protected int[] getTime() {
        return new int[]{mTimepicker.getCurrentHour(), mTimepicker.getCurrentMinute()};
    }
    protected void setTime() {
        int[] t = getTime();
        setTime(t[0], t[1]);
    }
    protected void setTime(int hour, int minute) {
        mTimepicker.setCurrentHour(hour);
        mTimepicker.setCurrentMinute(minute);
        if (!(mSettingsStorage.getHour() == hour) || !(mSettingsStorage.getMinute() == minute)) {
            mSettingsStorage.setTime(hour, minute);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_information) {
            mInfoFragment.setVisibility(View.VISIBLE);
            return true;
        } else if (id == R.id.action_start_quiz) {
            Intent intentStartQuizActivity = new Intent(this, QuizActivity.class);
            intentStartQuizActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentStartQuizActivity.putExtra(QuizActivity.ARG_RUN_FLAG,
                    QuizActivity.ARG_RUN_FLAG_TRAIN);
            startActivity(intentStartQuizActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i2) {
        setTime(i, i2);
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
    @Override
    public void afterTextChanged(Editable editable) { setLabel();  }
    @Override
    public void onClick(View view) {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        if (view.getId() == R.id.txtLabel) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            }).start();
        } else {
            setSwitchStatus();
            setRepeat();
            setWeekdays();
        }
    }

    @Override
    public void onBackPressed() {
        if (mInfoFragment.getVisibility() == View.VISIBLE) {
            mInfoFragment.setVisibility(View.INVISIBLE);
        } else {
            finish();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void displayNextAlarm() {

        Calendar calendar = new GregorianCalendar();
        long diffInMillisec = mSettingsStorage.getNextAlarmTime() - calendar.getTimeInMillis();
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec);
        diffInSec /= 60;
        long minutes = diffInSec % 60;
        diffInSec /= 60;
        long hours = diffInSec % 24;
        diffInSec /= 24;
        long days = diffInSec;

        String pattern = "";
        if (days == 0 && hours == 0 && minutes <= 1) {
            minutes = (minutes == 0) ? 1 : minutes;
            pattern = getResources().getString(R.string.alarm_show_min, minutes);
        } else if (days == 0 && hours == 0 && minutes > 1) {
            pattern = getResources().getString(R.string.alarm_show_mins, minutes);
        } else if (days == 0 && hours == 1 && minutes > 1) {
            pattern = getResources().getString(R.string.alarm_show_hour_mins, hours, minutes);
        } else if (days == 0 && hours > 1 && minutes > 1) {
            pattern = getResources().getString(R.string.alarm_show_hours_mins, hours, minutes);
        } else if (days == 1 && hours > 1 && minutes > 1) {
            pattern = getResources().getString(R.string.alarm_show_day_hour_mins,
                    days, hours, minutes);
        } else if (days > 1 && hours > 1 && minutes > 1) {
            pattern = getResources().getString(R.string.alarm_show_days_hours_mins,
                    days, hours, minutes);
        }

        if (!(pattern == ""))
            Toast.makeText(this, pattern, Toast.LENGTH_SHORT).show();
    }
}

