package com.ntrobotics.callproject;

public final class MyConst {

    public final static boolean DEBUG_MODE = false;
    public final static String NETWORK_TAG = "MY_NETWORK";

    public final static String login = "agentgateway/resource/login/";
    public final static String status = "agentgateway/resource/autocall/";
    public final static String callbook = "agentgateway/resource/HpReList/";

    public final static String callstart = "agentgateway/resource/callStart/";
    public final static String callingcheck = "agentgateway/resource/callingCheck/";

    public final static String callingend = "agentgateway/resource/callEnd/"; // 통화종료정보만

    public final static String recordFileUpload = "agentgateway/resource/recordFileUpload/"; // 파일처리 + 통화종료정보

    public final static String locationUpload = "agentgateway/resource/GPScoordinates/";


    public final static int API_Login = 1;
    public final static int API_NUMBER_CHECK = 2;
    public final static int API_CALLBOOK_TAKE = 3;

    public final static int API_CALL_STATUS_CHECK = 4;


    public final static int API_CALL_STATUS_START = 10;
    public final static int API_CALL_STATUS_END = 11;
    public final static int API_CALL_RECORDFILE_UPLOAD= 12;

    public final static int API_CALL_LOCATION_UPLOAD = 13;


}
