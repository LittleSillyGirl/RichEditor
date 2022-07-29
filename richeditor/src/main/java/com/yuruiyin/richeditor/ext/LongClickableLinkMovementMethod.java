package com.yuruiyin.richeditor.ext;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

import com.yuruiyin.richeditor.R;
import com.yuruiyin.richeditor.model.SpanTouchBean;
import com.yuruiyin.richeditor.span.LongClickableSpan;

/**
 * 支持长按的LinkMovementMethod
 * Description: 譬如当ImageSpan支持响应点击和长按事件
 *
 * @author admin
 */
public class LongClickableLinkMovementMethod extends LinkMovementMethod {

    private LongClickableSpan mPressedSpan;

    private void safeRemoveSpan(Spannable spannable) {
        if (!spannable.toString().isEmpty()) {
            Selection.removeSelection(spannable);
        }
    }

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPressedSpan = getPressedSpan(textView, spannable, event);
                break;
            case MotionEvent.ACTION_MOVE:
                LongClickableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan = null;
                    safeRemoveSpan(spannable);
                }
                break;
            default:
                if (mPressedSpan != null && MotionEvent.ACTION_UP == action) {
                    SpanTouchBean spanTouchBean = new SpanTouchBean();
                    spanTouchBean.setX(event.getX());
                    spanTouchBean.setY(event.getY());
                    textView.setTag(R.id.ivImageDel, spanTouchBean);
                    mPressedSpan.onClick(textView);
                }
                mPressedSpan = null;
                break;
        }
        return true;
        //如果返回super的方法，会触发本身的点击事件，那么就会出现点击背景色，所以我们直接返回true，消费掉该点击事件
//        return super.onTouchEvent(textView, spannable, event);

//        if (action == MotionEvent.ACTION_UP ||
//                action == MotionEvent.ACTION_DOWN) {
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//
//            x -= textView.getTotalPaddingLeft();
//            y -= textView.getTotalPaddingTop();
//
//            x += textView.getScrollX();
//            y += textView.getScrollY();
//
//            Layout layout = textView.getLayout();
//            int line = layout.getLineForVertical(y);
//            int off = layout.getOffsetForHorizontal(line, x);
//
//            LongClickableSpan[] longClickableSpans = spannable.getSpans(off, off, LongClickableSpan.class);
//            Log.i(TAG, "onTouchEvent: longClickableSpans==" + longClickableSpans.length);
//            if (longClickableSpans.length != 0) {
//                if (action == MotionEvent.ACTION_UP) {
//                    if (positionWithinTag(off, spannable, longClickableSpans[0]) && longClickableSpans[0].isClickable(x, y)) {
//                        LongClickableSpan touchedSpan = longClickableSpans[0];
//                        SpanTouchBean spanTouchBean = new SpanTouchBean();
//                        spanTouchBean.setX(event.getX());
//                        spanTouchBean.setY(event.getY());
//                        textView.setTag(R.id.ivImageDel, spanTouchBean);
//                        touchedSpan.onClick(textView);
//                    }
////                    SpanTouchBean spanTouchBean = new SpanTouchBean();
////                    spanTouchBean.setX(event.getX());
////                    spanTouchBean.setY(event.getY());
////                    textView.setTag(R.id.ivImageDel, spanTouchBean);
////                    longClickableSpans[0].onClick(textView);
//                } else {
//                    Selection.setSelection(spannable,
//                            spannable.getSpanStart(longClickableSpans[0]),
//                            spannable.getSpanEnd(longClickableSpans[0]));
//                }
//                return true;
//            }
//        }
//
//        return Touch.onTouchEvent(textView, spannable, event);

    }
//
//    @Override
//    public boolean canSelectArbitrarily() {
//        return true;
//    }
//
//    @Override
//    public void initialize(TextView widget, Spannable text) {
//        Selection.setSelection(text, text.length());
//    }
//
//    @Override
//    public void onTakeFocus(TextView view, Spannable text, int dir) {
//        if ((dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0) {
//            if (view.getLayout() == null) {
//                // This shouldn't be null, but do something sensible if it is.
//                Selection.setSelection(text, text.length());
//            }
//        } else {
//            Selection.setSelection(text, text.length());
//        }
//    }


    /**
     * 获取点击的
     *
     * @param textView  文本控件
     * @param spannable
     * @param event
     * @return 返回点击的span
     */
    private LongClickableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX() - textView.getTotalPaddingLeft() + textView.getScrollX();
        int y = (int) event.getY() - textView.getTotalPaddingTop() + textView.getScrollY();

        Layout layout = textView.getLayout();
        int position = layout.getOffsetForHorizontal(layout.getLineForVertical(y), x);

        LongClickableSpan[] longClickableSpans = spannable.getSpans(position, position, LongClickableSpan.class);
        LongClickableSpan touchedSpan = null;
        if (longClickableSpans.length > 0 && positionWithinTag(position, spannable, longClickableSpans[0])
                && longClickableSpans[0].isClickable(x, y)) {
            touchedSpan = longClickableSpans[0];
        }
        return touchedSpan;
    }

    /**
     * span是否在范围内
     *
     * @param position
     * @param spannable
     * @param tag
     * @return
     */
    private boolean positionWithinTag(int position, Spannable spannable, Object tag) {
        return position >= spannable.getSpanStart(tag) && position <= spannable.getSpanEnd(tag);
    }
}
