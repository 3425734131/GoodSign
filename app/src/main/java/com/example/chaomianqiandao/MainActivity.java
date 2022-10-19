package com.example.chaomianqiandao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.chaomianqiandao.utils.Network;
import com.example.chaomianqiandao.utils.ResponseInfo;

import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Headers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_account;
    private EditText et_password;
    private final static int Login=100;
    private String TAG="MainActivity";
    private FirstApplication mFirstApplication=FirstApplication.getInstance();


    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Login:
                    ResponseInfo info=(ResponseInfo) msg.obj;
                    Headers headers=info.HeaderInfo;
                    Log.e(TAG,info.BodyInfo);
                    JSONObject jsonObject=JSONObject.parseObject(info.BodyInfo);
                    if(jsonObject.getBoolean("status")){
                        List<String> list=headers.values("Set-Cookie");
                        StringBuilder cookies=new StringBuilder();
                        String uid=null;
                        if(list!=null&&list.size()>0){
                            for (int i=0;i<list.size();i++){
                                String temp=list.get(i).split(";")[0];
                                if(temp.startsWith("UID"))
                                    uid=temp.substring(4);
                                if(temp.startsWith("JSESSIONID"))
                                    continue;
                                cookies.append(temp).append(";");
                            }

                        }else {
                            promptDialog.showError("Cookies获取失败!");
                        }
                        mFirstApplication.infoMap.put("uid",uid);
                        mFirstApplication.infoMap.put("cookies",cookies.toString());
                        Intent intent=new Intent(MainActivity.this,CourseList.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        promptDialog.showSuccess("登陆成功，等待跳转！");

                        //保存账号和密码
                        SharedPreferences.Editor editor = account.edit();
                        editor.putString("account",et_account.getText().toString());
                        editor.putString("password",et_password.getText().toString());
                        editor.commit();
                        startActivity(intent);
                    }else {
                        promptDialog.showError("登陆失败！请检查手机号或者密码！");
                    }
                    break;
            }
        }
    };
    private SharedPreferences account;
    private PromptDialog promptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_account = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        Button login = findViewById(R.id.bt_login);
        login.setOnClickListener(this);
        promptDialog = new PromptDialog(this);
        account = getSharedPreferences("account_and_password", MODE_PRIVATE);
        if(account.contains("account")){
            et_account.setText(account.getString("account",""));
            et_password.setText(account.getString("password",""));
            if(account.getString("account","").length()>10)
            {
                login();
            }
        }
    }

    @Override
    public void onClick(View v) {
        login();
    }

    private void login() {
        if(System.currentTimeMillis()>1668180099000l){
            Toast.makeText(mFirstApplication, "内测版本设定到期时间为2022-11-11 23:21:39", Toast.LENGTH_SHORT).show();
            Toast.makeText(mFirstApplication, "软件已过期！请关注本人是否发布新版本..", Toast.LENGTH_SHORT).show();
            return;
        }
        promptDialog.showLoading("正在登陆...");

        String par1= et_account.getText().toString();
        String par2= et_password.getText().toString();
        if(par1.length()!=11){
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder url=new StringBuilder("https://passport2-api.chaoxing.com/v11/loginregister?code=");
        url.append(par2).append("&cx_xxt_passport=json&uname=");
        url.append(par1).append("&loginType=1&roleSelect=true");
        Network.firstLogin(url.toString(),handler,Login);
    }
}