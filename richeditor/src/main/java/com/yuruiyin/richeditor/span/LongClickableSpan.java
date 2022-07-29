package com.yuruiyin.richeditor.span;

import android.view.View;

/**
 * 支持长按的span,也就是实现点击事件
 *
 * @author admin
 */
public interface LongClickableSpan {

    /**
     * 短按事件
     *
     * @param widget view
     */
    void onClick(View widget);

    /**
     * 是否可以点击
     *
     * @param touchX 触摸的x位置
     * @param touchY 触摸的y位置
     * @return 是否可以点击
     */
    boolean isClickable(int touchX, int touchY);
}
