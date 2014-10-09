package com.tovsv.timmy.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.tovsv.timmy.R;
import com.tovsv.timmy.app.Constants;
import com.tovsv.timmy.model.AppAction;
import com.tovsv.timmy.model.AppRecord;
import com.tovsv.timmy.receiver.ScreenActionReceiver;
import com.tovsv.timmy.util.AppActionHelper;
import com.tovsv.timmy.util.AppListener;
import com.tovsv.timmy.util.DLog;

import java.util.Calendar;
import java.util.List;

public class AppListenerService extends Service {

    public final static String KEY_COMMAND = "key_command";
    public final static String COMMAND_TRICK_APP = "command_trick_app";
    public final static String COMMAND_START_TRICK = "command_start_trick";
    public final static String COMMAND_STOP_TRICK = "command_stop_trick";

    private AlarmManager mAlarmManager;
    private NotificationManager mNotificationManager;
    private PendingIntent mTrickPendingIntent;
    private NotificationCompat.Builder mBuilder;
    private AppListener mAppListener;

    //private Boolean isFirstStart = false;
    private int num = 0;

    public AppListenerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppListener = new AppListener(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);

        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("当前应用")
                        .setContentText("now" + num);

        startForeground(5, mBuilder.build());

        registerScreenActionReceiver();

    }

    @Override
    public void onDestroy() {
        stopListener();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra(KEY_COMMAND);

        if (COMMAND_START_TRICK.equals(command)) {
            startListener();
        } else if (COMMAND_TRICK_APP.equals(command)) {
            trickForegroundApp();
        } else if (COMMAND_STOP_TRICK.equals(command)){
            stopListener();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerScreenActionReceiver(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new ScreenActionReceiver(), filter);
    }

    private void startListener() {
        Intent intent = new Intent(this, AppListenerService.class);
        intent.putExtra(KEY_COMMAND, COMMAND_TRICK_APP);
        mTrickPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggertTime = SystemClock.elapsedRealtime();
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggertTime, Constants.TRICK_INTERVAL, mTrickPendingIntent);
    }

    private void stopListener() {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mTrickPendingIntent);
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                AppActionHelper.stop();
                return "";
            }
        }.execute();
    }

    private void exitListener () {
        stopListener();
        mAlarmManager = null;
        stopForeground(true);
        onDestroy();
    }

    private void trickForegroundApp() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String packageName = mAppListener.getForegroundPackage();
                AppActionHelper.increaseData(packageName);
                num = num + 1;
                return packageName;
            }
            @Override
            protected void onPostExecute(String result) {
                if (mBuilder != null) {
                    mBuilder.setContentText("" + num + result);
                    mNotificationManager.notify(5, mBuilder.build());
                }
            }
        }.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
