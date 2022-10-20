package com.example.chaomianqiandao;

import android.app.Application;
import java.util.HashMap;


public class FirstApplication extends Application {
    private final static String TAG="FirstApplication";
    private static FirstApplication mFirstApplication;  // 声明一个当前应用的静态实例
    // 声明一个公共的信息映射对象，可当作全局变量使用
    public HashMap<String, String> infoMap = new HashMap<String, String>();
    public static FirstApplication getInstance() {
        return mFirstApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFirstApplication=this;
    }
}
