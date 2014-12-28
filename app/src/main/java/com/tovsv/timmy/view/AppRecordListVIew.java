package com.tovsv.timmy.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tovsv.timmy.R;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.DLog;

/**
 * Created by shenvsv on 14-10-4.
 */
public class AppRecordListVIew extends ListView implements AbsListView.OnScrollListener {

    private View mHeader;
    private int mHeaderHeight;
    private int mHeaderWidth;
    private Context context;

    public AppRecordListVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setDividerHeight(0);
        init();
    }

    public void prepare(){

    }

    public void setAdapter(ListAdapter adapter){
        super.setAdapter(adapter);
    }

    private void addHeader() {
        mHeader = new View(getContext());
        ListView.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight);
        mHeader.setLayoutParams(lp);
        this.addHeaderView(mHeader);
    }

    private void init(){
        mHeaderHeight = getResources().getDimensionPixelOffset(R.dimen.chart_view_height);
        addHeader();
        super.setOnScrollListener(this);
    }

    public float getPY(){
        return mHeader.getY();
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
        if (ev.getAction() == MotionEvent.ACTION_DOWN && isTouchHeader(ev)){
            return false;
        }
        return super.onTouchEvent(ev);
    }


    private Boolean isTouchHeader(MotionEvent event) {
        int y = (int) event.getY();
        return y < mHeaderHeight + mHeader.getY();
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
       // DLog.i("change"+scrollState);
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
       // DLog.i("x"+firstVisibleItem+"x"+visibleItemCount+"x"+totalItemCount);

    }
}
