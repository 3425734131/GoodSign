package com.example.chaomianqiandao.refresh;

import android.view.View;

/**
 * 处理在竖直方向的滑动冲突
 */

public interface BounceHandler {
    boolean canChildPull(View v);//孩子是否可以上拉
    boolean canChildDrag(View v);//孩子是否可以下拉
}
