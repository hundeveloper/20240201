package com.ntrobotics.callproject.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.telecom.InCallService;
import android.view.View;

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
    private Timer timer;
    Handler handler;
    private boolean isHandlerActive = true;
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();


    @Override
    protected void createActivity() {
        binding = ActivityAfterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Init();

    }

    private void startTimer() {
        if (timer == null) {
            MyLogSupport.log_print("타이머가 실행되었습니다");
            timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    readViewModel.number_check(auto.getString("HpNum", ""));
                }
            }, 0, 10000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void Init() {
        binding.logoutButton.setOnClickListener(this);
        binding.buttonAutoCall.setOnClickListener(this);
        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);
        it = new Intent(this, MainActivity.class);

        // 현재 이 전화번호가 auto_call을 돌려도되는건지 체크?
        toastSupport.showToast("5초 뒤에 자동오토콜 프로그램이 동작됩니다.");

        handler = new Handler(Looper.getMainLooper());
        startTimer();


        try {
            readViewModel.auto_progress.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        MyLogSupport.log_print("autocall : true 승인받았으므로 실행합니다.");
                        readViewModel.callbook_take(auto.getString("CompanyId", ""));// 전화번호부 가져오기

                    } else {
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

//            readViewModel.autocall_start_and_stop.observe(this, new Observer<Boolean>() {
//                @Override
//                public void onChanged(Boolean aBoolean) {
//                    if (aBoolean) {
//                        MyLogSupport.log_print("autocall_start_and_stop -> true");
//                        readViewModel.number_check(auto.getString("HpNum", ""));
//                    } else {
//                        MyLogSupport.log_print("autocall_start_and_stop -> false");
//                    }
//                }
//            });
        }
        catch(Exception e){
            MyLogSupport.log_print(e.toString());
        }
    }

    public void Performdial(String phone_hp) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

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
        stopTimer();
    }

    @Override
    protected void onRestartActivity() {
        MyLogSupport.log_print("@@@@@@@@ onrestartactivity @@@@@@@@@@@");
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(isHandlerActive) {
//                readViewModel.number_check(auto.getString("HpNum", ""));
                    startTimer();
                }else{
                    MyLogSupport.log_print("StartTimer실행 else");
                }
            }
        }, 10000);


    }

    @Override
    protected void destroyActivity() {
        binding = null;
        isHandlerActive = false;
        handler.removeCallbacksAndMessages(null);
        stopTimer();

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logout_button){
            SharedPreferences.Editor autoLoginEdit = auto.edit();
            autoLoginEdit.clear();
            autoLoginEdit.commit();
            startActivity(it);
            finish();
        } else if( v.getId() == R.id.button_auto_call){

        }
    }
}
