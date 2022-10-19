package com.example.chaomianqiandao.refresh.header;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.chaomianqiandao.R;


/**
 * 默认刷新头，可以直接复制更改头中视图类容
 */

public class DefaultHeader extends BaseHeaderView {
    private static final String TAG = "DefaultHeader";
    /*刷新头的状态*/
    private int status;
    /*头布局高度*/
    private int childHeight;
    /*布局偏移量*/
    private float totalOffset;
    private Context mContext;
    /*-----------刷新头布局视图内容---------------*/
    private TextView tvHeaderTip;
    private ImageView ivHeaderTip;
    private ProgressBar pbRefreshing;
    public DefaultHeader(Context context) {
        this(context,null);
    }

    public DefaultHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DefaultHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }
    protected  void init(Context context, AttributeSet attrs, int defStyleAttr){
        setBackgroundColor(Color.parseColor("#eeeeee"));
        mContext = context;
        status = HEADER_DRAG;
        childHeight = mContext.getResources().getDimensionPixelSize(R.dimen.default_height);
        //添加头内容
        View view = LayoutInflater.from(context).inflate(R.layout.header_default,this,false);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                ,childHeight);
        addView(view,lp);
        tvHeaderTip =(TextView) view.findViewById(R.id.tv_tip);
        pbRefreshing = (ProgressBar) view.findViewById(R.id.pb_refreshing);
        ivHeaderTip = (ImageView) view.findViewById(R.id.iv_tip);
    }

    /**
     * 将移动的距离传递过来
     */
    @Override
    public void handleDrag(float dragY) {
        this.totalOffset = dragY;
        if (canTranslation) {
            setTranslationY(dragY);
        }
        if (status == HEADER_REFRESHING) {//只要是正在刷新
            return;
        }
        if(dragY <= 0){//回到初始位置
            status = HEADER_DRAG;
            ivHeaderTip.setBackgroundResource(R.drawable.down);
            ivHeaderTip.setVisibility(VISIBLE);
            pbRefreshing.setVisibility(GONE);
        }
        if (status == HEADER_DRAG) {//开始拖动
            if(dragY >= childHeight) {//一旦超过刷新头高度
                status = HEADER_RELEASE;
                ivHeaderTip.setVisibility(VISIBLE);
                startRotateAnimDown();
                pbRefreshing.setVisibility(GONE);
            }
        }
        if(status == HEADER_RELEASE){//还未释放拖拉回去
            if(dragY <= childHeight) {//一旦低于刷新头高度
                status = HEADER_DRAG;
                ivHeaderTip.setVisibility(VISIBLE);
                startRotateAnimUp();
                pbRefreshing.setVisibility(GONE);
            }
        }
    }

    @Override
    public boolean doRefresh() {
        if (status == HEADER_REFRESHING && totalOffset == childHeight) {//正在刷新，并且偏移量==刷新头高度才认为刷新
            return true;
        }
        return false;
    }

    @Override
    public void setParent(ViewGroup parent) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                ,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = - childHeight;
        parent.addView(this,lp);
    }

    @Override
    public boolean checkRefresh() {
        if ((status == HEADER_RELEASE || status == HEADER_REFRESHING) && totalOffset>=childHeight) {
            status = HEADER_REFRESHING;
            ivHeaderTip.clearAnimation();
            ivHeaderTip.setVisibility(GONE);
            pbRefreshing.setVisibility(VISIBLE);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void refreshCompleted() {
        status = HEADER_COMPLETED;
        ivHeaderTip.clearAnimation();
        ivHeaderTip.setBackgroundResource(R.drawable.completed);
        ivHeaderTip.setVisibility(VISIBLE);
        pbRefreshing.setVisibility(GONE);
    }

    @Override
    public int getHeaderHeight() {
        return childHeight;
    }

    @Override
    public void autoRefresh() {
        status = HEADER_REFRESHING;
        ivHeaderTip.clearAnimation();
        ivHeaderTip.setVisibility(GONE);
        pbRefreshing.setVisibility(VISIBLE);
    }

    /**
     * 从0度旋转到180度的动画
     */
    private void startRotateAnimDown(){
        RotateAnimation animation = new RotateAnimation(0
                , 180
                , Animation.RELATIVE_TO_SELF
                , 0.5f
                , Animation.RELATIVE_TO_SELF
                ,0.5f);
        animation.setDuration(120);
        animation.setFillAfter(true);//保持最后的状态
        ivHeaderTip.startAnimation(animation);
    }

    /**
     * 从180度旋转到0度的动画
     */
    private void startRotateAnimUp(){
        RotateAnimation animation = new RotateAnimation(180
                , 0
                , Animation.RELATIVE_TO_SELF
                , 0.5f
                , Animation.RELATIVE_TO_SELF
                ,0.5f);
        animation.setDuration(120);
        animation.setFillAfter(true);//保持最后的状态
        ivHeaderTip.startAnimation(animation);
    }
}
