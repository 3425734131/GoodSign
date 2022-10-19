package com.example.chaomianqiandao.refresh.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * 下拉刷新的基类，继承该类可以实现不同样式的刷新头，但是基本的刷新过程不变，都是：
 *      下拉--释放--加载（悬停）--回弹结束
 * 刷新头，一共有4种状态：
 *      a、下拉刷新
 *      b、释放立即刷新
 *      c、正在刷新
 *      d、刷新完成
 */

public abstract class BaseHeaderView extends FrameLayout{
    public final static int HEADER_DRAG = 0x001;//下拉刷新
    public final static int HEADER_RELEASE = 0x002;//释放立即刷新
    public final static int HEADER_REFRESHING = 0x003;//正在刷新
    public final static int HEADER_COMPLETED = 0x004;//刷新完成
    protected boolean canTranslation = true;

    public BaseHeaderView(Context context) {
        this(context,null);
    }

    public BaseHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*不停回调BounceLayout的拉动距离*/
    public abstract void handleDrag(float dragY);
    /*下拉头判断是否可以进行刷新加载*/
    public abstract boolean doRefresh();
    /*设置父布局*/
    public abstract void setParent(ViewGroup parent);
    /*手指抬起检查是否需要刷新*/
    public abstract boolean checkRefresh();
    /*刷新完成*/
    public abstract void refreshCompleted();
    /*获得头高度*/
    public abstract int getHeaderHeight();
    /*自动加载*/
    public abstract void autoRefresh();

    public void setCanTranslation(boolean canTranslation) {
        this.canTranslation = canTranslation;
    }
}
