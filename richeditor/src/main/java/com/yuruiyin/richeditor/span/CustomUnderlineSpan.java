package com.yuruiyin.richeditor.span;

import android.text.style.UnderlineSpan;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * * 行内标签 -> 下划线
 *
 * @author admin
 */
public class CustomUnderlineSpan extends UnderlineSpan implements IInlineSpan {

    @Override
    public String getType() {
        return InlineSpanEnum.UNDERLINE;
    }

}
