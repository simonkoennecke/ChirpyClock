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

public class SettingsStorage extends AsyncTask<String, String, String> {
    /**
     * SharedPreferences reference.
     */
    SharedPreferences mSharedPref;
    /**
     * Default alarm status is inaktive.
     */
    private static final boolean STATUS = false;
    /**
     * Repeat the alarm clock.
     */
    private static final boolean REPEAT = false;
    /**
     * Select specific mWeekdays when the alarm should ring.
     */
    private static final String WEEKDAYS = "0000000";
    /**
     * Default alarm time {hour, minute}.
     */
    private static final int[] TIME = new int[]{12, 0};
    /**
     * Default IS_RING value.
     */
    private static final boolean IS_RING = false;

    public SettingsStorage(SharedPreferences pref) {
        mSharedPref = pref;
        if (mSharedPref.getString("id", "NA") == "NA") {
            this.execute();
        }
    }
    protected String getId() {
        return mSharedPref.getString("id", "NA");
    }
    protected boolean getRing() {
        return mSharedPref.getBoolean("ring", IS_RING);
    }
    protected void setRing(boolean ring) {
        if (!(getRing() == ring)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("ring", ring);
            editor.commit();
        }
    }

    protected String getWeekdays() {
        return mSharedPref.getString("weekdays", WEEKDAYS);
    }

    protected void setWeekdays(String str) {
        if (!getWeekdays().equals(str)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString("weekdays", str);
            editor.commit();
        }
    }

    protected Boolean getRepeat() {
        return mSharedPref.getBoolean("repeat", REPEAT);
    }
    protected void setRepeat(boolean status) {
         if (!(getRepeat() == status)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("repeat", status);
            editor.commit();
        }
    }

    protected long getNextAlarmTime() {
        return mSharedPref.getLong("alarmTime", 0);
    }
    protected void setNextAlarmTime(long alarmTime) {
        if (!(getNextAlarmTime() == alarmTime)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putLong("alarmTime", alarmTime);
            editor.commit();
        }
    }

    protected Boolean getBuzzerStatus() {
        return mSharedPref.getBoolean("status", STATUS);
    }


    protected void setStatus(boolean status) {
        if (!(getBuzzerStatus() == status)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("status", status);
            editor.commit();
        }
    }
    protected String getLabel() {
        return mSharedPref.getString("label", "");
    }

    protected void setLabel(String label) {
        if (!getLabel().equals(label)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString("label", label);
            editor.commit();
        }
    }
    protected int[] getTime() {
        return new int[]{mSharedPref.getInt("hour", TIME[0]),
                mSharedPref.getInt("minute", TIME[1])};
    }

    protected void setTime(int hour, int minute) {
        if (!(getHour() == hour) || !(getMinute() == minute)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.commit();
        }
    }

    protected int getHour() {
        return mSharedPref.getInt("hour", TIME[0]);
    }

    protected void setHour(int hour) {
        if (!(getHour() == hour)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt("hour", hour);
            editor.commit();
        }
    }
    protected int getMinute() {
        return mSharedPref.getInt("minute", TIME[1]);
    }

    protected void setMinute(int minute) {
        if (!(getMinute() == minute)) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt("minute", minute);
            editor.commit();
        }
    }
    public String toString() {
        return "DeviceId:" + getId() + ", Repeat:" + getRepeat()
                + ", Status:" +  getBuzzerStatus() + ", Hour:" + getHour()
                + ", Minute:" + getMinute() + ", Weekday:" + getWeekdays();
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
            writer.write(SettingsStorage.getQuery(params));
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
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString("mId", inputLine);
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

    static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
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
