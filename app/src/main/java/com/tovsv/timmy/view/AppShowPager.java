package com.tovsv.timmy.view;

import android.content.Context;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tovsv.timmy.fragment.MainFragment;
import com.tovsv.timmy.util.AppRecordHelper;

import java.util.Calendar;

/**
 * Created by shenvsv on 15/1/2.
 */
public class AppShowPager extends ViewPager{
    private FragmentPagerAdapter adapter;
    private OnDragListener dragListener;

    public AppShowPager(Context context) {
        super(context);
    }

    public AppShowPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public void init(FragmentManager manager) {

        adapter = new AppShowAdapter(manager);
        this.setOffscreenPageLimit(5);
        this.setAdapter(adapter);
        this.setCurrentItem(3, false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        return super.onTouchEvent(ev);
    }

    public interface OnDragListener {

//        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
//
//        public void onPageSelected(int position);
//
//        public void onPageScrollStateChanged(int state);

    }


    class AppShowAdapter extends FragmentPagerAdapter {

        private int mCount = 5;

        public AppShowAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
           switch (position){
               case 0:
                   return new MainFragment(Calendar.getInstance(), AppRecordHelper.MODE_MONTH);
               case 1:
                   return new MainFragment(Calendar.getInstance(), AppRecordHelper.MODE_WEEK);
               case 2:
                   Calendar ca = Calendar.getInstance();
                   ca.add(Calendar.DAY_OF_YEAR,-1);
                   return new MainFragment(ca,AppRecordHelper.MODE_DAY);
               case 4:
                   return new MainFragment(Calendar.getInstance(),AppRecordHelper.MODE_ALL);
               default:
                   return new MainFragment();

           }
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }


}


