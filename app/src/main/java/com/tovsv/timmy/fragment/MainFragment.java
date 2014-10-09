package com.tovsv.timmy.fragment;

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
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tovsv.timmy.R;
import com.tovsv.timmy.adapter.AppRecordListAdapter;
import com.tovsv.timmy.model.event.CategoryEvent;
import com.tovsv.timmy.model.event.DataChangeEvent;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AppRecordHelper;
import com.tovsv.timmy.util.DLog;
import com.tovsv.timmy.view.AppRecordListVIew;

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
    }

    @InjectView(R.id.test_view)
    ImageView testView;

    @InjectView(R.id.listView)
    AppRecordListVIew timeList;

    private EventBus mEventBus;
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        showTime(Calendar.getInstance());
    }

    public void showTime(final Calendar ca){

        new AsyncTask<Void, Void, AppInfoList>() {
            @Override
            protected void onPreExecute(){
                prepare();
            }
            @Override
            protected AppInfoList doInBackground(Void... voids) {
                AppRecordHelper helper = new AppRecordHelper(context);
                AppInfoList list = helper.loadAppsByDate(ca);
                return list;
            }
            @Override
            protected void onPostExecute(AppInfoList list) {
                load();
                AppRecordListAdapter adapter = new AppRecordListAdapter(context, list);
                // timeList.setAdapter(adapter);
            }
        }.execute();

    }

    private void prepare(){

    }

    private void load() {
//        testView.
    }

    public void fresh(){

    }

    public void onEvent(CategoryEvent event) {

    }

    public void onEvent(DataChangeEvent event) {

    }

}
