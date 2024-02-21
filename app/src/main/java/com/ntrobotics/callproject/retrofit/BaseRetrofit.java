package com.ntrobotics.callproject.retrofit;

import android.annotation.SuppressLint;


import com.ntrobotics.callproject.NtApplication;
import com.ntrobotics.callproject.network.ServerAddress;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseRetrofit {

    private static RetrofitAPI retrofitAPI = null;

    public static RetrofitAPI getRetrofitBuilder(){
        if(retrofitAPI == null && !NtApplication.serverip.isEmpty()){
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NtApplication.serverip)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build()).build();

            retrofitAPI = retrofit.create(RetrofitAPI.class);
        }

        return retrofitAPI;
    }

    public static RetrofitAPI getUpdateRetrofitBuilder(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NtApplication.serverip)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build()).build();

        retrofitAPI = retrofit.create(RetrofitAPI.class);

        return retrofitAPI;
    }



    private static OkHttpClient.Builder getUnsafeOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS);
        builder.hostnameVerifier(new HostnameVerifier() {
            @SuppressLint("BadHostnameVerifier")
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return builder;
    }
}
