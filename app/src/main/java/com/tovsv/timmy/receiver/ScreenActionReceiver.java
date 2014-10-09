package com.tovsv.timmy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tovsv.timmy.service.AppListenerService;

public class ScreenActionReceiver extends BroadcastReceiver {
    public ScreenActionReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_SCREEN_ON){
            Intent serviceIntent = new Intent(context, AppListenerService.class);
            serviceIntent.putExtra(AppListenerService.KEY_COMMAND, AppListenerService.COMMAND_START_TRICK);
            context.startService(serviceIntent);
        }

        if (intent.getAction() == Intent.ACTION_SCREEN_OFF){
            Intent serviceIntent = new Intent(context, AppListenerService.class);
            serviceIntent.putExtra(AppListenerService.KEY_COMMAND, AppListenerService.COMMAND_STOP_TRICK);
            context.startService(serviceIntent);
        }

    }
}
