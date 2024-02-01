package com.ntrobotics.callproject.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ntrobotics.callproject.R;

public class MyToastSupport {
    public Context context = null;
    public static Toast Utiltoast = null;
    public View layout = null;

    public MyToastSupport(Context context){
        this.context = context;
    }

    public void showToast(String content){

        if(Utiltoast != null){
            Utiltoast.cancel(); // 중간에 떠있는 toast를 삭제
            Utiltoast = null;
        }

        View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        TextView message = layout.findViewById(R.id.toast_message);
        message.setText(content);

        Utiltoast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        Utiltoast.setView(layout);
        Utiltoast.show();
    }


}
