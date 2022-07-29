package com.yuruiyin.richeditor.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 居中ImageSpan，同时可以实现ImageSpan与文本垂直居中
 *
 * @author admin
 */
public class CenterImageSpan extends ImageSpan {

    /**
     * 缓存的图片drawable 对应draw操作的
     */
    private WeakReference<Drawable> mDrawCacheDrawable;

    public CenterImageSpan(Context context, int resourceId) {
        super(context, resourceId, ALIGN_BASELINE);
    }

    public CenterImageSpan(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }

    public CenterImageSpan(Drawable drawable) {
        super(drawable);
    }

    public CenterImageSpan(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getCachedDrawable();
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int drHeight = rect.bottom - rect.top;
            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = centerY + drHeight / 2;
            fontMetricsInt.descent = fontMetricsInt.bottom;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
                     int bottom, Paint paint) {
        Drawable drawable = getCachedDrawable();
        canvas.save();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.descent - fmPaint.ascent;
        int centerY = y + fmPaint.descent - fontHeight / 2;
        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 为了防止多次创建drawable，我们缓存一份drawable
     *
     * @return drawable 对象
     */
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawCacheDrawable;
        Drawable d = null;
        if (wr != null) {
            d = wr.get();
        }

        if (d == null) {
            d = getDrawable();
            mDrawCacheDrawable = new WeakReference<>(d);
        }
        return d;
    }

}
