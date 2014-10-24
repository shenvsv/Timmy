package com.tovsv.timmy.model;

import com.tovsv.timmy.util.AppActionHelper;
import com.tovsv.timmy.util.DLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by shenvsv on 14-9-21.
 */
@Table("app_action")
public class AppAction extends Model{
    @AutoIncrementPrimaryKey
    @Column("id")
    public long id;

    @Column("start_time")
    public long startTime;

    @Column("end_time")
    public long endTime;

    @Column("package_name")
    public String packageName;

    @Column("duration")
    public long duration;

    @Column("date")
    public String date;

    public static void update(Calendar startTime, Calendar endTime, String packageName){

        AppAction appAction = new AppAction();
        long duration = endTime.getTimeInMillis() - startTime.getTimeInMillis() + 1000;
        appAction.startTime = startTime.getTimeInMillis();
        appAction.endTime = endTime.getTimeInMillis();
        appAction.packageName = packageName;
        appAction.duration = duration;
        appAction.date = AppActionHelper.formatDate(startTime);
        DLog.i(duration+"//");
        appAction.save();

    }

    public static String formatTime(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(calendar.getTime());
    }
}
