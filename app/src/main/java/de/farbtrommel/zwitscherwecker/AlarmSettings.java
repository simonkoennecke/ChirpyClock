package de.farbtrommel.zwitscherwecker;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AlarmSettings extends AsyncTask<String, String, String> {
    SharedPreferences settings;
    /**
     * Alarm: Active or inactive?
     */
    private final static boolean STATUS = false;
    /**
     * Repeat the alarm clock
     */
    private final static boolean REPEAT = false;
    /**
     * Select specific weekdays when the alarm should ring.
     */
    private final static String WEEKDAYS = "0000000";
    /**
     * Alarm time {hour, minute}
     */
    private final static int[] TIME = new int[]{12, 0};
    /**
     *
     */
    private final static boolean IS_RING = false;

    public AlarmSettings(SharedPreferences settings){
        this.settings = settings;
        if(settings.getString("id", "NA") == "NA"){
            this.execute();
        }
    }
    protected String getId(){
        return settings.getString("id", "NA");
    }
    protected boolean getRing() {
        return settings.getBoolean("ring", IS_RING);
    }
    protected void setRing(boolean ring){
        if(!(getRing() == ring)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("ring", ring);
            editor.commit();
        }
    }

    protected String getWeekdays(){
        return settings.getString("weekdays", WEEKDAYS);
    }

    protected void setWeekdays(String str){
        if(!getWeekdays().equals(str)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("weekdays", str);
            editor.commit();
        }
    }

    protected Boolean getRepeat(){
        return settings.getBoolean("repeat",REPEAT);
    }
    protected void setRepeat(boolean status){
        if(!(getRepeat() == status)){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("repeat", status);
            editor.commit();
        }

    }
    protected Boolean getBuzzerStatus(){
        return settings.getBoolean("status",STATUS);
    }


    protected void setStatus(boolean status){
        if(!(getBuzzerStatus() == status)){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("status", status);
            editor.commit();
        }
    }
    protected String getLabel(){
        return settings.getString("label","");
    }

    protected void setLabel(String label){
        if(!getLabel().equals(label)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("label", label);
            editor.commit();
        }
    }
    protected int[] getTime(){
        return new int[]{settings.getInt("hour", TIME[0]), settings.getInt("minute", TIME[1])};
    }

    protected void setTime(int hour, int minute){
        if(!(getHour() == hour) || !(getMinute()==minute)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.commit();
        }
    }

    protected int getHour(){
        return settings.getInt("hour", TIME[0]);
    }

    protected void setHour(int hour){
        if(!(getHour() == hour)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("hour", hour);
            editor.commit();
        }
    }
    protected int getMinute(){
        return settings.getInt("minute", TIME[1]);
    }

    protected void setMinute(int minute){
        if(!(getMinute()==minute)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("minute", minute);
            editor.commit();
        }
    }
    public String toString(){
        return "DeviceId:"+getId()+", Repeat:"+getRepeat()+", Status:"+ getBuzzerStatus()+", Hour:"+getHour()+", Minute:"+getMinute()+", Weekday:"+getWeekdays();
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(BuildConfig.REST_URI);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("getid", "simple"));

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(AlarmSettings.getQuery(params));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            //Read simple answer from server. Just return a number
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            inputLine = in.readLine();
            in.close();

            //Store Device Id to Key Value Database
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("id", inputLine );
            editor.commit();

            return inputLine;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
