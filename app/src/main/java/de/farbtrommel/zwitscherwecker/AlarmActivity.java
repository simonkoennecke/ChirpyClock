package de.farbtrommel.zwitscherwecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.ToggleButton;


public class AlarmActivity extends FragmentActivity implements TimePicker.OnTimeChangedListener, TextWatcher, View.OnClickListener, InformationFragment.OnFragmentInteractionListener {
    public static final String PREFS_NAME = "zwitscherPrefsFile";

    protected AlarmSettings _alarmSettings;

    protected AlarmManagerController alarmManager;

    protected TimePicker timepicker;
    protected EditText txtLabel;
    protected Switch switchStatus;
    protected CheckBox checkBox;
    protected LinearLayout weekdayContainer;
    protected ToggleButton weekdays[];
    protected View _infoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        _infoFragment = (View) findViewById(R.id.info_fragment);
        _infoFragment.setVisibility(View.INVISIBLE);


        //Connect to shared preferences store
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        _alarmSettings = new AlarmSettings(settings);
        alarmManager = new AlarmManagerController(this, _alarmSettings);
        settings.registerOnSharedPreferenceChangeListener(alarmManager);
        //Connect var and form element
        timepicker = (TimePicker) findViewById(R.id.timePicker);
        txtLabel = (EditText) findViewById(R.id.txtLabel);
        switchStatus = (Switch) findViewById(R.id.switchStatus);
        checkBox = (CheckBox) findViewById(R.id.boxRepeat);
        weekdayContainer = (LinearLayout) findViewById(R.id.weekdays);
        weekdays = new ToggleButton[]{
                (ToggleButton) findViewById(R.id.btnDayMon),
                (ToggleButton) findViewById(R.id.btnDayTue),
                (ToggleButton) findViewById(R.id.btnDayWed),
                (ToggleButton) findViewById(R.id.btnDayThu),
                (ToggleButton) findViewById(R.id.btnDayFri),
                (ToggleButton) findViewById(R.id.btnDaySat),
                (ToggleButton) findViewById(R.id.btnDaySun)};

        // Load form settings from shared preferences
        setSwitchStatus(_alarmSettings.getBuzzerStatus());
        setTime(_alarmSettings.getHour(),_alarmSettings.getMinute());
        setLabel(_alarmSettings.getLabel());
        setRepeat(_alarmSettings.getRepeat());
        setWeekdays(_alarmSettings.getWeekdays());

        timepicker.setIs24HourView(true);


        //Setup listener
        timepicker.setOnTimeChangedListener(this);
        txtLabel.addTextChangedListener(this);
        switchStatus.setOnClickListener(this);
        checkBox.setOnClickListener(this);
        for(ToggleButton btn : weekdays)
                btn.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setSwitchStatus();
        setTime();
        setLabel();
        setRepeat();
        setWeekdays();
    }
    protected String getWeekday(){
        String str = "";
        for(ToggleButton btn : weekdays){
            str += (btn.isChecked())?"1":"0";
        }
        return str;
    }
    protected void setWeekdays(){
        setWeekdays(getWeekday());
    }
    protected void setWeekdays(String str){
        for(int i=0; i < 7; i++){
            boolean bool = (str.charAt(i)=='1');
            if(!(bool == weekdays[i].isChecked()))
                weekdays[i].toggle();
        }
        _alarmSettings.setWeekdays(str);
    }

    protected Boolean getRepeat(){
        return checkBox.isChecked();
    }
    protected void setRepeat(){
        setRepeat(getRepeat());
    }
    protected void setRepeat(boolean status){
        if(!(checkBox.isChecked() == status))
            checkBox.toggle();

        if(checkBox.isChecked())
            weekdayContainer.setVisibility(View.VISIBLE);
        else
            weekdayContainer.setVisibility(View.INVISIBLE);

        _alarmSettings.setRepeat(status);

    }
    protected Boolean getSwitchStatus(){
        return switchStatus.isChecked();
    }
    protected void setSwitchStatus(){
        setSwitchStatus(getSwitchStatus());
    }
    protected void setSwitchStatus(boolean status){
        if(!(status == switchStatus.isChecked()))
            switchStatus.toggle();

        _alarmSettings.setStatus(status);
    }
    protected String getLabel(){
        return txtLabel.getText().toString();
    }
    protected void setLabel(){
        String label = getLabel();
        _alarmSettings.setLabel(label);
    }
    protected void setLabel(String label){
        if(!label.equals(txtLabel.toString()))
            txtLabel.setText(label);
    }
    protected int[] getTime(){
        return new int[]{timepicker.getCurrentHour(),timepicker.getCurrentMinute()};
    }
    protected void setTime(){
        int[] t = getTime();
        setTime(t[0],t[1]);
    }
    protected void setTime(int hour, int minute){
        timepicker.setCurrentHour(hour);
        timepicker.setCurrentMinute(minute);
        if(!(_alarmSettings.getHour() == hour) || !(_alarmSettings.getMinute()==minute)) {
            _alarmSettings.setTime(hour,minute);
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
            _infoFragment.setVisibility(View.VISIBLE);
            return true;
        }
        else if (id == R.id.action_start_quiz) {
            startActivity(new Intent(this,QuizActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i2) {
        setTime(i,i2);
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
    @Override
    public void afterTextChanged(Editable editable) { setLabel();  }
    @Override
    public void onClick(View view) {
        setSwitchStatus();
        setRepeat();
        setWeekdays();
    }
    @Override
    public void onBackPressed() {
        if(_infoFragment.getVisibility() == View.VISIBLE) {
            _infoFragment.setVisibility(View.INVISIBLE);
        }
        else{
            finish();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
