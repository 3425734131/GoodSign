package com.example.chaomianqiandao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.chaomianqiandao.utils.Fileutils;
import com.example.chaomianqiandao.utils.Network;
import com.example.chaomianqiandao.utils.PermissionUtil;
import com.example.chaomianqiandao.utils.ResponseInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.leefeng.promptlibrary.PromptDialog;

public class Sign extends AppCompatActivity {

    private int type;
    private final static int SIGN0 = 100;
    private final static int SIGN1 = 101;
    private final static int SIGN4 = 104;
    private final static int TAKE_PHOTO = 150;
    private final static int Referer = 200;
    private final static int UPLOAD_IMAGE = 201;
    private final static int ScanCode = 202;
    private final static int Token = 110;
    private boolean flag = false;
    private FirstApplication mFirstApplication = FirstApplication.getInstance();
    String[] PERMISSON = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String objectId;
    private String token;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SIGN0:
                    ResponseInfo info = (ResponseInfo) msg.obj;
                    if (info.BodyInfo.contains("签到成功")) {
                        promptDialog.showSuccess("签到成功！");
                        Toast.makeText(Sign.this, "签到成功！", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        promptDialog.showError("未知错误~");
                        Toast.makeText(Sign.this, "", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SIGN1:
                    ResponseInfo info1 = (ResponseInfo) msg.obj;
                    Log.e("SIGN1", info1.BodyInfo);
                    promptDialog.showSuccess("签到成功！");
                    Toast.makeText(Sign.this, "签到成功！", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case SIGN4:
                    ResponseInfo info4 = (ResponseInfo) msg.obj;
                    Log.e("SIGN4", info4.BodyInfo);
                    Toast.makeText(mFirstApplication, info4.BodyInfo, Toast.LENGTH_SHORT).show();
                    if (info4.BodyInfo.equals("success")) {
                        promptDialog.showSuccess("签到成功！");
                        Toast.makeText(Sign.this, "签到成功！", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        promptDialog.showError(info4.BodyInfo);
                        Toast.makeText(Sign.this, info4.BodyInfo, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Referer:
                    flag = true;
                    sign();
                    break;
                case UPLOAD_IMAGE:
                    ResponseInfo info9 = (ResponseInfo) msg.obj;
                    JSONObject jsonObject = JSONObject.parseObject(info9.BodyInfo);
                    if (jsonObject.getString("msg").equals("success")) {
                        promptDialog.showSuccess("上传成功！");
                        objectId = jsonObject.getString("objectId");
                    } else {
                        promptDialog.showError("上传失败...未知错误");
                    }
                    break;
                case Token:
                    ResponseInfo info10 = (ResponseInfo) msg.obj;
                    Log.e("Token", info10.BodyInfo);
                    JSONObject jsonObject10 = JSONObject.parseObject(info10.BodyInfo);
                    token = jsonObject10.getString("_token");
                    break;
            }
        }
    };
    private String aid;
    private String sign_code;
    private String content;
    private EditText address;
    private EditText wei;
    private EditText jing;
    private ImageView imageView;
    private TextView tv_sign_code;
    private String realPath;
    private PromptDialog promptDialog;
    private SharedPreferences shared_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        TextView sign_name = findViewById(R.id.sign_name);
        TextView sign_course_name = findViewById(R.id.sign_course_name);
        TextView hint=findViewById(R.id.hint);
        Intent intent = getIntent();
        sign_course_name.setText(intent.getStringExtra("name"));
        aid = intent.getStringExtra("aid");
        sign_code = String.valueOf(getIntent().getStringExtra("sign_code"));
        tv_sign_code = findViewById(R.id.sign_code);
        content = getIntent().getStringExtra("content");
        //签到按钮监听
        findViewById(R.id.sign_button).setOnClickListener(v -> {
            sign();
        });
        if (content != null)
            content = content.replace("\\", "");

        promptDialog = new PromptDialog(this);

        //选取图片
        imageView = findViewById(R.id.video_profile);
        imageView.setOnClickListener(v -> gotoPic());

        //经纬度输入框
        jing = findViewById(R.id.jing);
        wei = findViewById(R.id.wei);
        address = findViewById(R.id.address);
        shared_address = getSharedPreferences("account_and_password", MODE_PRIVATE);
        if (shared_address.contains("address")) {
            address.setText(shared_address.getString("address", ""));
        }

        if (content != null&&content.length()>25) {
            JSONObject con = JSONObject.parseObject(content);
            jing.setText(con.getString("locationLongitude"));
            wei.setText(con.getString("locationLatitude"));
        }

        //获取token
        Network.getSync("https://pan-yz.chaoxing.com/api/token/uservalid", handler, Token);

        //设置签到类型名称
        sign_name.setText(getIntent().getStringExtra("sign_name"));
        type = getIntent().getIntExtra("sign_type", 0);


        switch (type) {
            case 0:
                hint.setText("直接点击签到即可！");
                break;
            case 1:
                imageView.setVisibility(View.VISIBLE);
                hint.setText("从相册选择图片后点击签到按钮\n如果直接点击签到为不上传图片！");
                break;
            case 2:
            case 3:
                hint.setText("请点击签到按钮\n然后选择直接扫码或者相册打开图片\n如果二维码图片不清晰，请多试几次");
                break;
            case 4:
                tv_sign_code.setText("签到手势是:" + sign_code);
                tv_sign_code.setVisibility(View.VISIBLE);
                hint.setText("直接点击签到即可，手势可以分享小伙伴！");
                break;
            case 5:
                findViewById(R.id.dingwei).setVisibility(View.VISIBLE);
                hint.setText("一定要修改地址显示\n这是老师可以看到的定位信息\n经纬度默认是签到范围的中心点\n如果教师未指定位置请自行设定经纬度");
                break;
            case 6:
                tv_sign_code.setText("签到码是:" + sign_code);
                tv_sign_code.setVisibility(View.VISIBLE);
                hint.setText("直接点击签到即可，签到码可以分享小伙伴！");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtil.checkPermissions(Sign.this,PERMISSON,0);
    }

    void sign(){
        //保存地址
        SharedPreferences.Editor editor = shared_address.edit();
        editor.putString("address",address.getText().toString());
        editor.commit();
        switch (type){
            case 0:
                //普通签到
                promptDialog.showLoading("正在签到中...");
                String url0="https://mobilelearn.chaoxing.com/widget/sign/pcStuSignController/preSign?activeId="+getIntent().getStringExtra("aid");
                Network.getSync(url0,handler,SIGN0);
                break;
            case 1:
                //拍照签到
                promptDialog.showLoading("正在签到中...");
                if(flag){
                    String url1;
                    if(objectId!=null){
                        url1="https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+aid+"&uid="+mFirstApplication.infoMap.get("uid")+"&appType=15&fid=0&objectId="+objectId;
                    }else {
                        url1="https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+aid+"&uid="+mFirstApplication.infoMap.get("uid")+
                                "&clientip=&useragent=&latitude=-1&longitude=-1&appType=15&fid=0&objectId="+"";
                    }
                    Network.getSync(url1,handler,SIGN1);
                }else{
                    Network.getSync(getReferer(),handler,Referer);
                }
                break;
            case 2:
            case 3:
                promptDialog.showLoading("跳转到扫码页面...");
                if(flag){
                    Intent intent=new Intent(this,CodeScanning.class);
                    startActivityForResult(intent,ScanCode);
                }else {
                    Network.getSync(getReferer(),handler,Referer);
                }
                break;
            case 4:
            case 6:
                promptDialog.showLoading("正在签到中...");
                //签到码签到、手势签到
                //先访问referer，才能进一步签到
                if (flag){
                    String url4="https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+aid;
                    Network.getSync(url4,handler,SIGN4);
                }else {
                    Network.getSync(getReferer(),handler,Referer);
                }
                break;
            case 5:
                promptDialog.showLoading("正在签到中...");
                //定位签到
                if (flag){
                    String url5="https://mobilelearn.chaoxing.com/pptSign/stuSignajax?address="+urlEncodeChinese(address.getText().toString())
                            +"&activeId="+aid+"&uid="+mFirstApplication.infoMap.get("uid")+"&clientip=&latitude="+
                            wei.getText().toString()+"&longitude="+jing.getText().toString();
                    Network.getSync(url5,handler,SIGN4);
                }else {
                    Network.getSync(getReferer(),handler,Referer);
                }
                break;
        }
    }


    String getReferer(){
        return getIntent().getStringExtra("url");
    }

    //将字符串改编码
    private String urlEncodeChinese(String url) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
            String tmp = "";
            while (matcher.find()) {
                tmp = matcher.group();
                url = url.replaceAll(tmp, URLEncoder.encode(tmp, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url.replace(" ", "%20");
    }

    void gotoPic(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                    TAKE_PHOTO);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, TAKE_PHOTO);
        }
    }

    private void uploadImage(String path) {
        promptDialog.showLoading("上传图片中...");
        String url_upload="https://pan-yz.chaoxing.com/upload?_token="+token;
        File file=new File(path);
        Bitmap bitmap= BitmapFactory.decodeFile(path);
        int width=bitmap.getWidth();
        int scale=width/500;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=scale;
        String[] s=path.split("\\.");
        String format=s[s.length-1];
        //文件大于 1mb
        if(scale>1||file.length()>500*1000){

            String fileName= Fileutils.checkDirPath(Sign.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/image")+System.currentTimeMillis()+"."+format;
            if(format.startsWith("jp")){
                try {
                    OutputStream os=new FileOutputStream(fileName);
                    bitmap=BitmapFactory.decodeFile(path,options);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,60,os);
                    os.close();
                    File temp=new File(fileName);
                    Network.postFile(url_upload,temp,handler,UPLOAD_IMAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (format.startsWith("png")){
                try {
                    OutputStream os=new FileOutputStream(fileName);
                    bitmap=BitmapFactory.decodeFile(path,options);
                    bitmap.compress(Bitmap.CompressFormat.PNG,60,os);
                    os.close();
                    File temp=new File(fileName);
                    Network.postFile(url_upload,temp,handler,UPLOAD_IMAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Network.postFile(url_upload,file,handler,UPLOAD_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case TAKE_PHOTO:
                    realPath = Fileutils.getRealFilePathFromUri(this,data.getData());
                    if (realPath!=null&&realPath.length()>1){
                        Log.e("realPath",realPath);
                        Log.e("data",data.getData().toString());
                        uploadImage(realPath);
                        imageView.setImageBitmap(BitmapFactory.decodeFile(realPath));
                    }
                    else{
                        Toast.makeText(mFirstApplication, "选择照片失败！", Toast.LENGTH_SHORT).show();
                        promptDialog.showError("选择照片失败！");
                    }
                    break;
                case ScanCode:
                    Log.e("result",data.getStringExtra("result"));
                    String enc=data.getStringExtra("result");
                    for (String temp:enc.split("&")){
                        if(temp.startsWith("enc"))
                        {
                            enc="&"+temp;
                            break;
                        }
                    }
                    if(enc.length()>5){
                        String url1="https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+aid+"&uid="+mFirstApplication.infoMap.get("uid")+enc;
                        Network.getSync(url1,handler,SIGN4);
                    }
                    break;
            }
        }else {
            promptDialog.showError("操作失败！");
        }
    }
}