package com.ntrobotics.callproject.di;

import android.content.Context;

import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;
import com.ntrobotics.callproject.viewmodel.ReadViewModel;

public class HandlerBase {
    public HandlerBase(Context context) {
        this.context = context;
    }

    private Context context;
    private static ReadViewModel readviewmodel = null;
    private static AutoCallViewModel autoCallViewModel = null;


    public ReadViewModel getReadViewModel() {
        if (readviewmodel == null) {
            readviewmodel = new ReadViewModel(context);
        }
        return readviewmodel;
    }

    public AutoCallViewModel getAutoCallViewModel() {
        if (autoCallViewModel == null){
            autoCallViewModel = new AutoCallViewModel(context);
        }
        return autoCallViewModel;
    }
}
