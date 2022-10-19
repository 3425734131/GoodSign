package com.example.chaomianqiandao.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {

    //判断是否所有权限都被批准
    public static boolean checkGrant(int[] grantResults){
        if(grantResults!=null){
            for(int grant:grantResults){
                if(grant==PackageManager.PERMISSION_DENIED)
                    return false;
            }
        }
        return true;
    }


    public static boolean checkPermission(Activity activity,String permission,int requestCode){
        return checkPermissions(activity,new String[]{permission},requestCode);
    }

    public static boolean checkPermissions(Activity activity,String[] permissions,int requestCode)
    {
        //在Android6.0之后才需要获取动态权限
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int check;
            for(String permission:permissions){
                //监测permission权限是否批准
                check=ContextCompat.checkSelfPermission(activity,permission);
                if(check!=PackageManager.PERMISSION_GRANTED){  //当前权限没开启
                    //请求系统弹窗，申请权限
                    ActivityCompat.requestPermissions(activity,permissions,requestCode);
                    return false;
                }
            }
        }
        return true;
    }

}
