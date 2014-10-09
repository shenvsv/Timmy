package com.tovsv.timmy.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.tovsv.timmy.model.AppAction;
import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.model.AppRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by kleist on 14-5-24.
 */
public class AppRecordHelper {

    private PackageManager mPackageManager;

    public AppRecordHelper(Context context) {
        mPackageManager = context.getPackageManager();
    }

    public AppInfoList loadAppsByDate(Calendar date) {
        List<AppRecord> appRecords = AppActionHelper.getRecordByDate(date);
        AppInfoList appInfos = new AppInfoList(appRecords.size());
        long totleTime = 0;
        for (AppRecord appRecord: appRecords) {
            AppInfo appInfo = new AppInfo();
            PackageInfo packageInfo = getPkgInfoByPkgName(appRecord.packageName);

            if (packageInfo == null) {
                continue;
            }
            long time = appRecord.duration / 1000;
            appInfo.appName = mPackageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
            appInfo.packageName = appRecord.packageName;
            appInfo.setTime(time);
            appInfos.add(appInfo);
            totleTime = totleTime + time;
        }
        appInfos.setTotleTime(totleTime);
        return appInfos;
    }

    private PackageInfo getPkgInfoByPkgName(String pkgName) {
        PackageInfo info;
        try {
            info = mPackageManager.getPackageInfo(pkgName, 0);
            return info;
        } catch (PackageManager.NameNotFoundException e) {
            DLog.e(String.format("package %1$s not found", pkgName));
            return null;
        }
    }
}
