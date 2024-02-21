package com.ntrobotics.callproject.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

import java.io.File;


public class ServiceReceiver extends BroadcastReceiver {

    private Context context;
    private ReadViewModel readViewModel = NtApplication.handlerViewModel.getReadViewModel();
    private final AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();
    private SharedPreferences auto;
    private TelephonyManager telephonyManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        auto = context.getSharedPreferences("autoLogin", context.MODE_PRIVATE);
        this.context = context;

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            MyLogSupport.log_print("여기실행!");
            final Bundle extras = intent.getExtras();
            if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                final String state = extras.getString(TelephonyManager.EXTRA_STATE);
                if ("IDLE".equals(state)) {
//                    autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                    autoCallViewModel.call_quit = false;
                    try {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MyLogSupport.log_print("통화가끝난후 저장된 상대전화번호값 : " + auto.getString("hp_re", null));
                                MyLogSupport.log_print("현재 내 핸드폰 기종 ->" + Build.MANUFACTURER.toString());
                                String re_Hp = auto.getString("hp_re", null);
                                if (re_Hp != null) {
                                    if (Build.MANUFACTURER.contains("samsung")) {
                                        MyLogSupport.log_print("SAMSUNG 폰");
                                        String replace_HP = re_Hp.replace("-", "");

                                        String filePath = "/storage/emulated/0/Recordings/Call/";
                                        File directory = new File(filePath);
                                        File[] files = directory.listFiles();

                                        if (files.length > 0) {
                                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/Call/" + files[files.length - 1].getName();
                                            if (path.contains(replace_HP)) {
                                                File file = new File(path);
                                                MyLogSupport.log_print("HpNum -> " + auto.getString("HpNum", ""));
                                                MyLogSupport.log_print("가져온 파일이름 -> " + files[files.length - 1].getName());
                                                MyLogSupport.log_print("파일업로드 시작");
                                                autoCallViewModel.recordFileUpload(auto.getString("HpNum", ""), file);
                                            } else {
                                                MyLogSupport.log_print("녹음파일이 없습니다.");
                                                autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                            }
                                        } else {
                                            MyLogSupport.log_print("녹음파일이 없습니다.");
                                            autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                        }

                                    } else if (Build.MANUFACTURER.contains("LG")) {
                                        MyLogSupport.log_print("LG 폰 테스트");
                                        String filePath = "/storage/emulated/0/VoiceRecorder/my_sounds/call_rec/";
                                        File directory = new File(filePath);
                                        File[] files = directory.listFiles();

                                        if (files.length > 0) {
                                            String path = filePath + files[files.length - 1].getName();
                                            if (path.contains(re_Hp)) {
                                                File file = new File(path);
                                                MyLogSupport.log_print("HpNum -> " + auto.getString("HpNum", ""));
                                                MyLogSupport.log_print("가져온 파일이름 -> " + files[files.length - 1].getName());
                                                MyLogSupport.log_print("파일업로드 시작");
                                                autoCallViewModel.recordFileUpload(auto.getString("HpNum", ""), file);
                                            } else {
                                                MyLogSupport.log_print("녹음파일이 없습니다.");
                                                autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                            }
                                        } else {
                                            MyLogSupport.log_print("녹음파일이 없습니다.");
                                            autoCallViewModel.calling_end(auto.getString("HpNum", ""));
                                        }
                                    }
                                }

                            }
                        }, 2000);

                    } catch (Exception e) {
                        MyLogSupport.log_print(e.toString());
                    }
                }

                if ("OFFHOOK".equals(state)) {
                    MyLogSupport.log_print("전화끊음!");

                }
                if ("RINGING".equals(state)) {
                    MyLogSupport.log_print("전화울림!");

                }
            }
//            telephonyManager.registerTelephonyCallback(context.getMainExecutor(), callStateListener);

        } else {
            MyLogSupport.log_print("일단 Android 12 이상으로 실행은되었습니당");

        }


    }


}
