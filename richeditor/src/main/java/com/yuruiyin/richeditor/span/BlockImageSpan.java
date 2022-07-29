package com.yuruiyin.richeditor.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yuruiyin.richeditor.R;
import com.yuruiyin.richeditor.callback.OnRichClickListener;
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.model.BlockSpanBean;
import com.yuruiyin.richeditor.model.SpanTouchBean;
import com.yuruiyin.richeditor.utils.ViewUtil;

import java.lang.ref.WeakReference;

/**
 * 段落ImageSpan, 比如图片、视频封面、以及自定义布局等，而且支持响应短按和长按事件
 *
 * @author admin
 */
public class BlockImageSpan extends CenterImageSpan implements LongClickableSpan {

    /**
     * 缓存的图片drawable 对应draw操作的
     */
    private WeakReference<Drawable> mCacheDrawable;

    /**
     * 手指触摸到图片左侧之后，当成光标移动到左侧，不响应图片点击事件
     */
    private static final int TOUCH_OFFSET_X = 45;

    private float x;
    private float y;

    private BlockSpanBean mBlockImageSpanBean;
    private SpanTouchBean mSpanTouchBean;

    private OnRichClickListener<BlockImageSpan> mOnRichClickListener;

    public void setOnRichClickListener(OnRichClickListener<BlockImageSpan> onInlineClickListener) {
        this.mOnRichClickListener = onInlineClickListener;
    }

    public BlockImageSpan(Context context, int resourceId, @NonNull BlockSpanBean blockImageSpanBean) {
        super(context, resourceId);
        initData(blockImageSpanBean);
    }

    public BlockImageSpan(Context context, Bitmap bitmap, @NonNull BlockSpanBean blockImageSpanBean) {
        super(context, bitmap);
        initData(blockImageSpanBean);
    }

    public BlockImageSpan(Drawable drawable, @NonNull BlockSpanBean blockImageSpanBean) {
        super(drawable);
        initData(blockImageSpanBean);
    }

    public BlockImageSpan(Context context, Uri uri, @NonNull BlockSpanBean blockImageSpanBean) {
        super(context, uri);
        initData(blockImageSpanBean);
    }

    private void initData(@NonNull BlockSpanBean blockImageSpanBean) {
        this.mBlockImageSpanBean = blockImageSpanBean;
    }

    public BlockSpanBean getBlockSpanObtainObject() {
        return mBlockImageSpanBean;
    }

    public void setBlockSpanObtainObject(BlockSpanBean blockImageSpanBean) {
        this.mBlockImageSpanBean = blockImageSpanBean;
    }

    @Override
    public void onClick(View widget) {
        Object object = widget.getTag(R.id.ivImageDel);
        if (object != null) {
            if (object instanceof SpanTouchBean) {
                mSpanTouchBean = (SpanTouchBean) object;
                if (mBlockImageSpanBean.isShowDel()) {
                    //判断删除按钮的位置
                    if (widget instanceof TextView) {
                        TextView textView = (TextView) widget;
                        Rect rect = mCacheDrawable.get().getBounds();
                        int touchX = (int) (mSpanTouchBean.getX() - textView.getTotalPaddingLeft() + textView.getScrollX());
                        int touchY = (int) (mSpanTouchBean.getY() - textView.getTotalPaddingTop() + textView.getScrollY());
                        int delImageWidth = ViewUtil.dp2px(textView.getContext().getResources().getDimension(R.dimen.rich_editor_image_del_width));
                        if (touchX <= (rect.right + x) && touchX >= (rect.right + x - delImageWidth) && touchY <= (rect.top + y + delImageWidth) && touchY >= (rect.top + y)) {
                            //删除本身的span
                            mSpanTouchBean.setType(R.id.ivImageDel);
                            if (mOnRichClickListener != null) {
                                mOnRichClickListener.onClick(this);
                            }
                            return;
                        }
                    }
                }
                if (TextUtils.equals(mBlockImageSpanBean.getType(), BlockSpanEnum.VIDEO)) {
                    //判断播放按钮的位置
                    mSpanTouchBean.setType(R.id.ivVideoIcon);
                    if (mOnRichClickListener != null) {
                        mOnRichClickListener.onClick(this);
                    }
                    return;
                }
            }
        }
        if (mOnRichClickListener != null) {
            mOnRichClickListener.onClick(this);
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        this.x = x;
        this.y = top;
    }

    public SpanTouchBean getSpanTouchBean() {
        return mSpanTouchBean;
    }

    @Override
    public boolean isClickable(int touchX, int touchY) {
        if (mCacheDrawable.get() != null) {
            Rect rect = mCacheDrawable.get().getBounds();
            return touchX <= rect.right + x && touchX >= rect.left + x + TOUCH_OFFSET_X
                    && touchY <= rect.bottom + y && touchY >= rect.top + y;
        }
        return false;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = super.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        int maxWidth = mBlockImageSpanBean.getMaxWidth();
        // 防止drawable宽度过大，超过编辑器的宽度
        if (width > maxWidth) {
            float scale = ((float) maxWidth / width);
            drawable.setBounds(0, 0, maxWidth, (int) (height * scale));
        }
        //为了防止多次创建
        if (mCacheDrawable == null) {
            mCacheDrawable = new WeakReference<>(drawable);
        }
        return mCacheDrawable.get();
    }

}