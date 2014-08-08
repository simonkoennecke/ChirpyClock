package de.farbtrommel.zwitscherwecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;


public class AlarmManagerReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("zwitscherwechker","Alarm received!");
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "zwitscherwecker");
        wl.acquire();
            Intent intentStartStartActivity = new Intent(context, QuizActivity.class);
            intentStartStartActivity.putExtra(QuizActivity.ARG_RUN_FLAG, QuizActivity.ARG_RUN_FLAG_ALARM);
            intentStartStartActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK );
            context.startActivity(intentStartStartActivity);
        wl.release();
    }
}
