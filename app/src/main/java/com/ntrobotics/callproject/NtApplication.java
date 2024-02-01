package com.ntrobotics.callproject;

import android.app.Application;

import com.ntrobotics.callproject.di.HandlerBase;

public class NtApplication extends Application {
    public static HandlerBase handlerViewModel;

    @Override
    public void onCreate() {
        super.onCreate();

        if(handlerViewModel == null){
            handlerViewModel = new HandlerBase(this.getApplicationContext());
        }
    }
}
