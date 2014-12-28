package com.tovsv.timmy.util;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kleist on 14-5-24.
 */
public class AppListener {

    private Context mContext;
    private ActivityManager mActivityManager;
    private UsageStatsManager manager;
    private List<UsageStats> lastUsageStats;

    public AppListener(Context context) {
        mContext = context;
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        manager = (UsageStatsManager)context.getSystemService("usagestats");
    }

    public String getForegroundPackage() {
        return getPackage();
    }

    public String getPackageCompat(){

        ActivityManager.RunningTaskInfo runningTaskInfo = mActivityManager.getRunningTasks(1).get(0);
        ActivityManager.RunningAppProcessInfo info = mActivityManager.getRunningAppProcesses().get(0);
        return runningTaskInfo.topActivity.getPackageName();

    }
    // use in 5.0
    public String getPackage(){

        Calendar beginCal = Calendar.getInstance();
        beginCal.set(Calendar.DATE, 1);
        beginCal.set(Calendar.MONTH, 0);
        beginCal.set(Calendar.YEAR, 2012);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.DATE, 1);
        endCal.set(Calendar.MONTH, 0);
        endCal.set(Calendar.YEAR, 2015);

        List<UsageStats> queryUsageStats=manager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());

        UsageStats nS = null;
        for (UsageStats stats : queryUsageStats){
            //DLog.i("//xxx"+stats.getPackageName());
            if (nS == null){
                nS = stats;
            }else {
                if (nS.getLastTimeUsed() < stats.getLastTimeUsed()){
                    nS = stats;
                }
            }
        }
        if (nS == null){
            return "null";
        }else {
            return nS.getPackageName();
        }

    }

}
