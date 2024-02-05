package com.ntrobotics.callproject.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.ConnectionService;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.gun0912.tedpermission.normal.TedPermission;
import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.R;
import com.ntrobotics.callproject.databinding.ActivityMainBinding;
import com.ntrobotics.callproject.service.Alarm;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;
import com.ntrobotics.callproject.util.PermissionListener;
import com.ntrobotics.callproject.util.ProgressDialog;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.lang.reflect.Method;
import java.util.Calendar;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private TelephonyManager telephonyManager;
    private ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private Intent it;
    private MyToastSupport myToastSupport = new MyToastSupport(this);
    private SharedPreferences auto;
    private ProgressDialog progressDialog;
    private AlarmManager alarmManager;
    private TimePicker timePicker;
    private Long mLastClickTime = 0L;


    @Override
    protected void createActivity() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.companyId.setText("SAMSUNG");

        permission_check();
        Go_Settings();

    }

    private void permission_check() {
        String[] PERMISSIONS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PERMISSIONS = new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.ANSWER_PHONE_CALLS
            };
        }else{
            PERMISSIONS = new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE
            };
        }

        PermissionListener permissionListener = new PermissionListener();


        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 0);

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

    @SuppressLint("MissingPermission")
    private void Go_Settings() {
        // TODO : 로그인설정
        auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        String checkbox = auto.getString("checkbox", "");
        String companyId = auto.getString("CompanyId", "");
        String HpNum = auto.getString("HpNum", "");

        if (checkbox.equals("y")) {
            readViewModel.login(companyId, HpNum);
        }

        // TODO : 기능 동작 초기 설정
        it = new Intent(this, AfterActivity.class);
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        MyLogSupport.log_print("TelephonyManager 전화번호 -> + "+telephonyManager.getLine1Number());

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        progressDialog = new ProgressDialog(this);
        binding.loginButton.setOnClickListener(this);


        // TODO : REST API 관찰호선
        readViewModel.loginsuccess.observe(this, new Observer<Boolean>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onChanged(Boolean aBoolean) {
                MyLogSupport.log_print("onchange 작동중!");
                if (aBoolean) {
                    SharedPreferences.Editor autoLoginEdit = auto.edit();

                    autoLoginEdit.putString("CompanyId", binding.companyId.getText().toString());
                    autoLoginEdit.putString("HpNum", telephonyManager.getLine1Number());

                    // TODO : 자동로그인 체크 여부
                    if (binding.checkbox.isChecked()) {
                        autoLoginEdit.putString("checkbox", "y");
                        MyLogSupport.log_print("자동로그인활성화");

                    } else {
                        autoLoginEdit.putString("checkbox", "n");
                        MyLogSupport.log_print("자동로그인비활성화");
                    }
                    autoLoginEdit.commit();

                    // TODO : 화면 넘기기
                    startActivity(it);
                    progressDialog.closeDialog();
                    finish();
                } else {
                    myToastSupport.showToast("로그인에 실패하였습니다. ");
                    progressDialog.closeDialog();
                    // 자동로그인 초기화
                }

            }
        });

        readViewModel.server_online.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == true){
                    myToastSupport.showToast("현재 서버가 off상태입니다.");
                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    @Override
    public void onClick(View v) {
        if(SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
            if (v.getId() == R.id.login_button) {
                MyLogSupport.log_print("로그인버튼클릭!");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (telephonyManager.getLine1Number() == null) {
                    myToastSupport.showToast("유심을 장착해 주세요.");
                    return;
                }


                readViewModel.login(binding.companyId.getText().toString(), telephonyManager.getLine1Number());
                myToastSupport.showToast("로그인중입니다.");
                progressDialog.showDialog();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.closeDialog();
                    }
                }, 2000);
            }
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    @SuppressLint({"MissingPermission", "ScheduleExactAlarm"})
    public void regist() {
        // 앱 재부팅 실행!!
        // TODO Calendar 시간에 맞춰서넣으면됩니다. calendar.set(이부분)
        Intent intent = new Intent(this, Alarm.class);
        intent.putExtra("app", "restart");
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        MyLogSupport.log_print("regist실행");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        MyLogSupport.log_print("Alarm On : " + calendar.getTime());
        MyLogSupport.log_print("Alarm On : " + calendar.getTimeInMillis());

        // 지정한 시간에 매일 알림
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pIntent), pIntent);

    }

    @Override
    protected void resumeActivity () {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(this)){
                Toast.makeText(this,"(autocall)다른 앱 위에 표시 -> 허용을 해주세요.",Toast.LENGTH_SHORT).show();
                Intent it = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivity(it);
            }
        }
    }

    @Override
    protected void startActivity () {

    }

    @Override
    protected void pauseActivity () {

    }

    @Override
    protected void onRestartActivity () {

    }

    @Override
    protected void destroyActivity () {
        progressDialog.closeDialog();
    }


    }
