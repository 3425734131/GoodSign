package com.example.chaomianqiandao.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

//圆角图片
public class ImageViewM extends androidx.appcompat.widget.AppCompatImageView {
    private float[] rids = {10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f,};

    public ImageViewM(Context context) {
        super(context);
    }

    public ImageViewM(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewM(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    protected void onDraw(Canvas canvas) { Path path = new Path() ; int w = this.getWidth() ; int h = this.getHeight() ; path.addRoundRect( new RectF( 0 , 0 , w , h) , rids , Path.Direction. CW) ; canvas.clipPath(path) ; super.onDraw(canvas) ; }}
