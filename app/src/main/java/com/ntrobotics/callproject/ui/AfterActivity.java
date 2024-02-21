package com.ntrobotics.callproject.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.ntrobotics.callproject.CallStateListener;
import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.R;
import com.ntrobotics.callproject.databinding.ActivityAfterBinding;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.service.Alarm;
import com.ntrobotics.callproject.service.ServiceReceiver;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AfterActivity extends BaseActivity {

    private ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private ActivityAfterBinding binding;
    private SharedPreferences auto;
    private Intent it;
    private MyToastSupport toastSupport = new MyToastSupport(this);
    private Timer timer, timer_calling, timer_location;
    Handler handler;
    private boolean isHandlerActive = true;
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();
    private AlarmManager alarmManager;

    private TelephonyManager telephonyManager;
    private boolean firstcall = false;

    @Override
    protected void createActivity() {
        binding = ActivityAfterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Init();
    }

    private void startTimer_callwating() {
        if (timer == null) {
            MyLogSupport.log_print("StartTimer_callwating");
            timer = new Timer();

            // TODO : Android 14 버전 이슈로 timer가 중단? or 스택처럼 쌓여서 한번에 밀리는 상황 발생

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    readViewModel.number_check(auto.getString("HpNum", ""));
                }
            }, 0, 12000);
        }
    }

    private void stopTimer_callwating() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startTimer_calling() {
        Context context = this.getApplicationContext();
        if (this.timer_calling == null) {
            MyLogSupport.log_print("startTimer_calling");
            this.timer_calling = new Timer();
            this.timer_calling.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    autoCallViewModel.calling_check(auto.getString("HpNum", ""));
                    if (autoCallViewModel.call_quit == true) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            hangUpCall(context);
                        } else {
                            endCall();
                        }
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

    private void startTimer_location(){
        if (this.timer_location == null) {
            MyLogSupport.log_print("startTimer_calling");
            this.timer_location = new Timer();
            this.timer_location.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    //마지막 위치 받아오기
                    @SuppressLint("MissingPermission")
                    Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    Double cur_lat = loc_Current.getLatitude(); //위도 y
                    Double cur_lon = loc_Current.getLongitude(); //경도 x
                    autoCallViewModel.locationUpload(auto.getString("HpNum", ""),  cur_lat, cur_lon);
                }
            }, 0, 1800000);
        }
    }

    private void stopTimer_location(){
        if(timer_location != null){
            timer_location.cancel();
            timer_location = null;
        }
    }

    private void endCall() {
        try {
            // 통화 종료 시도
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

    private void Init() {
        readViewModel.updateRetrofit();
        autoCallViewModel.updateRetrofit();

        startTimer_location();

        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        binding.logoutButton.setOnClickListener(this);
        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);

        it = new Intent(this, MainActivity.class);


        // 현재 이 전화번호가 auto_call을 돌려도되는건지 체크?
        toastSupport.showToast("곧 오토콜 프로그램이 동작됩니다..");

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isHandlerActive) {
//                readViewModel.number_check(auto.getString("HpNum", ""));
                    startTimer_callwating();
                } else {
                    MyLogSupport.log_print("StartTimer실행 else");
                }
            }
        }, 12000);

        try {

            readViewModel.auto_progress.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        MyLogSupport.log_print("autocall : true 승인받았으므로 실행합니다.");
                        readViewModel.callbook_take(auto.getString("CompanyId", ""));// 전화번호부 가져오기

                    } else {
                        toastSupport.showToast("(상담원) 통화가능 상태 확인 바랍니다.");
                        MyLogSupport.log_print("autocall : false 거절");
                    }
                }
            });

            readViewModel.callbook.observe(this, new Observer<CallBook>() {
                @Override
                public void onChanged(CallBook callBook) {
                    if (callBook != null) {
                        MyLogSupport.log_print("전화번호 -> " + callBook.getHpRe());
                        SharedPreferences.Editor autoHp_Re = auto.edit();
                        autoHp_Re.putString("hp_re", callBook.getHpRe());
                        autoHp_Re.apply();
                        Performdial(callBook.getHpRe());
                    }
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.registerTelephonyCallback(getMainExecutor(), callStateListener);
                }
            }

        } catch (Exception e) {
            MyLogSupport.log_print(e.toString());
        }

    }


    public void hangUpCall(Context context) {
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    MyLogSupport.log_print("권한거부로 return~");
                    return;
                }
                telecomManager.endCall();
            }
        }
    }

    public void Performdial(String phone_hp) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        autoCallViewModel.call_status_start(auto.getString("HpNum", ""), phone_hp);

        readViewModel.callbook_re_Successful = true;

        intent.setData(Uri.parse("tel:" + phone_hp));
        firstcall = true;
        try {
            this.startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void resumeActivity() {
        MyLogSupport.log_print("@@@@@@@@ ResumeActivity @@@@@@@@@@@");
    }

    @Override
    protected void startActivity() {

    }

    @Override
    protected void pauseActivity() {
        autoCallViewModel.call_quit = false;
        stopTimer_callwating();
        startTimer_calling();
    }

    @Override
    protected void onRestartActivity() {
        MyLogSupport.log_print("@@@@@@@@ onrestartactivity @@@@@@@@@@@");
        stopTimer_calling();
        autoCallViewModel.call_quit = false;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isHandlerActive) {
                    readViewModel.number_check(auto.getString("HpNum", ""));
                    startTimer_callwating();
                } else {
                    MyLogSupport.log_print("StartTimer실행 else");
                }
            }
        }, 12000);


    }

    @Override
    protected void destroyActivity() {
        binding = null;
        isHandlerActive = false;
        handler.removeCallbacksAndMessages(null);
        stopTimer_callwating();
        stopTimer_calling();
        stopTimer_location();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logout_button) {
            SharedPreferences.Editor autoLoginEdit = auto.edit();
            String serverip = auto.getString("serverip", "");
            autoLoginEdit.clear();
            if(!serverip.isEmpty()){
                autoLoginEdit.putString("serverip", serverip);
            }
            autoLoginEdit.commit();
            startActivity(it);
            finish();
        }
    }

    CallStateListener callStateListener = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
            new CallStateListener() {
                @Override
                public void onCallStateChanged(int state) {
                    switch(state){
                        case 0 :
                            //전화끊김
                            MyLogSupport.log_print("state값 android 13이상 -> " + state );
                            autoCallViewModel.call_quit = false;
                            endcall_restapi();
                            break;
                        case 1 :
                            MyLogSupport.log_print("state값 android 13이상 -> " + state );
                            break;
                        case 2 :
                            //전화검
                            MyLogSupport.log_print("state값 android 13이상 -> " + state );
                            break;
                    }

                }
            }
            : null;

    private void endcall_restapi(){
        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);
        try{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(firstcall) {
                        MyLogSupport.log_print("통화가끝난후 저장된 상대전화번호값 : " + auto.getString("hp_re", null));
                        String re_Hp = auto.getString("hp_re", null);
                        if (re_Hp != null) {

                            if(Build.MANUFACTURER.contains("samsung")){
                                String replace_HP = re_Hp.replace("-", "");

                                String filePath = "/storage/emulated/0/Recordings/Call/";
                                File directory = new File(filePath);
                                File[] files = directory.listFiles();

                                if(files.length > 0){
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recordings/Call/" + files[files.length-1].getName();
                                    if(path.contains(replace_HP)) {
                                        File file = new File(path);
                                        MyLogSupport.log_print("HpNum -> " + auto.getString("HpNum", ""));
                                        MyLogSupport.log_print("가져온 파일이름 -> " + files[files.length-1].getName());
                                        MyLogSupport.log_print("파일업로드 시작");
                                        autoCallViewModel.recordFileUpload(auto.getString("HpNum", ""), file);
                                    }else{
                                        MyLogSupport.log_print("녹음파일이 없습니다.");
                                        autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                    }
                                }else{
                                    MyLogSupport.log_print("녹음파일이 없습니다.");
                                    autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                }

                            }else if(Build.MANUFACTURER.contains("lg")){
                                MyLogSupport.log_print("LG 폰 테스트");
                                String filePath = "/storage/emulated/0/VoiceRecorder/my_sounds/call_rec/";
                                File directory = new File(filePath);
                                File[] files = directory.listFiles();

                                if(files.length > 0) {
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recordings/Call/" + files[files.length-1].getName();
                                    if(path.contains(re_Hp)) {
                                        File file = new File(path);
                                        MyLogSupport.log_print("HpNum -> " + auto.getString("HpNum", ""));
                                        MyLogSupport.log_print("가져온 파일이름 -> " + files[files.length-1].getName());
                                        MyLogSupport.log_print("파일업로드 시작");
                                        autoCallViewModel.recordFileUpload(auto.getString("HpNum", ""), file);
                                    }else{
                                        MyLogSupport.log_print("녹음파일이 없습니다.");
                                        autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                    }
                                }else {
                                    MyLogSupport.log_print("녹음파일이 없습니다.");
                                    autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                }
                            }

                        }

                    }
                }
            }, 2000);

            }
        catch (Exception e){
            MyLogSupport.log_print(e.toString());
        }
    }


    @SuppressLint({"MissingPermission", "ScheduleExactAlarm"})
    public void regist() {
        // 앱 재부팅 실행!!
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // TODO Calendar 시간에 맞춰서넣으면됩니다. calendar.set(이부분)
        Intent intent = new Intent(this, Alarm.class);
        intent.putExtra("app", "restart");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        MyLogSupport.log_print("regist실행");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 57);
        calendar.set(Calendar.SECOND, 0);

        MyLogSupport.log_print("Alarm On : " + calendar.getTime());
        MyLogSupport.log_print("Alarm On : " + calendar.getTimeInMillis());

        // 지정한 시간에 매일 알림
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pIntent);

        alarmManager.setAlarmClock(alarmClockInfo, pIntent);

    }


}
