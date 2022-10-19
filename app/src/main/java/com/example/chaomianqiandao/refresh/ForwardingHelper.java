package com.example.chaomianqiandao.refresh;

/**
 * 自定义的事件转发判断，大多数用于水平和竖直方向的冲突解决回调
 * @author tangxianqiang
 */

public class ForwardingHelper {
    public static boolean isXMore(float downX, float downY, float moveX, float moveY){
        float distanceX = Math.abs(moveX - downX);
        float distanceY = Math.abs(moveY - downY);
        if (distanceY < distanceX) {
            return true;
        }else {
            return false;
        }
    }
}
