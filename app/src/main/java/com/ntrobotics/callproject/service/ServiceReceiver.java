package com.ntrobotics.callproject.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.ntrobotics.callproject.DialerActivity;
import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.util.Timer;
import java.util.TimerTask;


public class ServiceReceiver extends BroadcastReceiver {

    private Context context;
    private ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();
    private SharedPreferences auto;

    @Override
    public void onReceive(Context context, Intent intent) {
        auto = context.getSharedPreferences("autoLogin", context.MODE_PRIVATE);
        this.context = context;

        final Bundle extras = intent.getExtras();
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            final String state = extras.getString(TelephonyManager.EXTRA_STATE);
            if ("IDLE".equals(state)){
                MyLogSupport.log_print("전화끊음!");
                autoCallViewModel.timer_Setting.setValue(false);
            }

            if ("OFFHOOK".equals(state)){
                MyLogSupport.log_print("전화중!");
                autoCallViewModel.timer_Setting.setValue(true);

            }
            if ("RINGING".equals(state)){
                MyLogSupport.log_print("전화울림!");

            }
        }

    }



}
