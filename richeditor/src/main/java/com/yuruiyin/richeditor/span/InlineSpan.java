package com.yuruiyin.richeditor.span;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import android.view.View;

import com.yuruiyin.richeditor.callback.OnRichClickListener;
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;
import com.yuruiyin.richeditor.model.InlineSpanBean;

/**
 * 场景，@人，提及游戏，提及话题等，类似发微博
 * 注意：这个是整体的，到末尾如果换行的话，只能整体换行
 *
 * @author Admin
 */
public class InlineSpan extends ReplacementSpan implements IInlineSpan, LongClickableSpan {

    private static final String TAG = "InlineSpan";

    /**
     * 行内Span对应的实体类
     */
    private final InlineSpanBean mInlineSpanBean;

    private OnRichClickListener<InlineSpan> mOnRichClickListener;

    public void setOnRichClickListener(OnRichClickListener<InlineSpan> onInlineClickListener) {
        this.mOnRichClickListener = onInlineClickListener;
    }

    public InlineSpan(InlineSpanBean inlineSpanBean) {
        this.mInlineSpanBean = inlineSpanBean;
    }

//    @Override
//    public int getSize(Paint paint, CharSequence text, int start, int end,
//                       FontMetricsInt fm) {
////        paint.setTextSize(mInlineImageSpanVm.getTextSize());
//        paint.setAntiAlias(true);
//        paint.setTextAlign(Paint.Align.CENTER);
//        return (int) paint.measureText(text, start, end);
//    }
//
//    private static final String TAG = "InlineImageSpan";
//
//    @Override
//    public void draw(Canvas canvas, CharSequence text, int start, int end,
//                     float x, int top, int y, int bottom, Paint paint) {
//        paint.setColor(Color.RED);
//        float width = paint.measureText(text, start, end);
//        canvas.drawRect(x, top, x + width, bottom, paint);
//        paint.setColor(0xFF000000 | mInlineImageSpanVm.getTextColor());
//        canvas.drawText(text, start, end, x, (float) y, paint);
//    }

    /**
     * 短按事件
     *
     * @param widget view
     */
    @Override
    public void onClick(View widget) {
        if (mOnRichClickListener != null) {
            mOnRichClickListener.onClick(this);
        }
    }

    /**
     * 是否可以点击
     *
     * @param touchX 触摸的x位置
     * @param touchY 触摸的y位置
     * @return 是否可以点击
     */
    @Override
    public boolean isClickable(int touchX, int touchY) {
        return true;
    }

    public InlineSpanBean getInlineSpanBean() {
        return mInlineSpanBean;
    }

    @Override
    public String getType() {
        return InlineSpanEnum.IMAGE;
    }


//    private Context mContext;
//    public OfficialColorSpan(Context context, String text) {
//        this(context, R.color.rich_editor_at_text_color, text, 13);
//    }
//
//    public OfficialColorSpan(Context context, int textColor, String text, float textSize) {
//        if (TextUtils.isEmpty(text)) {
//            return;
//        }
//        this.mText = text;
//        this.mContext = context;
//        this.mTextColor = ContextCompat.getColor(context, textColor);
//        this.mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, mContext.getResources().getDisplayMetrics());
//    }

    /**
     * 设置宽度，这里我们只用计算文字的宽度
     */
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
//        paint.setTextSize(mTextSize);
        String content = mInlineSpanBean.getValue();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        return (int) paint.measureText(content, 0, content.length());
    }

    /**
     * draw
     *
     * @param text   完整文本
     * @param start  setSpan里设置的start
     * @param end    setSpan里设置的start
     * @param x      文字绘制的中心位置
     * @param top    当前span所在行的上方y
     * @param y      y其实就是metric里baseline的位置
     * @param bottom 当前span所在行的下方y(包含了行间距)，会和下一行的top重合
     * @param paint  使用此span的画笔
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
//        paint.setTextSize(mTextSize);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
//        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//        float textRectHeight = fontMetrics.bottom - fontMetrics.top;
//        //文字的高度
//        float textHeight = fontMetrics.descent - fontMetrics.ascent;
//        Log.i(TAG, "draw: textRectHeight==" + textRectHeight);
//        Log.i(TAG, "draw: textHeight==" + textHeight);
//        Log.i(TAG, "draw: y==" + y);
        String content = mInlineSpanBean.getValue();
        //适当设置间距
        final float textWidth = paint.measureText(content);
        paint.setColor(0xFF000000 | Color.parseColor(mInlineSpanBean.getTextColor()));
//        paint.setColor(mTextColor);
        canvas.drawText(content, x + textWidth / 2, y, paint);
    }

}
