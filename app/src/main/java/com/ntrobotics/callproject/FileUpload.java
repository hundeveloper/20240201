package com.ntrobotics.callproject;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ntrobotics.callproject.databinding.ActivityFileBinding;
import com.ntrobotics.callproject.support.MyLogSupport;
import com.ntrobotics.callproject.ui.BaseActivity;
import com.ntrobotics.callproject.viewmodel.AutoCallViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FileUpload extends BaseActivity {

    private AutoCallViewModel autoCallViewModel = NtApplication.handlerViewModel.getAutoCallViewModel();

    Handler handler = new Handler();

    /*********  work only for Dedicated IP ***********/
    static final String FTP_HOST= "192.168.0.193";

    /*********  FTP USERNAME ***********/
    static final String FTP_USER = "ntrobo";

    /*********  FTP PASSWORD ***********/
    static final String FTP_PASS  ="!Q2w3e4r5t";

    File[] imageFiles;
    String imageFname;

    private String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_AUDIO
            };//권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101;//권한 동의 여부 문의 후 callback함수에 쓰일 변수

    ActivityFileBinding binding;
    Button btn;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    @SuppressLint("InlinedApi")
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permission3 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO);


        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                activity.startActivity(intent);
            }
        }

    }

    @Override
    protected void createActivity() {
        binding = ActivityFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        btn = binding.buttonFile;
        btn.setOnClickListener(this);
        verifyStoragePermissions(this);

        if(Build.VERSION.SDK_INT>22){
            checkPermissions();
        }

//        imageFiles=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recordings").listFiles();
//        imageFname=imageFiles[0].toString();
//        MyLogSupport.log_print("imageFname : "+imageFname);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).start();


        try{
            //디바이스 제조사



            MyLogSupport.log_print("--- getManufacturer : " + Build.MANUFACTURER);  //제조사


            String re_Hp = "010-5658-7246";
            String replace_HP = re_Hp.replace("-", "");

                if(Build.MANUFACTURER.equals("samsung")){
                    String filePath = "/storage/emulated/0/Recordings/Call/";
                    File directory = new File(filePath);
                    File[] files = directory.listFiles();

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recordings/Call/" + files[files.length-1].getName();

                    if(path.contains(replace_HP)) {
                        File file = new File(path);
//                        MyLogSupport.log_print(file.getName());
                        MyLogSupport.log_print("files getname : " + files[files.length-1].getName());

                        Calendar calendar = Calendar.getInstance();
                        Date today = calendar.getTime(); // 현재날짜
                        calendar.add(Calendar.DAY_OF_MONTH, 0);// 7일전날짜

                        Date lastModifiedDate = new Date(file.lastModified());
                        Date sevenDaysAgo = calendar.getTime(); // TODO : 7일전날짜


                        if(sevenDaysAgo.before(lastModifiedDate)){
                            MyLogSupport.log_print("파일 7일이 지났습니다.");
                        }else{
                            MyLogSupport.log_print("파일 7일이 안지났습니다.");
                        }



//                        autoCallViewModel.recordFileUpload("010-5658-7246", file);
                    }
                }else{
                    MyLogSupport.log_print("LG 폰 테스트");
                    MyLogSupport.log_print(Environment.getExternalStorageDirectory().getAbsolutePath());
                    String filePath = "/storage/emulated/0/VoiceRecorder/my_sounds/call_rec/";
                    File directory = new File(filePath);
                    File[] files = directory.listFiles();
                    MyLogSupport.log_print("가져온 파일이름 -> " + files[files.length-1].getName());

                    String path = filePath + files[files.length-1].getName();
                    File file = new File(path);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -7); //2024-02-06 11:26
                    Date date = calendar.getTime();

                    for(File file1 : files){
                        Date file_currentDates = new Date(file1.lastModified());
                        if(file_currentDates.before(date)){
                            boolean test = file1.delete();
                            if(test){
                                MyLogSupport.log_print("파일삭제완료");
                            }else{
                                MyLogSupport.log_print("파일삭제안됐음");
                            }
                        }
                    }




                }

        }
        catch (Exception e){
            MyLogSupport.log_print(e.toString());
        }

    }



    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);//현재 컨텍스트가 pm 권한을 가졌는지 확인
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우
                MyLogSupport.log_print(pm+" -> 권한없음!");
                permissionList.add(pm);//리스트에 해당 권한명을 추가한다
            }
        }
        if (!permissionList.isEmpty()) {//권한이 추가되었으면 해당 리스트가 empty가 아니므로, request 즉 권한을 요청한다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
//                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    //Uri -> Path(파일경로)
    public static String uri2path(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

        cursor.close();
        return path;
    }

    //Path(파일경로) -> Uri
    public static Uri path2uri(Context context, String filePath) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();
        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
    @Override
    protected void resumeActivity() {

    }

    @Override
    protected void startActivity() {

    }

    @Override
    protected void pauseActivity() {

    }

    @Override
    protected void onRestartActivity() {

    }

    @Override
    protected void destroyActivity() {

    }


    @Override
    public void onClick(View v) {

    }
}
