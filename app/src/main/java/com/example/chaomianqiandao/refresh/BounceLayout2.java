package com.example.chaomianqiandao.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *继承于帧布局，下拉刷新、上拉加载更多的容器；
 * 功能：
 *      1、使布局上拉下拉具有回弹效果；
 *      2、可以只有上拉刷新；
 *      3、能够auto刷新（界面进入就刷新）；
 *      4、能够快速自定义刷新头，刷新效果；
 *      5、刷新开始、刷新结束回调；
 *      6、加载更多开始、结束回调；
 *      7、必须要有实际宽高才能激活下拉刷新和加载更多；
 *      8、能够结合RecyclerView、ListView、ScrollerView、WebView等相关变化的View，能够同时结合列表+ViewPager+侧拉删除的结构，能够结合侧拉；
 *      9、能够管理多指触控；
 *      10、强烈的阻尼效果
 * @author tangxianqiang
 * @deprecated
 */

public class BounceLayout2 extends FrameLayout {

    private static final String TAG = "BounceLayout";
    /*滚动实例*/
    private Scroller mScroller;
    /*设备滚动间隙最小值*/
    private int mTouchSlop;
    /*手指按下的y位置*/
    private float mYDown;
    /*手机上一次移动的y轴位置,也是在拦截事件前最后的y*/
    private float mYLastMove;
    /*上次移动的x值*/
    private float mXDown;
    /*手指在不断移动时的y轴的实时坐标，不管任何手指*/
    private float mYMove;
    /*多点触控的时候，记录总的偏移量*/
    private float totalOffsetY;
    /*移动进行时，此时对应的手指*/
    private int currentPointer;
    /*移动进行时，处理多指对应的y*/
    private float currentY;
    /*手指index变化*/
    private boolean pointerChange;
    /*保证随时按下都可以开始滑动*/
    private boolean forceDrag;
    /*布局的高度*/
    private float height;
    /*阻尼系数*/
    private float dampingCoefficient = 2.5f;
    /*是否允许下拉*/
    private boolean canPull;
    /*是否允许上拉*/
    private boolean canDrag;
    /*控制内容上拉和下拉的处理者，可以自己定义*/
    private BounceHandler bounceHandler;
    /*滚动的孩子*/
    private View childContent;


    public BounceLayout2(@NonNull Context context) {
        this(context, null);
    }

    public BounceLayout2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BounceLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context,  AttributeSet attrs, int defStyleAttr){
        //初始化滚动实例
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        // 获取TouchSlop值
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    /**
     * onInterceptTouchEvent方法默认并不会拦截并不会拦击子view的事件，但是我们要在这个方法里面
     * 写逻辑，告诉Bouncelayout什么时候、什么条件进行拦截，并且拦截之后交给自己的onTouchEvent进行消费，还有如何消费
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (forwardingHelper == null || bounceHandler == null
                || childContent == null) {//三者为空，直接认为不拦截，导致有冲突地方将不会出现刷新头
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN://动作按下,只是第一根手指才会触发
                currentPointer = 0;
                mYDown = ev.getY();// getRawY表示相对屏幕的位置，也就是绝对位置 和 getY表示相对父亲的位置
                mXDown = ev.getX();
                mYLastMove = ev.getY();//这样做是为了初始化 mYLastMove
                currentY = mYDown;
                break;
            case MotionEvent.ACTION_POINTER_DOWN://只有第一根以上的手指才会触发，第一根手指不会触发
                break;
            case MotionEvent.ACTION_POINTER_UP://只有第一根以上的手指才会触发，第一根手指不会触发
                break;
            case MotionEvent.ACTION_MOVE://任何手指都会触发
                mYLastMove = ev.getY();//每一次用了mYMove都保存起来
                if(mYDown < ev.getY()){//下拉
                    return (forwardingHelper.notForwarding(mXDown,mYDown,ev.getX(),ev.getY())
                            && bounceHandler.canChildDrag(childContent));
                }else{
                    return ( forwardingHelper.notForwarding(mXDown,mYDown,ev.getX(),ev.getY())
                            && bounceHandler.canChildPull(childContent));
                }
            case MotionEvent.ACTION_UP:
                forceDrag = false;
                break;

        }
        return super.onInterceptTouchEvent(ev);//默认是super
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bounceHandler == null
                || childContent == null) {//三者为空，直接认为不拦截，导致有冲突地方将不会出现刷新头
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN://只有第一根以上的手指才会触发，第一根手指不会触发
                currentPointer = event.getActionIndex();
                currentY = event.getY(currentPointer);
                pointerChange = true;
                break;
            case MotionEvent.ACTION_POINTER_UP://只有第一根以上的手指才会触发，第一根手指不会触发，getPointerCount依然是之前的值，尽管松开了
                pointerChange = true;
                if (event.getPointerCount() == 2) {//说明即将只有一根手指了，事件以第一根手指为准
                    currentPointer = 0;
                    currentY = mYLastMove;
                }else{
                    if (currentPointer == event.getActionIndex()) {//离开的是最近的手指，事件以第一根手指为准
                        currentPointer = 0;
                        currentY = mYLastMove;
                    }else{//事件以最后一根手指为准
                        currentPointer = event.getPointerCount()-1-1;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE://任何一根手指的移动只做累加,累加最后一次按下那根手指的移动距离
                forceDrag = true;
                mYMove = event.getY(currentPointer);
                if (pointerChange) {
                    currentY = mYMove;
                }
                float scrollY = mYMove - currentY;
                float p = Math.abs(totalOffsetY / height);
                if (p == 1) {
                    p = 1 - Integer.MIN_VALUE;
                }
                scrollY = scrollY / (dampingCoefficient * (1.0f/(1 - p)));
                totalOffsetY += scrollY;
                scrollTo(0, (int) -totalOffsetY);
                currentY = mYMove;
                pointerChange = false;
                break;
            case MotionEvent.ACTION_UP:
                forceDrag = false;
                mScroller.startScroll(0,getScrollY(),0,-getScrollY(),500);
                invalidate();
                break;

        }
        return super.onTouchEvent(event);

    }



    @Override

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setClickable(true);
        for (int i = 0; i < getChildCount(); i++) {
            if (!getChildAt(i).isClickable()) {
                getChildAt(i).setClickable(true);
            }
        }
        height = h;
    }


    @Override
    public void computeScroll() {
        if (forceDrag) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            totalOffsetY = -mScroller.getCurrY();
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    public void setBounceHandler(BounceHandler bounceHandler,View v) {
        this.bounceHandler = bounceHandler;
        this.childContent = v;
    }

    public void setCanDrag(boolean canDrag) {
        this.canDrag = canDrag;
        this.canPull = false;
    }

    public void setCanPull(boolean canPull) {
        this.canPull = canPull;
        this.canDrag = false;
    }

    public void setEventForwardingHelper(EventForwardingHelper forwardingHelper){
        this.forwardingHelper = forwardingHelper;
    }
    private EventForwardingHelper forwardingHelper;
}