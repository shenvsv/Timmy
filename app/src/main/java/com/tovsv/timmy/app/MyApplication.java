package com.tovsv.timmy.app;

import android.app.Application;


import com.tovsv.timmy.model.AppAction;
import com.tovsv.timmy.model.AppRecord;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;

/**
 * Created by kleist on 14-5-24.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initDB();
    }

    private void initDB() {
        Sprinkles sprinkles = Sprinkles.init(getApplicationContext());
        Migration migration = new Migration();
//        migration.createTable(AppRecord.class);
        migration.createTable(AppAction.class);
//        migration.createTable(Category.class);
//        migration.createTable(AppsCategory.class);
//        migration.createTable(DailyRecord.class);
        sprinkles.addMigration(migration);
    }
}
