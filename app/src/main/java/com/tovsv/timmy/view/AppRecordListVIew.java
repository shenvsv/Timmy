package com.tovsv.timmy.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tovsv.timmy.R;
import com.tovsv.timmy.adapter.AppRecordAdapter;

/**
 * Created by shenvsv on 14-10-4.
 */
public class AppRecordListVIew extends RecyclerView {

    private Context context;
    private AppRecordAdapter adapter;

    public AppRecordListVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void prepare(){

    }

    public void setAdapter(AppRecordAdapter adapter){
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    private void init(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        this.setLayoutManager(layoutManager);
        this.setHasFixedSize(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (isTouchHeader(ev)) {
//            return false;
//        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && adapter.isTouchHeader(ev)){
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
