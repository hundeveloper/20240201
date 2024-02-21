package com.ntrobotics.callproject.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.ntrobotics.callproject.MyConst;
import com.ntrobotics.callproject.model.AgentState;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.model.CallModel;
import com.ntrobotics.callproject.model.USIMModel;
import com.ntrobotics.callproject.retrofit.BaseRetrofit;
import com.ntrobotics.callproject.retrofit.RetrofitAPI;
import com.ntrobotics.callproject.support.MyLogSupport;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseViewModel<T> extends ViewModel  implements Callback<T> {

    public RetrofitAPI retrofitAPI = null;
    private Handler handler;
    private int requestCnt = 0;
    public int API_ID = -1;

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public abstract void onRFResponseSuccess(int apiId, Response response);
    public abstract void onRFResponseFail(int apiId, String status);

    public BaseViewModel(Context context) { retrofitAPI = BaseRetrofit.getRetrofitBuilder(); }

    public void updateRetrofit() { retrofitAPI = BaseRetrofit.getUpdateRetrofitBuilder();}

    public void commit(int API_ID, String companyId, String hp_number){
        this.API_ID = API_ID;
        requestCnt++;
        switch(API_ID){
            case MyConst.API_Login:
                if(companyId != null && hp_number != null) {
                    RequestBody requestid = RequestBody.create(MediaType.parse("text/plain"), companyId);
                    RequestBody requesthp = RequestBody.create(MediaType.parse("text/plain"), hp_number);
                    retrofitAPI.login(requestid, requesthp).enqueue((Callback<USIMModel>) this);
                }
                break;
            case MyConst.API_NUMBER_CHECK:
                RequestBody requestid = RequestBody.create(MediaType.parse("text/plain"), hp_number);
//                retrofitAPI.call_status("+821057692354").enqueue((Callback<AgentState>) this);
                retrofitAPI.number_check(hp_number).enqueue((Callback<AgentState>) this);
                break;
            case MyConst.API_CALLBOOK_TAKE:
                retrofitAPI.callbook_take(companyId).enqueue((Callback<CallBook>) this);
                MyLogSupport.log_print("Callbook_TAKE 조회...................");
                break;
        }
    }

    public void commit_status(int API_ID, String hp_number, String hp_re_number){
        this.API_ID = API_ID;
        requestCnt++;
        switch (API_ID){
            case MyConst.API_CALL_STATUS_START:
                retrofitAPI.call_start(hp_number, hp_re_number).enqueue((Callback<AgentState>) this);
                break;
            case MyConst.API_CALL_STATUS_CHECK:
                retrofitAPI.calling_check(hp_number).enqueue((Callback<CallModel>) this);
                break;
            case MyConst.API_CALL_STATUS_END:
                retrofitAPI.calling_end(hp_number,"").enqueue((Callback<Void>) this);
                break;
        }
    }


    public void commit_status(int API_ID, String hp_number, String hp_re_number, File file){
        this.API_ID = API_ID;
        requestCnt++;
        switch (API_ID){
            case MyConst.API_CALL_RECORDFILE_UPLOAD:
                MultipartBody.Part files = null;

                if (file != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    files = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                }

                retrofitAPI.recordFileUpload(hp_number,"", files).enqueue((Callback<Void>) this);
                break;
        }
    }

    public void commit_status(int API_ID, String hp_number, Double Latitude, Double Longitude){
        this.API_ID = API_ID;
        requestCnt++;
        switch (API_ID){
            case MyConst.API_CALL_LOCATION_UPLOAD:
                retrofitAPI.locationUpload(hp_number,Latitude, Longitude).enqueue((Callback<Void>) this);
                break;
        }
    }
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        Object dataBody = response.body();
        int responseCode = response.code();

        Log.e("network", "통신성공@@@@@@@@@@@@@@@@@@@");
        if (dataBody == null) {
            MyLogSupport.log_print(String.valueOf(dataBody));
            MyLogSupport.log_print("dataBody(반환값) == Null(비어있어요!)  Check Server!!");
            MyLogSupport.log_print("response(반환값없음?) errorBody :: "+response.errorBody()+ "");
            MyLogSupport.log_print("response.code(반환값코드) :: "+response.code()+ "");
            //return;
        }

        if(response.isSuccessful()) {
            requestDelayedSuccess(API_ID, response);
        }else {
            if (responseCode == 401) {
                requestDelayedFail(API_ID, "401");
            }
            MyLogSupport.log_print("error " + "what code? -> " + responseCode);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        MyLogSupport.log_print("onFailure t -> "+t.toString());
        requestDelayedFail(API_ID, t.toString());
    }

    void requestDelayedSuccess(final int apiId, final Response response) {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCnt--;
                onRFResponseSuccess(apiId, response);

                if (requestCnt <= 0) {
                    requestCnt = 0;
                }
            }
        }, 150);
    }

    void requestDelayedFail(final int apiId, final String status) {
        MyLogSupport.log_print("Check Server!! Fail...");
        MyLogSupport.log_print("requestDelayedFail!! status -> "+status);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCnt--;
                onRFResponseFail(apiId, status);

                if (requestCnt <= 0) {
                    requestCnt = 0;
                }
            }
        }, 100);
    }
}
