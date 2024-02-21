package com.ntrobotics.callproject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.ntrobotics.callproject.di.HandlerBase;
import com.ntrobotics.callproject.support.MyLogSupport;

public class NtApplication extends Application {
    public static HandlerBase handlerViewModel;
    Context context;
    SharedPreferences auto;
    public static String serverip;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        serverip = "http://"+auto.getString("serverip", "")+":8043";
        MyLogSupport.log_print("Ntapplication serverip -> "+serverip);


        if(handlerViewModel == null){
            handlerViewModel = new HandlerBase(this.getApplicationContext());
        }
    }

    public static void set_serverip(String ip){
        MyLogSupport.log_print(ip);
        serverip = "http://"+ ip +":8043";
    }
}
