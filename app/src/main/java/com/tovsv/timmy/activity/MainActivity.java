package com.tovsv.timmy.activity;


import android.app.ActionBar;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tovsv.timmy.fragment.DrawerFragment;
import com.tovsv.timmy.fragment.MainFragment;
import com.tovsv.timmy.R;
import com.tovsv.timmy.model.event.CategoryEvent;
import com.tovsv.timmy.service.AppListenerService;
import com.tovsv.timmy.view.AppShowPager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity {

    private DrawerFragment mDrawerFragment;
    private MainFragment mainFragment;
    private ActionBarDrawerToggle mDrawerToggle;
    private EventBus mEventBus;

    private FragmentPagerAdapter adapter;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.toolbar)
    Toolbar toolBar;
    @InjectView(R.id.container)
    AppShowPager container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        toolBar.setBackgroundColor(Color.rgb(0,175,254));
        toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolBar);

        //UmengUpdateAgent.update(this);
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        // Set up applistener
        startListenerServices();

        // Set up the drawer.
        initDrawer();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // showTime(Calendar.getInstance());
    }

    private void initDrawer(){

        mDrawerFragment = new DrawerFragment();
        mainFragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
//                .replace(R.id.container, mainFragment)
                .replace(R.id.left_drawer, mDrawerFragment)
                .commit();

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //set up pager

        container.init(fragmentManager);

    }

    private void startListenerServices() {
        Intent intent = new Intent(this, AppListenerService.class);
        intent.putExtra(AppListenerService.KEY_COMMAND, AppListenerService.COMMAND_START_TRICK);
        startService(intent);
    }

    public void onEvent(CategoryEvent event) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
//https://www.google.com/search?client=safari&rls=en&q=fragmentstatepageradapter&ie=UTF-8&oe=UTF-8
