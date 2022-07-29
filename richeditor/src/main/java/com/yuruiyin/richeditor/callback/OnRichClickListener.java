package com.yuruiyin.richeditor.callback;

/**
 * 块级标签：
 * 图片（ImageSpan）短按监听器
 * 譬如若ImageSpan是相册的图片的话，则点击查看大图，
 * 若是自定义view（比如左图右文的链接）的imageSpan，则点击可能是跳转到具体的详情页面
 * <p>
 * 行内标签：
 * 场景，@人，提及游戏，提及话题等，类似发微博
 * 比如点击@的人或者#的话题
 *
 * @author admin
 */
public interface OnRichClickListener<T> {

    /**
     * 块级或者行内标签被点击时回调
     *
     * @param t span对象
     */
    void onClick(T t);

}
