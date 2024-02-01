package com.ntrobotics.callproject.ui;


import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    protected abstract void createActivity();
    protected abstract void resumeActivity();
    protected abstract void startActivity();
    protected abstract void pauseActivity();
    protected abstract void onRestartActivity();
    protected abstract void destroyActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e("myapp-----lifecycle", "onCreate");
        this.createActivity();
    }



    @Override
    protected void onStart() {
        super.onStart();
//        Log.e("myapp-----lifecycle", "onStart");
        startActivity();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.e("myapp-----lifecycle", "onrestart");
        this.onRestartActivity();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e("myapp-----lifecycle", "onResume");
        this.resumeActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.e("myapp-----lifecycle", "OnPause");
        this.pauseActivity();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.e("myapp-----lifecycle", "onDestroy");
        this.destroyActivity();

    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}

