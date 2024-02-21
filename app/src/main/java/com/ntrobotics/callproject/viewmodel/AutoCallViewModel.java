package com.ntrobotics.callproject.viewmodel;

import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.lifecycle.MutableLiveData;

import com.ntrobotics.callproject.MyConst;
import com.ntrobotics.callproject.model.AgentState;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.model.CallModel;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;

import java.io.File;

import retrofit2.Response;

public class AutoCallViewModel extends BaseViewModel {

    private MyToastSupport toastSupport;
    public Context context;
    public AutoCallViewModel(Context context) {
        super(context);
        this.context = context;
        this.toastSupport = new MyToastSupport(context);
    }
    public final SingleLiveEvent<Boolean> callstatus = new SingleLiveEvent<>();
    public final SingleLiveEvent<CallModel> callmodelstatus = new SingleLiveEvent<>();
    public final SingleLiveEvent<String> statuscode = new SingleLiveEvent<>();


    public boolean call_quit = false;
    public void call_status_start(String hp_number, String hp_number_re) {
        commit_status(MyConst.API_CALL_STATUS_START, hp_number, hp_number_re);
    }

    public void calling_check(String hp_number){
        commit_status(MyConst.API_CALL_STATUS_CHECK, hp_number, "");
    }

    public void calling_end(String hp_number){
        MyLogSupport.log_print("calling_end() ");
        commit_status(MyConst.API_CALL_STATUS_END ,hp_number, "");
    }

    public void recordFileUpload(String hp_number, File file){
        commit_status(MyConst.API_CALL_RECORDFILE_UPLOAD ,hp_number, "", file);
    }

    public void locationUpload(String hp_number ,Double Latitude, Double Longitude){
        MyLogSupport.log_print(hp_number + Latitude + Longitude);
        commit_status(MyConst.API_CALL_LOCATION_UPLOAD, hp_number,Latitude, Longitude);
    }


    @Override
    public void onRFResponseSuccess(int apiId, Response response) {
        switch(apiId){
            case MyConst.API_CALL_STATUS_START:
                break;

            case MyConst.API_CALL_STATUS_CHECK:
                try {
                    CallModel callModel = (CallModel) response.body();
                    MyLogSupport.log_print("통화Status값 출력 -> " + callModel.getStatus());

                    if (callModel.getStatus().equals("0002") || callModel.getStatus().equals("0006")) { // TODO : 통화중인 상태
                        MyLogSupport.log_print("전화안끊으려고요~");
                    } else { // TODO : 통화가능 상태 두개 조건 두개제외한 코드들은 전화를 끊는다
                        call_quit = true;
                    }
                    statuscode.setValue(callModel.getStatus());
                    break;
                } catch(Exception e){
                    MyLogSupport.log_print(e.toString());

                }

            case MyConst.API_CALL_STATUS_END:

                break;
            case MyConst.API_CALL_RECORDFILE_UPLOAD:
                MyLogSupport.log_print("녹음파일 업로드 성공");
                toastSupport.showToast("녹음파일 업로드 성공");
                break;


        }

    }

    @Override
    public void onRFResponseFail(int apiId, String status) {

        switch (apiId){
            case MyConst.API_CALL_STATUS_END:
                MyLogSupport.log_print("CALL_STATUS_END 통신실패....");
                break;

            case MyConst.API_CALL_RECORDFILE_UPLOAD:
                MyLogSupport.log_print("CALL_RECORDFILE_UPLOAD 통신실패....");
                break;

            case MyConst.API_CALL_STATUS_CHECK:
                MyLogSupport.log_print("CALL_STATUS_CHECK 통신실패....");
                break;


        }
    }


}
