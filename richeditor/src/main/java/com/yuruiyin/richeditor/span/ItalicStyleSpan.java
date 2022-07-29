package com.yuruiyin.richeditor.span;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 行内标签 -> 斜体
 *
 * @author admin
 */
public class ItalicStyleSpan extends StyleSpan implements IInlineSpan {

    public ItalicStyleSpan() {
        super(Typeface.ITALIC);
    }

    @Override
    public String getType() {
        return InlineSpanEnum.ITALIC;
    }
}
