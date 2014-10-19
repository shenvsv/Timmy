package com.tovsv.timmy.activity;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tovsv.timmy.adapter.AppRecordListAdapter;
import com.tovsv.timmy.fragment.MainFragment;
import com.tovsv.timmy.fragment.NavigationDrawerFragment;
import com.tovsv.timmy.R;
import com.tovsv.timmy.model.event.CategoryEvent;
import com.tovsv.timmy.service.AppListenerService;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.AppRecordHelper;
import com.tovsv.timmy.view.AppRecordListVIew;
import com.umeng.update.UmengUpdateAgent;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private MainFragment mainFragment;
    private EventBus mEventBus;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        //UmengUpdateAgent.update(this);
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up applistener
        startListenerServices();

        // Set up the drawer.
        initDrawer();

        //mainFragment.showTime(Calendar.getInstance());
    }

    @Override
    protected void onResume(){
        super.onResume();
        // showTime(Calendar.getInstance());
    }

    private void initDrawer(){
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        mainFragment = new MainFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainFragment)
                .commit();
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
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Toast.makeText(this,"po"+position, Toast.LENGTH_LONG).show();
    }

}
