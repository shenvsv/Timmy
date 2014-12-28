package com.tovsv.timmy.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tovsv.timmy.R;
import com.tovsv.timmy.adapter.AppRecordListAdapter;
import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.model.event.CategoryEvent;
import com.tovsv.timmy.model.event.DataChangeEvent;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AppRecordHelper;
import com.tovsv.timmy.util.DLog;
import com.tovsv.timmy.view.AppRecordListVIew;
import com.tovsv.timmy.view.PieView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainFragment extends Fragment {

    //test
    @OnClick(R.id.button_refresh)
    public void btn_refresh(){
        showTime(Calendar.getInstance());
    }

    @OnClick(R.id.button_yesterday)
    public void btn_yesterday(){
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_YEAR, -1);
        showTime(ca);
//        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//        startActivity(intent);
    }

    @InjectView(R.id.chat_view)
    PieView chartView;

    @InjectView(R.id.listView)
    AppRecordListVIew timeList;

    @InjectView(R.id.chart_layout)
    LinearLayout chartLayout;


    private EventBus mEventBus;
    private Context context;
    private Animator animator;
    private int animatorProgress = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAllAnimators();
        mEventBus.unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        context = this.getActivity();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //startAnimators();
        init();
        showTime(Calendar.getInstance());
    }

    private void init() {
        timeList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

//        timeList.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                //DLog.i("//"+scrollState);
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                DLog.i(".."+ timeList.getPY());
//            }
//        });
    }

    public void showTime(final Calendar ca){

        new AsyncTask<Void, Void, AppInfoList>() {
            @Override
            protected void onPreExecute(){
//                prepare();
            }
            @Override
            protected AppInfoList doInBackground(Void... voids) {
                AppRecordHelper helper = new AppRecordHelper(context);
                AppInfoList list = helper.loadAppsByDate(ca);
                chartView.setData(list);
                return list;
            }
            @Override
            protected void onPostExecute(AppInfoList list) {

                chartView.invalidate();

                AppRecordListAdapter adapter = new AppRecordListAdapter(context, list);
                timeList.setAdapter(adapter);

//                chartView.animateY(1500);
                // startAnimators();

            }
        }.execute();

    }

    private void cancelAllAnimators() {
        if (animator != null) {
            animator .cancel();
            animator  = null;
        }
    }

    public void setAnimatorProgress(int animatorProgress){
        this.animatorProgress = animatorProgress;
        DLog.i("//"+animatorProgress);


    }

    public int getAnimatorProgress(){
        return animatorProgress;
    }

    private void startAnimators() {
        animator = ObjectAnimator.ofInt(MainFragment.this, "animatorProgress", animatorProgress, 1100);
        animator.setDuration(3000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    public void fresh(){

    }

    public void onEvent(CategoryEvent event) {

    }

    public void onEvent(DataChangeEvent event) {

    }

}
