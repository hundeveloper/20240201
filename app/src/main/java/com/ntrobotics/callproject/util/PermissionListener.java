package com.ntrobotics.callproject.util;

import android.util.Log;

import java.util.List;

public class PermissionListener implements com.gun0912.tedpermission.PermissionListener {
    @Override
    public void onPermissionGranted() {
        Log.e("myapp", "#####################권한승인#########################");
    }

    @Override
    public void onPermissionDenied(List<String> deniedPermissions) {
        Log.e("myapp", "#####################권한거절#########################");
        System.exit(0);
    }
}
