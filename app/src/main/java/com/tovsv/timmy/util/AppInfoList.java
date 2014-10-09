package com.tovsv.timmy.util;

import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.model.AppRecord;

import java.util.ArrayList;


/**
 * Created by shenvsv on 14-10-2.
 */
public class AppInfoList extends ArrayList<AppInfo>{
    private long totleTime = 0;

    public AppInfoList(int size) {
        super(size);
    }

    public AppInfoList() {

    }

    public void setTotleTime(long totleTime){
        this.totleTime = totleTime;
    }

    public long getTotleTime(){
        return totleTime;
    }

    public void clean(){
        totleTime = 0;
        super.clear();
    }
}
