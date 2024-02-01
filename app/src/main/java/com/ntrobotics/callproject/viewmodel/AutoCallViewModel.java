package com.ntrobotics.callproject.viewmodel;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.ntrobotics.callproject.MyConst;
import com.ntrobotics.callproject.model.AgentState;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.model.CallModel;
import com.ntrobotics.callproject.support.MyLogSupport;

import retrofit2.Response;

public class AutoCallViewModel extends BaseViewModel {

    public Context context;
    public AutoCallViewModel(Context context) { super(context); }

    public final SingleLiveEvent<Boolean> callstatus = new SingleLiveEvent<>();
    public final SingleLiveEvent<CallModel> callmodelstatus = new SingleLiveEvent<>();
    public final SingleLiveEvent<String> statuscode = new SingleLiveEvent<>();

    public final SingleLiveEvent<Boolean> timer_Setting = new SingleLiveEvent<>();

    public boolean call_quit = false;
    public void call_status_start(String hp_number, String hp_number_re) {
        commit_status(MyConst.API_CALL_STATUS_START, hp_number, hp_number_re);
    }

    public void calling_check(String hp_number){
        commit_status(MyConst.API_CALL_STATUS_CHECK, hp_number, "");
    }

    public void calling_end(String hp_number){
        commit_status(MyConst.API_CALL_STATUS_END ,hp_number, "");
    }

    @Override
    public void onRFResponseSuccess(int apiId, Response response) {
        switch(apiId){
            case MyConst.API_CALL_STATUS_START:
                break;

            case MyConst.API_CALL_STATUS_CHECK:
                CallModel callModel = (CallModel) response.body();
                MyLogSupport.log_print("통화Status값 출력 -> "+callModel.getStatus());
                if(!callModel.getStatus().equals("0006")){
                    call_quit = true;
                }
                statuscode.setValue(callModel.getStatus());
                break;
            case MyConst.API_CALL_STATUS_END:

                break;
        }

    }

    @Override
    public void onRFResponseFail(int apiId, String status) {
        MyLogSupport.log_print("통신실패....");
    }


}
