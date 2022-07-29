package com.yuruiyin.richeditor.span;

import android.text.style.StrikethroughSpan;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 行内标签 -> 删除线
 *
 * @author admin
 */
public class CustomStrikeThroughSpan extends StrikethroughSpan implements IInlineSpan {

    @Override
    public String getType() {
        return InlineSpanEnum.STRIKE_THROUGH;
    }
}
