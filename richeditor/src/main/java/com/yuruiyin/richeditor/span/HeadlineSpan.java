package com.yuruiyin.richeditor.span;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;

import androidx.annotation.NonNull;

import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;

/**
 * 文章标题span
 * 利用加粗修改字体大小来实现
 *
 * @author admin
 */
public class HeadlineSpan extends AbsoluteSizeSpan implements IBlockSpan {

    public HeadlineSpan(int textSize) {
        super(textSize);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    }

    @SuppressLint("WrongConstant")
    @Override
    public String getType() {
        return BlockSpanEnum.HEADLINE;
    }

}
