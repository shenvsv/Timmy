package com.tovsv.timmy.util;

import android.content.pm.PackageInfo;

import com.tovsv.timmy.model.AppAction;
import com.tovsv.timmy.model.AppRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.Query;

/**
 * Created by shenvsv on 14-9-22.
 */
public class AppActionHelper {
    private static String packageName = "";
    private static Calendar startTime = Calendar.getInstance();

    public static void increaseData(String mPackageName){
        Calendar time = Calendar.getInstance();
        if (!mPackageName.equals(packageName)){

            if (!packageName.equals("")){
                AppAction.update(startTime, time, packageName);
            }
            packageName = mPackageName;
            startTime = time;
            //loadAppByDate(Calendar.getInstance());
        }else if (!formatDate(time).equals(formatDate(startTime))){
            changeDate(time);
        }
    }

    public static void changeDate(Calendar time){
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, -1);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.SECOND, 59);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.MILLISECOND, 0);

        AppAction.update(startTime, endTime, packageName);
        endTime.add(Calendar.MINUTE, +1);

        startTime = time;
    }

    public static void stop(){
        Calendar endTime = Calendar.getInstance();
        if (!packageName.equals("")){
            AppAction.update(startTime, endTime, packageName);
        }
        packageName = "";
        startTime = null;
    }

    //data
    public static List<AppRecord>  getRecordByDate(Calendar date){
        CursorList<AppRecord> appRecords = Query.many(AppRecord.class,
                "SELECT package_name,sum(duration) FROM app_action WHERE date = ? GROUP BY package_name ORDER BY sum(duration) DESC",formatDate(date))
                .get();
        List<AppRecord> list = appRecords.asList();
        appRecords.close();
        return list;
    }

    public static String formatDate(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(calendar.getTime());
    }
}
