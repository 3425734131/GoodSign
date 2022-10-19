package com.example.chaomianqiandao.refresh;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * 自定义弹性处理类，用于控制垂直滚动的子view和BounceLayout的冲突
 */
public class NormalBounceHandler implements BounceHandler {

    @Override
    public boolean canChildPull(View v) {
        return !canScrollDown(v);//意味着不能向下滚动
    }


    @Override
    public boolean canChildDrag(View v) {
       return !canScrollUP(v);//意味着不能向上滚动
    }

    /**
     * 是否还可以在竖直方向向上滚动
     * @param v
     * @return false不可以滚动，那么就可以下拉了
     */
    private boolean canScrollUP(View v){
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (v instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) v;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return v.getScrollY() > 0;
            }
        } else {
            return v.canScrollVertically(-1);//true 表示还可以在竖直方向上拉
        }
    }

    /**
     * 认为还可以下滑
     * @param v
     * @return
     */
    private boolean canScrollDown(View v){
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (v instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) v;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else if (v instanceof ScrollView) {
                ScrollView scrollView = (ScrollView) v;
                if (scrollView.getChildCount() == 0) {
                    return false;
                } else {
                    return scrollView.getScrollY() < scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
                }
            } else {
                return false;
            }
        } else {
            return v.canScrollVertically(1);//true表示竖直方向还可以滚动
        }
    }
}
