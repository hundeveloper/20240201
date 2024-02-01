package com.ntrobotics.callproject.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.ui.MainActivity;
import com.ntrobotics.callproject.R;

public class RestartReceiver extends BroadcastReceiver {
    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static final int NOTIFICATIONID=0;
    private static final String CHANNEL_ID = "IdRestart";
    private static final String CHANNEL_NAME = "NameRestart";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notifyBuilder;

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLogSupport.log_print("retartrecevier 작동 ::::");

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,notificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            // Manager을 이용하여 Channel 생성
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Log.e("Alarm","재부팅 감지");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_MUTABLE);

                    try {
                        pendingIntent.send();
                        notifyBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setContentTitle("AUTO_CALL")
                                .setContentText("전원 재부팅 후 앱 실행완료 하였습니다.")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentIntent(pendingIntent);

                        notificationManager.notify(NOTIFICATIONID, notifyBuilder.build());

                    } catch (PendingIntent.CanceledException e) {
                        // TODO : Notification channel 만들어서 오류가 났을 경우 notification 띄워서 앱 실행시키게 유도하는 것.
                        notifyBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setContentTitle("AUTO_CALL")
                                .setContentText("앱을 재시작하는도중 오류가 발생하였습니다. 알림을 클릭하여 다시 실행해주세요.")
                                .setSmallIcon(R.drawable.ic_launcher_foreground);

                        notificationManager.notify(NOTIFICATIONID, notifyBuilder.build());

                    }
                } else {
                    Log.e("Alarm", "앱재시작감지");
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }

    }
}
