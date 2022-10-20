package com.example.chaomianqiandao.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chaomianqiandao.FirstApplication;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Network {
    static OkHttpClient okHttpClient= new OkHttpClient();
    static FirstApplication mFirstApplication=FirstApplication.getInstance();
    private static String TAG="Network";

    public static void getSync(String url,Handler handler,int what){
        new Thread(() -> {
            Request request=new Request.Builder().url(url)
                    .addHeader("Accept-Language","zh-Hans-CN;q=1, zh-Hant-CN;q=0.9")
                    .addHeader("cookie",mFirstApplication.infoMap.get("cookies"))
                    .addHeader("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 14_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 com.ssreader.ChaoXingStudy/ChaoXingStudy_3_4.8_ios_phone_202012052220_56 (@Kalimdor)_12787186548451577248")
                    .build();
            Call call=okHttpClient.newCall(request);
            try {
                Log.e("NetWork",request.toString());
                Response response=call.execute();
                ResponseInfo responseInfo=new ResponseInfo(response.headers(),response.body().string());
                Message message=Message.obtain();
                message.what=what;
                message.obj=responseInfo;
                if(handler!=null){
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void postFile(String url, File file, Handler handler, int what){

        new Thread(){
            @Override
            public void run() {
                RequestBody body= new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(),
                                RequestBody.create(MediaType.parse("multipart/form-data"),file))
                        .addFormDataPart("puid",mFirstApplication.infoMap.get("uid"))
                        .build();
                Request request=new Request.Builder().url(url)
                        .addHeader("Accept-Language","zh-Hans-CN;q=1, zh-Hant-CN;q=0.9")
                        .addHeader("cookie",mFirstApplication.infoMap.get("cookies"))
                        .addHeader("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 14_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 com.ssreader.ChaoXingStudy/ChaoXingStudy_3_4.8_ios_phone_202012052220_56 (@Kalimdor)_12787186548451577248")
                        .post(body).build();
                Log.e(TAG,request.toString());
                Call call=okHttpClient.newCall(request);
                try {
                    Response response=call.execute();
                    ResponseInfo responseInfo=new ResponseInfo(response.headers(),response.body().string());
                    Message message=Message.obtain();
                    message.what=what;
                    message.obj=responseInfo;
                    Log.e(TAG, responseInfo.BodyInfo);
                    if(handler!=null){
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //上传完图片即删除
                file.delete();
            }
        }.start();
    }

    public static void firstLogin(String url,Handler handler,int what){

        new Thread(){
            @Override
            public void run() {
                Request request=new Request.Builder().url(url).build();
                Call call=okHttpClient.newCall(request);
                try {
                    Response response=call.execute();
                    String bodyS=response.body().string();
                    ResponseInfo responseInfo=new ResponseInfo(response.headers(),bodyS);
                    Message message=Message.obtain();
                    message.what=what;
                    message.obj=responseInfo;
                    if(handler!=null){
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
