package com.example.chaomianqiandao;


import static com.example.chaomianqiandao.utils.Fileutils.getRealFilePathFromUri;
import static com.example.chaomianqiandao.utils.PermissionUtil.checkGrant;
import static com.example.chaomianqiandao.utils.PermissionUtil.checkPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chaomianqiandao.utils.ClipImageActivity;
import com.example.chaomianqiandao.utils.Fileutils;
import com.example.chaomianqiandao.utils.Network;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class CodeScanning extends AppCompatActivity implements QRCodeView.Delegate {

    private final static int TAKE_PHOTO=150;
    private final static int REQUEST_CROP_PHOTO=151;
    //通讯录所需要的权限
    private final String[] PERMISSION_SCAN=new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE
    };
    private ZXingView scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanning);
        scanner = findViewById(R.id.scanner);
        TextView album= findViewById(R.id.album);
        album.setOnClickListener(v -> gotoPic());
        //检查权限
        checkPermissions(this,PERMISSION_SCAN,1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        scanner.setDelegate(CodeScanning.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanner.startCamera();       // 打开后置摄像头开始预览，但是并未开始识别
        scanner.startSpotAndShowRect();   // 显示扫描框，并开始识别
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!checkGrant(grantResults)){
            checkPermissions(this,PERMISSION_SCAN,1);
            jumpToSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanner.stopCamera();
        scanner.stopSpotAndHiddenRect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.onDestroy();
        scanner.destroyDrawingCache();
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

    //跳转到应用设置页面
    public void jumpToSettings(){
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    //处理扫描结果
    @Override
    public void onScanQRCodeSuccess(String result) {
        vibrate();
        Intent intent=new Intent();
        intent.putExtra("result",result);
        setResult(RESULT_OK,intent);
        finish();
    }



    //摄像头亮度变化
    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(this, "相机打开失败！", Toast.LENGTH_SHORT).show();
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=RESULT_OK)
            return;
        switch (requestCode){
            case TAKE_PHOTO:
                gotoClipActivity(data.getData());
                break;
            case REQUEST_CROP_PHOTO:
                final Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                String cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
                Log.e("crop",cropImagePath);
                Bitmap bitMap = BitmapFactory.decodeFile(cropImagePath);
                scanner.decodeQRCode(bitMap);
                break;
        }
    }


    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, ClipImageActivity.class);
        intent.putExtra("type", 2);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }
}