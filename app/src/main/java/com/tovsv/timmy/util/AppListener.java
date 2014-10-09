package com.tovsv.timmy.util;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by kleist on 14-5-24.
 */
public class AppListener {

    private Context mContext;
    private ActivityManager mActivityManager;

    public AppListener(Context context) {
        mContext = context;
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public String getForegroundPackage() {
        ActivityManager.RunningTaskInfo runningTaskInfo = mActivityManager.getRunningTasks(1).get(0);
        return runningTaskInfo.topActivity.getPackageName();
    }

}
