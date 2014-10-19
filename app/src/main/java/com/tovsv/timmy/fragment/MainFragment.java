package com.tovsv.timmy.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
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
import com.tovsv.timmy.view.ChartView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static android.view.ViewGroup.*;

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
    }

    @InjectView(R.id.chart)
    PieChart pieChart;

    @InjectView(R.id.chat_view)
    ChartView chartView;

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
        pieChart.setHoleRadius(60f);

        pieChart.setDescription("");

        pieChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true);

        pieChart.setDrawHoleEnabled(true);

        pieChart.setRotationAngle(0);

        // draws the corresponding description value into the slice
        pieChart.setDrawXValues(true);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);

        // display percentage values
        pieChart.setUsePercentValues(true);
        // mChart.setUnit(" €");
        pieChart.setDrawUnitsInChart(true);

        timeList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
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
                return list;
            }
            @Override
            protected void onPostExecute(AppInfoList list) {
                setData(list);
                pieChart.animateY(1500);
                chartView.animateY(1500);
                // startAnimators();
//                AppRecordListAdapter adapter = new AppRecordListAdapter(context, list);

//                timeList.setAdapter(adapter);
            }
        }.execute();

    }

    private void load() {
//        pieChart
        startAnimators();
    }

    private void prepare(){
        ViewGroup.LayoutParams  lp = pieChart.getLayoutParams();
        lp.height = 1000;
        animatorProgress = 1000;
        pieChart.setLayoutParams(lp);
    }

    private void setData(AppInfoList list){

        ArrayList<Entry> durations = new ArrayList<Entry>();
        ArrayList<String> names = new ArrayList<String>();
        long totle = 0;
        int size = list.size() > 4 ? 4 : list.size();

        for (int i = 0; i < size; i++){
            AppInfo info = list.get(i);
            durations.add(new Entry((float) info.duration, i));
            names.add(info.appName);
            totle = info.duration + totle;
        }

        durations.add(new Entry((float) (list.getTotleTime() - totle), size));
        names.add(getResources().getString(R.string.pie_chat_other));

        PieDataSet set1 = new PieDataSet(durations, "app duration");
        set1.setSliceSpace(3f);
        set1.setColors(ColorTemplate.VORDIPLOM_COLORS);

        PieData data = new PieData(names, set1);
        pieChart.setData(data);
        chartView.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        // set a text for the chart center
        pieChart.setCenterText("Total Value\n" + (int) pieChart.getYValueSum() + "\n(all slices)");
        pieChart.setCenterText("app test");
        pieChart.invalidate();
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
        //pieChart.setY(pieChart.getY() + animatorProgress * 10);
        ViewGroup.LayoutParams  lp = pieChart.getLayoutParams();
        lp.height = animatorProgress;
        pieChart.setLayoutParams(lp);

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
