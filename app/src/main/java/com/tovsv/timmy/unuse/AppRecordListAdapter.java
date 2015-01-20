package com.tovsv.timmy.unuse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tovsv.timmy.R;
import com.tovsv.timmy.app.Constants;
import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.test.DetailsActivity;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AsyncIconLoader;

import java.util.List;

/**
 * Created by shenvsv on 14-9-24.
 */
public class AppRecordListAdapter extends BaseAdapter {

    private List<AppInfo> mAppInfos;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private AsyncIconLoader mAsyncIconLoader;
    private long mMax;

    public AppRecordListAdapter(Context context, AppInfoList appInfos) {
        mContext = context;
        mAppInfos = appInfos;
        mLayoutInflater = LayoutInflater.from(context);
        mAsyncIconLoader = new AsyncIconLoader(context);
        mMax = appInfos.getTotleTime();
    }

    public void flush(AppInfoList appInfos) {
        mAppInfos.clear();
        mAppInfos.addAll(appInfos);
        mMax = appInfos.size() > 0 ? mAppInfos.get(0).duration : 0;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAppInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return mAppInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.item_app_record, viewGroup, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.tv_name);
            holder.duration = (TextView) view.findViewById(R.id.tv_duration);
            holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
            holder.progressbar = (ProgressBar) view.findViewById(R.id.pb_time);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final AppInfo appInfo = mAppInfos.get(i);
        holder.name.setText(appInfo.appName);
        holder.duration.setText(appInfo.time);
        holder.progressbar.setProgress((int) (appInfo.duration * 100.0 / mMax));
        mAsyncIconLoader.loadBitmap(appInfo.packageName, holder.icon);
//        mAsyncIconLoader.loadIcon(appInfo.packageName, holder.icon);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, DetailsActivity.class);
                i.putExtra("package_name", appInfo.packageName);
                mContext.startActivity(i);
            }
        });
        return view;
    }

    private static class ViewHolder {
        public TextView duration;
        public TextView name;
        public ImageView icon;
        public ProgressBar progressbar;
    }

    private void getColorByDuration(long duration) {

    }
}
