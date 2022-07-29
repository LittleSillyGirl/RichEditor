package com.yuruiyin.richeditor.span;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.yuruiyin.richeditor.callback.OnRichClickListener;
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;
import com.yuruiyin.richeditor.model.InlineSpanBean;
import com.yuruiyin.richeditor.utils.ViewUtil;

/**
 * at用户对应的span
 *
 * @author Admin
 */
public class AtSpan extends ClickableSpan implements IInlineSpan, LongClickableSpan {

    private final InlineSpanBean mInlineSpanBean;

    private OnRichClickListener<AtSpan> mOnRichClickListener;

    public void setOnRichClickListener(OnRichClickListener<AtSpan> onInlineClickListener) {
        this.mOnRichClickListener = onInlineClickListener;
    }

    public AtSpan(InlineSpanBean inlineSpanBean) {
        this.mInlineSpanBean = inlineSpanBean;
    }

    @Override
    public String getType() {
        return mInlineSpanBean == null ? InlineSpanEnum.AT : mInlineSpanBean.getType();
    }

    /**
     * 执行与此范围关联的单击操作。
     *
     * @param widget 点击的控件
     */
    @Override
    public void onClick(@NonNull View widget) {
        //@或者#话题点击事件
        if (mOnRichClickListener != null) {
            mOnRichClickListener.onClick(this);
        }
    }

    @Override
    public boolean isClickable(int touchX, int touchY) {
        return true;
    }

    /**
     * Updates the color of the TextPaint to the foreground color.
     */
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        float textSize = mInlineSpanBean.getTextSize();
        ds.setTextSize(ViewUtil.sp2px(textSize <= 0 ? 15 : textSize));
        ds.setColor(Color.parseColor(mInlineSpanBean.getTextColor()));
        ds.setUnderlineText(TextUtils.equals(getType(), InlineSpanEnum.LINK));
    }

    /**
     * 获取行内标签对象
     *
     * @return
     */
    public InlineSpanBean getInlineSpanBean() {
        return mInlineSpanBean;
    }
}