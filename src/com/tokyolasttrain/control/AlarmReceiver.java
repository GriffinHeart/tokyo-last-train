package com.tokyolasttrain.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tokyolasttrain.view.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, AlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}