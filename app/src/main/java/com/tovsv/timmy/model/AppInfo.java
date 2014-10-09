package com.tovsv.timmy.model;

/**
 * Created by kleist on 14-5-24.
 */
public class AppInfo {

    public String packageName;

    public String appName;

    public long duration;

    public String time;

    public void setTime(long time) {
        this.duration = time;
        this.time = formatTime(time);
        this.packageName = packageName;
    }

    private String formatTime(long second) {
        long hour = second / 3600;
        second %= 3600;
        long mintue = second / 60;
        second = second % 60;

        if (hour == 0) {
            if (mintue < 1) {
                mintue = 1;
            }
            return String.format("%1$dm %2$2ds", mintue, second);
        } else {
            return String.format("%1$dh %2$2dm", hour, mintue);
        }
    }
}
