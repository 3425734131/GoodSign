package com.example.chaomianqiandao.refresh;

/**
 * 第一层的事件转发判断，用户希望在转发前做一些自己的判断处理，比如x方向的变化大于y方向，认为需要转发，otherwise；
 * @see #notForwarding(float, float, float, float) 返回true不一定意味着子view得不到事件
 */
public interface EventForwardingHelper {
    //返回true表示不转发到孩子
    boolean notForwarding(float downX,float downY,float moveX,float moveY);
}
