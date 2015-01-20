package com.tovsv.timmy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tovsv.timmy.R;
import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AsyncIconLoader;
import com.tovsv.timmy.util.DLog;

import java.util.List;

/**
 * Created by shenvsv on 14/12/28.
 */

public class AppRecordAdapter extends RecyclerView.Adapter<AppRecordAdapter.ViewHolder> {
    private List<AppInfo> mAppInfos;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private AsyncIconLoader mAsyncIconLoader;
    private long mMax;
    private static final int HEADERS_START = Integer.MIN_VALUE;
    private View mHeader;
    private int mHeaderHeight;

    public AppRecordAdapter(Context context, AppInfoList appInfos) {
        mContext = context;
        mAppInfos = appInfos;
        mLayoutInflater = LayoutInflater.from(context);
        mAsyncIconLoader = new AsyncIconLoader(context);
        mMax = appInfos.getTotleTime();
        mHeaderHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.chart_view_height);
    }

    @Override
    public AppRecordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1){
            mHeader = new View(mContext);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight);
            mHeader.setLayoutParams(lp);
            return new ViewHolder(mHeader);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_record, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.name = (TextView) view.findViewById(R.id.tv_name);
            holder.duration = (TextView) view.findViewById(R.id.tv_duration);
            holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
            holder.progressbar = (ProgressBar) view.findViewById(R.id.pb_time);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position != 0){
            final AppInfo appInfo = mAppInfos.get(position);
            holder.name.setText(appInfo.appName);
            holder.duration.setText(appInfo.time);
            holder.progressbar.setProgress((int) (appInfo.duration * 100.0 / mMax));
            mAsyncIconLoader.loadBitmap(appInfo.packageName, holder.icon);
        }

    }

    @Override
    public int getItemViewType(int position) {
       if (position == 0){
           return -1;
       }else {
           return 0;
       }

    }

    @Override
    public int getItemCount() {
        return mAppInfos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView duration;
        public TextView name;
        public ImageView icon;
        public ProgressBar progressbar;
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View view) {
            DLog.i("onClick " + getPosition());
        }

    }

    public Boolean isTouchHeader(MotionEvent event) {
        int y = (int) event.getY();
        return y < mHeaderHeight + mHeader.getY();
    }
}
