package com.ntrobotics.callproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.R;
import com.ntrobotics.callproject.databinding.ActivityAfterBinding;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.util.Timer;
import java.util.TimerTask;


public class AfterActivity extends BaseActivity {

    private ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private ActivityAfterBinding binding;
    private SharedPreferences auto;
    private Intent it;
    private MyToastSupport toastSupport = new MyToastSupport(this);
    private Timer timer, timer_calling;
    Handler handler;
    private boolean isHandlerActive = true;
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();


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
                        MyLogSupport.log_print("통화종료 메소드 실행@@@@@@@@@@@@@@@@");
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

    private void life_autocalling_observer() {
        autoCallViewModel.timer_Setting.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    MyLogSupport.log_print("통화중 : 타이머 시작@@@@@@@@@@@@@@@@@@@@@");
                    startTimer_calling();

                } else {
                    MyLogSupport.log_print("통화종료 : 타이머를 중단@@@@@@@@@@@@@@@");
                    stopTimer_calling();
                }
            }
        });
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

    private void Init() {
        binding.logoutButton.setOnClickListener(this);
        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);
        it = new Intent(this, MainActivity.class);

        // 현재 이 전화번호가 auto_call을 돌려도되는건지 체크?
        toastSupport.showToast("곧 자동오토콜 프로그램이 동작됩니다..");

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
                        Performdial(callBook.getHpRe());
                    }
                }
            });


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

        intent.setData(Uri.parse("tel:" + phone_hp));
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

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(isHandlerActive) {
//                readViewModel.number_check(auto.getString("HpNum", ""));
                    startTimer_callwating();
                }else{
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

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logout_button){
            SharedPreferences.Editor autoLoginEdit = auto.edit();
            autoLoginEdit.clear();
            autoLoginEdit.commit();
            startActivity(it);
            finish();
        }
    }
}
