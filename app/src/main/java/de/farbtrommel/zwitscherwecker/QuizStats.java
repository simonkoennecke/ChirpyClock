package de.farbtrommel.zwitscherwecker;

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class QuizStats extends AsyncTask<String, String, String> {
    private Date mStart = new Date();
    private Date mEnd = null;
    private int mClickCount = 0;
    private String mDeviceId;
    private int mCorrectAnswer;
    private int[] mPossible;

    public QuizStats() {

    }
    public QuizStats(int correctAnswer, int[] possible, String deviceId) {
        this.mDeviceId = deviceId;
        this.mCorrectAnswer = correctAnswer;
        this.mPossible = possible;
    }

    public int getClickCount() {
        return mClickCount;
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
            params.add(new BasicNameValuePair("addstats", "addstats"));
            params.add(new BasicNameValuePair("start", strings[0]));
            params.add(new BasicNameValuePair("end", strings[1]));
            params.add(new BasicNameValuePair("clickcount", strings[2]));
            params.add(new BasicNameValuePair("answer", strings[3]));
            params.add(new BasicNameValuePair("options", strings[4]));
            params.add(new BasicNameValuePair("deviceid", strings[5]));

            String strparam  = SettingsStorage.getQuery(params);
            Log.d("Stats", strparam);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(strparam);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

            }
                //System.out.println(inputLine);
            in.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void onPostExecute(String result) {

    }

    public void incClickCounter() {
        mClickCount++;
    }

    public void transmit(boolean nextRound) {
        if (mEnd == null)
            mEnd = new Date();

        new QuizStats().execute(
                String.valueOf(mStart.getTime()),
                String.valueOf(((nextRound) ? 0 : mEnd.getTime())),
                String.valueOf(mClickCount),
                String.valueOf(mCorrectAnswer),
                String.valueOf(mPossible[0]) + "," + String.valueOf(mPossible[1])
                        + "," + String.valueOf(mPossible[2]) + "," + String.valueOf(mPossible[3]),
                mDeviceId
                );

    }

}
