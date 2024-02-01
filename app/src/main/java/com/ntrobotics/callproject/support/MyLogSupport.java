package com.ntrobotics.callproject.support;

import android.util.Log;

public class MyLogSupport {
    public static void log_print(String string){
        Log.d("MyApp", string);
    }
    public static void e_log_print(String string){
        Log.e("MyApp", string);
    }
}
