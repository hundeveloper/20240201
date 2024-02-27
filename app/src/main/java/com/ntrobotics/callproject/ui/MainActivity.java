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
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.ConnectionService;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.gun0912.tedpermission.normal.TedPermission;
import com.ntrobotics.callproject.CallStateListener;
import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.R;
import com.ntrobotics.callproject.YourPermissionClass;
import com.ntrobotics.callproject.databinding.ActivityMainBinding;
import com.ntrobotics.callproject.network.ServerAddress;
import com.ntrobotics.callproject.service.Alarm;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;
import com.ntrobotics.callproject.util.PermissionListener;
import com.ntrobotics.callproject.util.ProgressDialog;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private TelephonyManager telephonyManager;
    private ReadViewModel readViewModel;
    private Intent it;
    private MyToastSupport myToastSupport = new MyToastSupport(this);
    private SharedPreferences auto;
    private ProgressDialog progressDialog;
    private AlarmManager alarmManager;
    private TimePicker timePicker;
    private Long mLastClickTime = 0L;
    private AlertDialog alertDialog;
    private SharedPreferences.Editor autoLoginEdit;


    @Override
    protected void createActivity() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission_check();
        Go_Settings();

//        regist();
        record_file_delete();

        Button showPopupButton = findViewById(R.id.showPopupButton);
        showPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });
    }

    @Override
    protected void resumeActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "(autocall)다른 앱 위에 표시 -> 허용을 해주세요.", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상 버전인지 확인
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "(autocall)모든 파일에 대한 접근을 허용해주세요..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void Go_Settings() {
        // TODO : 로그인설정
        auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        autoLoginEdit = auto.edit();
        MyLogSupport.log_print("go_settings");

        String serverip = auto.getString("serverip", "");
        try {
            if (!serverip.isEmpty()) {
                readViewModel = NtApplication.handlerViewModel.getReadViewModel();
                readViewModel.updateRetrofit();
                binding.tvServerIp.setText(serverip);
//                ServerAddress.BASE_URL = "http://" + serverip + ":8043/";


                // TODO : REST API 관찰호선
                readViewModel.loginsuccess.observe(this, new Observer<Boolean>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
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
                        if (aBoolean == true) {
                            myToastSupport.showToast("현재 서버가 off상태입니다.");
                        }
                    }
                });
            }
        }catch (Exception e){
            MyLogSupport.log_print(e.toString());
        }

        String checkbox = auto.getString("checkbox", "");
        String companyId = auto.getString("CompanyId", "");
        String HpNum = auto.getString("HpNum", "");

        if (checkbox.equals("y")) {
            readViewModel.login(companyId, HpNum);
        }

        // TODO : 기능 동작 초기 설정
        it = new Intent(this, AfterActivity.class);
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        progressDialog = new ProgressDialog(this);
        binding.loginButton.setOnClickListener(this);
    }



    @SuppressLint("HardwareIds")
    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
            if (v.getId() == R.id.login_button) {
                MyLogSupport.log_print("로그인버튼클릭!");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    permission_check();
                    return;
                }
                if (telephonyManager.getLine1Number() == null) {
                    myToastSupport.showToast("유심을 장착해 주세요.");
                    return;
                } else if (auto.getString("serverip", "").isEmpty()) {
                    myToastSupport.showToast("서버 IP주소를 입력해주세요.");
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





    private void showPopup() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText userInput = promptView.findViewById(R.id.editTextDialogUserInput);
        Button confirmButton = promptView.findViewById(R.id.confirmButton);
        Button cancelButton = promptView.findViewById(R.id.cancelButton);


        userInput.setText(auto.getString("serverip", ""));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = userInput.getText().toString();

                // 확인 버튼을 눌렀을 때 실행될 메소드를 호출
                onConfirmButtonClicked(inputText);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 취소 버튼을 눌렀을 때 실행될 메소드를 호출
                onCancelButtonClicked();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void onConfirmButtonClicked(String userInputText) {
        final String IP_REGEX =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        Pattern pattern = Pattern.compile(IP_REGEX);
        Matcher matcher = pattern.matcher(userInputText);


        if (userInputText.isEmpty() || !matcher.matches()) {
            myToastSupport.showToast("유효하지않은 IP주소입니다.");
        } else {
            autoLoginEdit.putString("serverip", userInputText);
            autoLoginEdit.commit();

            myToastSupport.showToast("IP주소 변경으로 앱을 재시작합니다.");
            alertDialog.dismiss();

            NtApplication.set_serverip(userInputText);

            Intent it = new Intent(this, MainActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);

        }

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
        progressDialog.closeDialog();
    }

    private void onCancelButtonClicked() {
        // 이 부분에 취소 버튼을 눌렀을 때 실행되어야 하는 코드를 작성합니다.
        // 예를 들어, 다이얼로그를 닫을 수 있습니다.
        try {
            alertDialog.dismiss();

        } catch (Exception e) {
            MyLogSupport.log_print(e.toString());
        }
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
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        MyLogSupport.log_print("Alarm On : " + calendar.getTime());
        MyLogSupport.log_print("Alarm On : " + calendar.getTimeInMillis());

        // 지정한 시간에 매일 알림
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pIntent), pIntent);
    }

    private void record_file_delete() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7); //2024-02-06 11:26
        Date date = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더해줌
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        MyLogSupport.log_print("현재 날짜: " + year + "-" + month + "-" + day);


        if (Build.MANUFACTURER.contains("LG")) {
            MyLogSupport.log_print("파일정리 START");
            String filePath = "/storage/emulated/0/VoiceRecorder/my_sounds/call_rec/";
            File directory = new File(filePath);
            File[] files = directory.listFiles();

            for (File file1 : files) {
                Date file_currentDates = new Date(file1.lastModified());
                if (file_currentDates.before(date)) {
                    if (file1.delete()) {
                        MyLogSupport.log_print(file1.getName() + " 파일삭제완료");
                    } else {
                        MyLogSupport.log_print(file1.getName() + " 파일삭제안됐음");
                    }
                }
            }
        } else if (Build.MANUFACTURER.contains("samsung")) {
            MyLogSupport.log_print("파일정리 START");
            String filePath = "/storage/emulated/0/Recordings/Call/";
            File directory = new File(filePath);
            File[] files = directory.listFiles();

            for (File file1 : files) {
                Date file_currentDates = new Date(file1.lastModified());
                if (file_currentDates.before(date)) {
                    MyLogSupport.log_print("현재 이 파일은 " + file_currentDates + " 현재 날짜보다 이전날짜입니다.");
                    if (file1.delete()) {
                        MyLogSupport.log_print(file1.getName() + " 파일삭제완료");
                    } else {
                        MyLogSupport.log_print(file1.getName() + " 파일삭제안됐음");
                    }
                }
            }
        }

    }


    private void permission_check() {
        String[] PERMISSIONS = YourPermissionClass.requiredPermissionList;
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
                    .setPermissions(Manifest.permission.READ_PHONE_STATE)
                    .setPermissions(Manifest.permission.READ_MEDIA_AUDIO)
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();

        } else { // TODO : API 30 이하
            MyLogSupport.e_log_print("API30이하");
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("App 실행을 위해서 필요한 권한을 요청합니다.")
                    .setDeniedMessage("권한을 설정하지않을시 앱이 종료됩니다.")
                    .setPermissions(Manifest.permission.CALL_PHONE)
                    .setPermissions(Manifest.permission.READ_PHONE_STATE)
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
        }

    }
}
