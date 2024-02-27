package com.ntrobotics.callproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;

@SuppressLint("InlinedApi")
public class YourPermissionClass {

    public static String[] requiredPermissionList;

    static {
        requiredPermissionList = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                new String[]{
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_NUMBERS,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.ANSWER_PHONE_CALLS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                } :
                new String[]{
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                };
    }

    // 나머지 코드...
}