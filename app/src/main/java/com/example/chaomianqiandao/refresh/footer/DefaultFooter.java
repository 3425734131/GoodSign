package com.example.chaomianqiandao.refresh.footer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chaomianqiandao.R;


/**
 * 默认的加载头
 */

public class DefaultFooter extends BaseFooterView {
    private static final String TAG = "DefaultFooter";
    /*当前状态*/
    private int status;
    /*加载头的高度*/
    private int childHeight;
    /*布局偏移量*/
    private float totalOffset;
    /*设备上下文*/
    private Context mContext;
    /*---------头布局中的view-------------*/
    private ProgressBar pbFooterTip;
    private TextView tvFooterTip;
    private ImageView ivFooterTip;

    public DefaultFooter(@NonNull Context context) {
        this(context,null);
    }

    public DefaultFooter(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DefaultFooter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setBackgroundColor(Color.parseColor("#eeeeee"));
        mContext = context;
        childHeight = mContext.getResources().getDimensionPixelSize(R.dimen.default_footer);
        status = FOOTER_PULL;
        //添加底部内容
        View view = LayoutInflater.from(context).inflate(R.layout.footer_default,this,false);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                ,childHeight);
        addView(view,lp);
        pbFooterTip = (ProgressBar) view.findViewById(R.id.pb_loading);
        ivFooterTip = (ImageView) view.findViewById(R.id.iv_tip);
        tvFooterTip = (TextView) view.findViewById(R.id.tv_tip);
    }

    @Override
    public void handlePull(float dragY) {
        totalOffset = dragY;
        if (canTranslation) {
            setTranslationY(dragY);
        }
        if (status == FOOTER_LOADING) {//只要是正在加载
            return;
        }
        if(dragY >= 0){//回到开始位置
            status = FOOTER_PULL;
            ivFooterTip.setVisibility(GONE);
            pbFooterTip.setVisibility(GONE);
        }
        if (status == FOOTER_PULL) {//开始拖动
            if(dragY <= -childHeight) {//一旦超过刷新头高度
                status = FOOTER_RELEASE;
                ivFooterTip.setVisibility(GONE);
                pbFooterTip.setVisibility(GONE);
            }
        }
        if(status == FOOTER_RELEASE){//还未释放拖拉回去
            if(dragY >= -childHeight) {//一旦低于刷新头高度
                status = FOOTER_PULL;
                ivFooterTip.setVisibility(GONE);
                pbFooterTip.setVisibility(GONE);
            }
        }
    }

    @Override
    public boolean doLoading() {
        if (status == FOOTER_LOADING && totalOffset == -childHeight) {//正在刷新，并且偏移量==刷新头高度
            return true;
        }
        return false;
    }

    @Override
    public void setParent(ViewGroup parent) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                ,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        lp.bottomMargin =  -childHeight;
        parent.addView(this,lp);
    }

    @Override
    public boolean checkLoading() {
        if ((status == FOOTER_RELEASE || status == FOOTER_LOADING) && totalOffset<= -childHeight) {
            status = FOOTER_LOADING;
            ivFooterTip.setVisibility(GONE);
            pbFooterTip.setVisibility(VISIBLE);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void LoadingCompleted() {
        status = FOOTER_COMPLETED;
        ivFooterTip.setBackgroundResource(R.drawable.completed);
        ivFooterTip.setVisibility(VISIBLE);
        pbFooterTip.setVisibility(GONE);
    }

    @Override
    public int getFooterHeight() {
        return childHeight;
    }
}
