package com.ntrobotics.callproject.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.ntrobotics.callproject.MyConst;
import com.ntrobotics.callproject.model.AgentState;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.model.USIMModel;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.support.MyToastSupport;

import retrofit2.Response;

public class ReadViewModel extends BaseViewModel {

    private MyToastSupport toastSupport;
    Context context;
    public ReadViewModel(Context context) {
        super(context);
        this.context = context;
        this.toastSupport = new MyToastSupport(context);
    }

    public final SingleLiveEvent<Boolean> loginsuccess = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> server_online = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> auto_progress = new SingleLiveEvent<>();
    public final SingleLiveEvent<CallBook> callbook = new SingleLiveEvent<>();
    public boolean callbook_re_Successful = false;


    public void login(String companyId, String hp_number) { commit(MyConst.API_Login, companyId, hp_number);}
    public void number_check(String hp_number) { commit(MyConst.API_NUMBER_CHECK, null, hp_number); }
    public void callbook_take(String companyId) { commit(MyConst.API_CALLBOOK_TAKE, companyId, ""); }

    @Override
    public void onRFResponseSuccess(int apiId, Response response) {

        if(apiId == MyConst.API_Login){
           USIMModel usimModel = (USIMModel) response.body();
           MyLogSupport.log_print("API_LOGIN : "+usimModel.getSuccess().toString());
           loginsuccess.setValue(usimModel.getSuccess());

        }else if(apiId == MyConst.API_NUMBER_CHECK){
            AgentState agentState = (AgentState) response.body();
            MyLogSupport.log_print("API_NUMBER_CHECK : "+agentState.getStatus().toString());
            auto_progress.setValue(agentState.getStatus());

        }else if(apiId == MyConst.API_CALLBOOK_TAKE) {
            try {
                CallBook callBook = (CallBook) response.body();
                callbook.setValue(callBook);
            }catch (Exception e) {
                MyLogSupport.log_print(e.toString());
            }
        }
    }

    @Override
    public void onRFResponseFail(int apiId, String status) {
        if(apiId == MyConst.API_Login){
            server_online.setValue(true);
        }else if(apiId == MyConst.API_CALLBOOK_TAKE){
            toastSupport.showToast("전화번호를 전부 호출하였습니다.");

        }
    }

}
