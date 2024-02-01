package com.ntrobotics.callproject.retrofit;


import com.ntrobotics.callproject.MyConst;
import com.ntrobotics.callproject.model.AgentState;
import com.ntrobotics.callproject.model.CallBook;
import com.ntrobotics.callproject.model.CallModel;
import com.ntrobotics.callproject.model.USIMModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitAPI {

    @Multipart
    @POST(MyConst.login)
    Call<USIMModel> login(@Part("COMPANY_ID") RequestBody companyId, @Part("HP") RequestBody usimNo);

    @GET(MyConst.status)
    Call<AgentState> number_check(@Query("HP") String hp);

    @GET(MyConst.callbook)
    Call<CallBook> callbook_take(@Query("COMPANY_ID") String Company);

    @GET(MyConst.callstart)
    Call<AgentState> call_start(@Query("HP") String HP, @Query("HP_RE") String HP_RE);

    @GET(MyConst.callingcheck)
    Call<CallModel> calling_check(@Query("HP") String HP);

    @POST(MyConst.callingend)
    Call<Void> calling_end(@Query("HP") String HP, @Query("CALL_TYPE") String CALL_TYPE);

}

