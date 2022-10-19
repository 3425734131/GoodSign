package com.example.chaomianqiandao.refresh.footer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 加载更多的基类，继承该类可以实现不同样式的加载头，但是基本的加载过程不变，都是：
 *      上拉--释放--加载（悬停）--回弹结束
 * 加载更多的布局，一共有4种状态：
 *      a、加载更多
 *      b、释放加载更多
 *      c、正在加载
 *      d、加载完成
 */
public abstract class BaseFooterView extends FrameLayout{
    public final static int FOOTER_PULL = 0x005;//加载更多
    public final static int FOOTER_RELEASE = 0x006;//释放加载更多
    public final static int FOOTER_LOADING = 0x007;//正在加载
    public final static int FOOTER_COMPLETED = 0x008;//加载完成
    protected boolean canTranslation = true;

    public BaseFooterView(@NonNull Context context) {
        this(context,null);
    }

    public BaseFooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseFooterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*不停回调BounceLayout的拉动距离*/
    public abstract void handlePull(float dragY);
    /*加载头判断是否可以进行加载更多*/
    public abstract boolean doLoading();
    /*设置父布局*/
    public abstract void setParent(ViewGroup parent);
    /*手指抬起检查是否需要加载更多*/
    public abstract boolean checkLoading();
    /*加载完成*/
    public abstract void LoadingCompleted();
    /*获得底部高度*/
    public abstract int getFooterHeight();
    public void setCanTranslation(boolean canTranslation) {
        this.canTranslation = canTranslation;
    }
}
