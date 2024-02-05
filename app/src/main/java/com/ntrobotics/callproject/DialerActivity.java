package com.ntrobotics.callproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.gun0912.tedpermission.normal.TedPermission;
import com.ntrobotics.callproject.databinding.ActivityDialerBinding;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.ui.BaseActivity;
import com.ntrobotics.callproject.util.PermissionListener;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.util.Timer;
import java.util.TimerTask;

public class DialerActivity extends BaseActivity {

    Button callBtn;
    EditText phoneNumber;
    ActivityDialerBinding binding;
    private final ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();
    private Timer timer_calling;
    private SharedPreferences auto;

    private void permission_check() {
        String[] PERMISSIONS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PERMISSIONS = new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
            };
        }else{
            PERMISSIONS = new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE
            };
        }

        PermissionListener permissionListener = new PermissionListener();


        ActivityCompat.requestPermissions(this, PERMISSIONS, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // TODO : API 30 이상
            MyLogSupport.e_log_print("API30이상");
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("앱 실행을 위해 필요한 권한을 요청합니다.")
                    .setDeniedMessage("권한을 설정하지않을시 앱이 종료됩니다.")
                    .setPermissions(Manifest.permission.CALL_PHONE)
                    .setPermissions(Manifest.permission.READ_PHONE_NUMBERS)
                    .check();

        } else { // TODO : API 30 이하
            MyLogSupport.e_log_print("API30이하");
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("App 실행을 위해서 필요한 권한을 요청합니다.")
                    .setDeniedMessage("권한을 설정하지않을시 앱이 종료됩니다.")
                    .setPermissions(Manifest.permission.CALL_PHONE)
                    .setPermissions(Manifest.permission.READ_PHONE_STATE)
                    .check();
        }

    }
    @Override
    @SuppressLint({"MissingPermission", "HardwareIds"})
    protected void createActivity() {
        binding = ActivityDialerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        callBtn = binding.callBtn;
        phoneNumber = binding.inputNumberET;
        Toast toast = Toast.makeText(this, "유심을 장착해 주세요.", Toast.LENGTH_SHORT);

        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor autoLoginEdit = auto.edit();

        permission_check();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager.getLine1Number() == null){
            return;
        }
        autoLoginEdit.putString("HpNum", telephonyManager.getLine1Number());
        autoLoginEdit.commit();



        binding.test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //전화걸었을때
                autoCallViewModel.call_status_start(telephonyManager.getLine1Number(), "01056587246");
            }
        });

        binding.test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //전화상태체크
                autoCallViewModel.calling_check(telephonyManager.getLine1Number());
            }
        });


        binding.test.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HardwareIds")
            @Override
            public void onClick(View view) {
                if(telephonyManager.getLine1Number() == null){
                    toast.show();
                    return;
                }else{

                    //전화끊었을때 신호
//                    autoCallViewModel.call_status_start(telephonyManager.getLine1Number(), "01056587246");
                    MyLogSupport.log_print("onclick 클릭");
//                    autoCallViewModel.calling_check(telephonyManager.getLine1Number());
                    autoCallViewModel.calling_end(telephonyManager.getLine1Number());
                }

            }
        });


        autoCallViewModel.timer_Setting.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    MyLogSupport.log_print("통화가시작되었으니 타이머를 시작하죠");
                    startTimer_calling();

                }else{
                    MyLogSupport.log_print("통화가종료 타이머를 중단!");
                    stopTimer_calling();
                }
            }
        });


        callBtn.setOnClickListener(v -> {
            String inputNumber = phoneNumber.getText().toString();

            if (!inputNumber.isEmpty()) {
                @SuppressLint("ServiceCast") TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                Uri uri = Uri.fromParts("tel", inputNumber, null);
                Bundle extras = new Bundle();
                extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                    if (telecomManager.getDefaultDialerPackage().equals(getPackageName())) {
                        telecomManager.placeCall(uri, extras);
                    } else {
                        Uri phoneNumber = Uri.parse("tel:" + inputNumber);
                        Intent callIntent = new Intent(Intent.ACTION_CALL, phoneNumber);
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                    }
                } else {
                    Toast.makeText(this, "Please allow permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void resumeActivity() {

    }

    @Override
    protected void startActivity() {

    }

    @Override
    protected void pauseActivity() {

    }

    @Override
    protected void onRestartActivity() {

    }

    @Override
    protected void destroyActivity() {

    }


    @Override
    public void onClick(View view) {

    }

    private void startTimer_calling() {
        if (this.timer_calling == null) {
            MyLogSupport.log_print("타이머가 실행되었습니다");
            this.timer_calling = new Timer();

            this.timer_calling.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    autoCallViewModel.calling_check(auto.getString("HpNum", ""));
                    if(autoCallViewModel.call_quit == true){
                        endCall();
                    }
                }
            }, 0, 2000);
        }
    }
    private void stopTimer_calling() {
        autoCallViewModel.call_quit = false;
        if (this.timer_calling != null) {
            this.timer_calling.cancel();
            this.timer_calling = null;
        }
    }

    private void endCall() {
        try {
//             통화 종료 시도
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> c = Class.forName(tm.getClass().getName());
            java.lang.reflect.Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm);
            c = Class.forName(telephonyService.getClass().getName());
            m = c.getDeclaredMethod("endCall");
            m.setAccessible(true);
            m.invoke(telephonyService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
