package com.tovsv.timmy.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.tovsv.timmy.R;
import com.tovsv.timmy.adapter.AppRecordAdapter;
import com.tovsv.timmy.model.event.CategoryEvent;
import com.tovsv.timmy.model.event.DataChangeEvent;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AppRecordHelper;
import com.tovsv.timmy.util.DLog;
import com.tovsv.timmy.view.AppRecordListVIew;
import com.tovsv.timmy.view.PieView;


import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainFragment extends Fragment {

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
    private Calendar ca = null;
    private int mode = -1;

    public MainFragment(){
        super();
        ca = Calendar.getInstance();
        this.mode = AppRecordHelper.MODE_DAY;
    }

    public MainFragment(Calendar ca,int mode){
        super();
        this.ca = ca;
        this.mode = mode;
    }

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
        showTime(ca,mode);
    }

    private void init() {
        timeList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    public void showTime(final Calendar ca, final int mode){

        new AsyncTask<Void, Void, AppInfoList>() {
            @Override
            protected void onPreExecute(){
//                prepare();
            }
            @Override
            protected AppInfoList doInBackground(Void... voids) {
                AppRecordHelper helper = new AppRecordHelper(context);
                AppInfoList list = helper.loadAppsByDate(ca,mode);
//                chartView.setData(list);
                return list;
            }
            @Override
            protected void onPostExecute(AppInfoList list) {
//                chartView.invalidate();


                AppRecordAdapter adapter = new AppRecordAdapter(context, list);
                timeList.setAdapter(adapter);

                chartView.setData(list);
                chartView.animateY(1500);
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
