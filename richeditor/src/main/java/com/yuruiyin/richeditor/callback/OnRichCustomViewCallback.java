package com.yuruiyin.richeditor.callback;

import android.view.View;

import com.yuruiyin.richeditor.model.RichBlockBean;

/**
 * 自定义view的回调处理
 *
 * @author admin
 */
public interface OnRichCustomViewCallback {

    /**
     * 回调处理自定义view
     *
     * @param richBlockBean richBlockBean对象
     * @return 返回自定义的view
     */
    View onCallback(RichBlockBean richBlockBean);

}
