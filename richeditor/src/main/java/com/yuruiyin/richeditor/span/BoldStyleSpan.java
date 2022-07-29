package com.yuruiyin.richeditor.span;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 行内标签 -> 加粗
 *
 * @author admin
 */
public class BoldStyleSpan extends StyleSpan implements IInlineSpan {

    public BoldStyleSpan() {
        super(Typeface.BOLD);
    }

    @Override
    public String getType() {
        return InlineSpanEnum.BOLD;
    }

}
