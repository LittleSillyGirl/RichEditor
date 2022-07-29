package com.yuruiyin.richeditor;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.makeramen.roundedimageview.RoundedImageView;
import com.yuruiyin.richeditor.callback.OnRichClickListener;
import com.yuruiyin.richeditor.callback.OnRichCustomViewCallback;
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.enumtype.ImageTypeMarkEnum;
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;
import com.yuruiyin.richeditor.ext.LongClickableLinkMovementMethod;
import com.yuruiyin.richeditor.model.BlockSpanBean;
import com.yuruiyin.richeditor.model.InlineSpanBean;
import com.yuruiyin.richeditor.model.RichBlockBean;
import com.yuruiyin.richeditor.span.AtSpan;
import com.yuruiyin.richeditor.span.BlockImageSpan;
import com.yuruiyin.richeditor.span.BoldStyleSpan;
import com.yuruiyin.richeditor.span.CustomQuoteSpan;
import com.yuruiyin.richeditor.span.CustomStrikeThroughSpan;
import com.yuruiyin.richeditor.span.CustomUnderlineSpan;
import com.yuruiyin.richeditor.span.HeadlineSpan;
import com.yuruiyin.richeditor.span.IBlockSpan;
import com.yuruiyin.richeditor.span.IInlineSpan;
import com.yuruiyin.richeditor.span.ItalicStyleSpan;
import com.yuruiyin.richeditor.utils.BitmapUtil;
import com.yuruiyin.richeditor.utils.LogUtil;
import com.yuruiyin.richeditor.utils.ViewUtil;
import com.yuruiyin.richeditor.utils.WindowUtil;

import java.util.List;

import static com.yuruiyin.richeditor.config.AppConfig.IMAGE_SPAN_PLACEHOLDER;

/**
 * 富文本的预览功能
 *
 * @author admin
 */
public class RichTextView extends AppCompatTextView {

    private final String TAG = "RichTextView";

    /**
     * 宽度撑满编辑区的ImageSpan需要减去的一个值，为了防止ImageSpan碰到边界导致的重复绘制的问题
     */
    private final int IMAGE_SPAN_MINUS_VALUE = 6;

    /**
     * 是否显示视频标识
     */
    private boolean mIsShowVideoMark;
    /**
     * 视频标识图标资源id
     */
    private int mVideoMarkResourceId;

    /**
     * 是否显示gif标识
     */
    private boolean mIsShowGifMark;
    /**
     * 是否显示长图标识
     */
    private boolean mIsShowLongImageMark;

    /**
     * 图片和视频封面的圆角大小
     */
    private int mImageRadius;

    /**
     * 屏幕宽度
     */
    private int mScreenWidth;

    /**
     * 标题字体大小
     */
    private int mHeadlineTextSize;

    private Activity mActivity;

    public RichTextView(Context context) {
        super(context);
        init(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private OnRichClickListener mOnRichClickListener;

    public void setOnRichClickListener(OnRichClickListener onRichClickListener) {
        this.mOnRichClickListener = onRichClickListener;
    }

    /**
     * 初始化属性
     *
     * @param context 上下文生命周期
     * @param attrs   属性
     */
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextView);
            //是否显示视频标识图标
            mIsShowVideoMark = ta.getBoolean(R.styleable.RichTextView_text_show_video_mark, true);
            //视频图标资源id
            mVideoMarkResourceId = ta.getResourceId(R.styleable.RichTextView_text_video_mark_resource_id, R.drawable.default_video_icon);
            //是否显示gif标识图标
            mIsShowGifMark = ta.getBoolean(R.styleable.RichTextView_text_show_gif_mark, true);
            //是否显示长图标识
            mIsShowLongImageMark = ta.getBoolean(R.styleable.RichTextView_text_show_long_image_mark, true);

            //是否显示长图标识
            mImageRadius = (int) ta.getDimension(R.styleable.RichTextView_text_image_radius, 0);

            float defHeadlineTextSize = context.getResources().getDimension(R.dimen.rich_editor_headline_text_size);
            //标题字体大小
            mHeadlineTextSize = (int) ta.getDimension(R.styleable.RichTextView_text_headline_text_size, defHeadlineTextSize);

            ta.recycle();
        }

        mActivity = (Activity) context;
        if (mActivity == null) {
            LogUtil.e(TAG, "activity is null");
            return;
        }
        //富文本的点击事件
        setMovementMethod(new LongClickableLinkMovementMethod());
        //屏幕的宽度
        mScreenWidth = WindowUtil.getScreenSize(mActivity)[0];
    }

    /**
     * 获取不含padding的控件宽度
     *
     * @return 控件宽度
     */
    private int getWidthWithoutPadding() {
        int textViewMeasureWidth = getMeasuredWidth();
        if (textViewMeasureWidth <= 0) {
            // 可能是编辑器还不可见, 则直接设置编辑器的宽度为屏幕的宽度
            textViewMeasureWidth = mScreenWidth;
        }
        return textViewMeasureWidth - getPaddingLeft() - getPaddingRight() - IMAGE_SPAN_MINUS_VALUE;
    }

    /**
     * 预览内容
     *
     * @param richBlockBeanList 富文本内容对象猎豹
     */
    public void showContent(List<RichBlockBean> richBlockBeanList, OnRichCustomViewCallback onRichCustomViewCallback) {
        //预览内容
        for (RichBlockBean richBlockBean : richBlockBeanList) {
            if (richBlockBean == null) {
                continue;
            }
            removeSelectedContent();
            switch (richBlockBean.getBlockType()) {
                default:
                case BlockSpanEnum.NORMAL_TEXT:
                case BlockSpanEnum.HEADLINE:
                case BlockSpanEnum.QUOTE:
                    insertBlockText(richBlockBean);
                    break;
                case BlockSpanEnum.IMAGE:
                case BlockSpanEnum.VIDEO:
                    if (richBlockBean.getBlockSpan() == null) {
                        continue;
                    }
                    //加载图片
                    insertBlockImage(richBlockBean);
                    break;
                case BlockSpanEnum.DIVIDER:
                    if (richBlockBean.getBlockSpan() == null) {
                        continue;
                    }
                    //加载图片
                    insertDivider(R.drawable.image_divider_line, richBlockBean);
                    break;
                case BlockSpanEnum.GAME:
                    BlockSpanBean beanBlockSpan = richBlockBean.getBlockSpan();
                    if (beanBlockSpan == null) {
                        continue;
                    }
                    int itemWidth = getWidthWithoutPadding();
                    beanBlockSpan.setMaxWidth(itemWidth);
                    if (beanBlockSpan.getWidth() <= 0) {
                        beanBlockSpan.setWidth(itemWidth);
                    }
                    int itemHeight = ViewUtil.dp2px(1000);
                    if (beanBlockSpan.getHeight() <= 0) {
                        beanBlockSpan.setHeight(itemHeight);
                    }
                    if (beanBlockSpan.getMaxHeight() <= 0) {
                        beanBlockSpan.setMaxHeight(itemHeight);
                    }
                    View customView = onRichCustomViewCallback.onCallback(richBlockBean);
                    ViewUtil.layoutView(customView,
                            beanBlockSpan.getWidth(),
                            beanBlockSpan.getHeight());
                    insertBlockImage(BitmapUtil.getBitmap(customView), new SpannableString(IMAGE_SPAN_PLACEHOLDER), beanBlockSpan);
                    break;
            }
        }
    }

    /**
     * 插入整段文本(可能是普通文本、段落样式文本（标题或引用）)
     * 使用场景：如恢复草稿
     *
     * @param richBlockBean 段落的文本内容
     */
    public void insertBlockText(RichBlockBean richBlockBean) {
        //应为块级也就是段落的因素，我们需要主动加入换行符
        SpannableString spanStringContent = new SpannableString(richBlockBean.getText());
        String blockType = richBlockBean.getBlockType();
        switch (blockType) {
            default:
            case BlockSpanEnum.NORMAL_TEXT:
                break;
            case BlockSpanEnum.HEADLINE:
            case BlockSpanEnum.QUOTE:
                IBlockSpan blockSpan = getBlockSpan(blockType);
                spanStringContent.setSpan(blockSpan, 0, spanStringContent.length(), getBlockSpanFlag(blockType));
                break;
        }
        insertNormalTextBlock(richBlockBean, spanStringContent);
        append(spanStringContent);
        append("\n");
    }

    private void insertNormalTextBlock(RichBlockBean richBlockBean, SpannableString spanStringContent) {
        //遍历设置行内样式
        List<RichBlockBean.InlineStyleEntity> inlineStyleEntityList = richBlockBean.getInlineStyleEntityList();
        if (inlineStyleEntityList != null && inlineStyleEntityList.size() > 0) {
            //@或者#之前的span，我们需要在@或者#的时候把之前的样式清除掉
            for (RichBlockBean.InlineStyleEntity inlineStyleEntity : inlineStyleEntityList) {
                String inlineType = inlineStyleEntity.getInlineType();
                int offset = inlineStyleEntity.getOffset();
                int length = inlineStyleEntity.getLength();
                if (InlineSpanEnum.AT.equals(inlineType) || InlineSpanEnum.TOPIC.equals(inlineType) || InlineSpanEnum.LINK.equals(inlineType)) {
                    // @用户或者#话题
                    if (inlineStyleEntity.getInlineSpan() == null) {
                        continue;
                    }
                    InlineSpanBean inlineSpanBean = inlineStyleEntity.getInlineSpan();
                    AtSpan atSpan = new AtSpan(inlineSpanBean);
                    if (mOnRichClickListener != null) {
                        atSpan.setOnRichClickListener(mOnRichClickListener);
                    }
                    spanStringContent.setSpan(atSpan, offset, offset + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    continue;
                }
                //行内样式对应的span对象，比如加粗、斜体等
                IInlineSpan inlineSpan = getInlineStyleSpan(inlineType);
                spanStringContent.setSpan(inlineSpan, offset, offset + length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    /**
     * 控制视频、gif、长图标识的显示和隐藏
     *
     * @param imageItemView      包裹图标的外层View
     * @param blockImageSpanBean 相关实体
     */
    private void setMarkIconVisibility(View imageItemView, BlockSpanBean blockImageSpanBean) {
        //这里我们还可以扩展带删除图标
        ImageView ivVideoIcon = imageItemView.findViewById(R.id.ivVideoIcon);
        TextView tvGifOrLongImageMark = imageItemView.findViewById(R.id.tvGifOrLongImageMark);
        ImageView ivImageDel = imageItemView.findViewById(R.id.ivImageDel);
        //控制视频、gif、长图标识图标的显示和隐藏
        ivVideoIcon.setVisibility(GONE);
        tvGifOrLongImageMark.setVisibility(GONE);
        ivImageDel.setVisibility(View.GONE);
        // 处理视频
        if (TextUtils.equals(blockImageSpanBean.getType(), BlockSpanEnum.VIDEO) && mIsShowVideoMark && mVideoMarkResourceId != 0) {
            // 视频封面，显示视频标识
            Drawable videoIconDrawable = AppCompatResources.getDrawable(mActivity, mVideoMarkResourceId);
            if (videoIconDrawable != null) {
                ivVideoIcon.setVisibility(VISIBLE);
                ivVideoIcon.setImageDrawable(videoIconDrawable);
                ViewGroup.LayoutParams layoutParams = ivVideoIcon.getLayoutParams();
                layoutParams.width = videoIconDrawable.getIntrinsicWidth();
                layoutParams.height = videoIconDrawable.getIntrinsicHeight();
            }
            return;
        }
        // 处理长图
        if (blockImageSpanBean.isLong() && mIsShowLongImageMark) {
            // 长图，显示长图标识
            tvGifOrLongImageMark.setVisibility(VISIBLE);
            tvGifOrLongImageMark.setText(ImageTypeMarkEnum.LONG);
            return;
        }
        // 处理gif
        if (blockImageSpanBean.isGif() && mIsShowGifMark) {
            // gif, 显示gif标识
            tvGifOrLongImageMark.setVisibility(VISIBLE);
            tvGifOrLongImageMark.setText(ImageTypeMarkEnum.GIF);
        }
    }

    /**
     * 根据文件路径或者图片url插入图片或视频封面
     *
     * @param richBlockBean 相关实体
     */
    public void insertBlockImage(@NonNull RichBlockBean richBlockBean) {
        // 图片(或视频)的url或者本地的图片路径
        BlockSpanBean blockSpanBean = richBlockBean.getBlockSpan();
        if (blockSpanBean == null) {
            return;
        }
        String url = blockSpanBean.getPath();
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "url is empty");
            return;
        }
        int maxWidth = getWidthWithoutPadding();
        blockSpanBean.setMaxWidth(maxWidth);
        if (blockSpanBean.getMaxHeight() <= 0) {
            blockSpanBean.setMaxHeight(ViewUtil.dp2px(1000));
        }
        //防止异步加载过长，打乱顺序，我们先占位
        int start = getText().toString().length();
        Drawable plackHolderDrawable = AppCompatResources.getDrawable(mActivity, R.drawable.bg_personal_space);
        insertBlockImage(plackHolderDrawable, new SpannableString(IMAGE_SPAN_PLACEHOLDER), blockSpanBean);
        int end = getText().toString().length();
        Glide.with(mActivity)
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // 图片加载完成之后，替换占位图片，当然这里会有一个问题，如果异步加载过慢，那么用户如果删除了占位图片如何操作。
                        SpannableString spanStringContent = new SpannableString(IMAGE_SPAN_PLACEHOLDER + "\n");
                        spanStringContent.setSpan(getBlockImageSpan(resource, blockSpanBean), 0, spanStringContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //所以这里用的是替换，而不再是append了
                        getEditableText().replace(start, end, spanStringContent);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * 插入图片(Drawable)
     *
     * @param resource          drawable资源
     * @param spanStringContent SpannableString 文本
     * @param blockSpanBean     图片的扩展对象
     */
    private void insertBlockImage(@NonNull Drawable resource, @NonNull SpannableString spanStringContent, BlockSpanBean blockSpanBean) {
        BlockImageSpan blockImageSpan = getBlockImageSpan(resource, blockSpanBean);
        spanStringContent.setSpan(blockImageSpan, 0, spanStringContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        append(spanStringContent);
        append("\n");
    }

    /**
     * 获取图片的BlockSpan
     *
     * @param resource      图片资源drawable
     * @param blockSpanBean 图片的span对象
     * @return 图片的Span
     */
    @NonNull
    private BlockImageSpan getBlockImageSpan(@NonNull Drawable resource, BlockSpanBean blockSpanBean) {
        View imageItemView = mActivity.getLayoutInflater().inflate(R.layout.rich_editor_image, null);
        RoundedImageView imageView = imageItemView.findViewById(R.id.image);
        imageView.setImageDrawable(resource);
        // 设置圆角
        imageView.setCornerRadius(mImageRadius);
        blockSpanBean.setShowDel(false);
        // 控制视频、gif、长图标识的显示和隐藏
        setMarkIconVisibility(imageItemView, blockSpanBean);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int imageWidth = blockSpanBean.getWidth();
        int imageHeight = blockSpanBean.getHeight();
        layoutParams.width = imageWidth;
        layoutParams.height = imageHeight;
        int paddingLeft = TextUtils.equals(BlockSpanEnum.GAME, blockSpanBean.getType()) ? (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_left) : 0;
        int paddingTop = TextUtils.equals(BlockSpanEnum.GAME, blockSpanBean.getType()) ? (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_top) : 0;
        ViewUtil.layoutView(
                imageItemView,
                imageWidth + 2 * paddingLeft,
                imageHeight + 2 * paddingTop
        );
        BlockImageSpan blockImageSpan = new BlockImageSpan(mActivity, BitmapUtil.getBitmap(imageItemView), blockSpanBean);
        // 设置图片点击监听器
        if (mOnRichClickListener != null) {
            blockImageSpan.setOnRichClickListener(mOnRichClickListener);
        }
        return blockImageSpan;
    }

    /**
     * 插入图片
     *
     * @param resourceId    图片的资源id
     * @param richBlockBean 图片的实体类
     */
    public void insertBlockImage(@DrawableRes int resourceId, @NonNull RichBlockBean richBlockBean) {
        try {
            //这里可以用占位图
            Drawable drawable = AppCompatResources.getDrawable(mActivity, resourceId);
            BlockSpanBean blockSpanBean = richBlockBean.getBlockSpan();
            if (drawable == null || blockSpanBean == null) {
                return;
            }
            SpannableString spanStringContent = new SpannableString(IMAGE_SPAN_PLACEHOLDER);
            insertBlockImage(drawable, spanStringContent, blockSpanBean);
        } catch (Exception e) {
            LogUtil.e(TAG, "Unable to find resource: " + resourceId);
        }
    }

    /**
     * 插入分割线
     *
     * @param resourceId    图片的资源id
     * @param richBlockBean 图片的实体类
     */
    public void insertDivider(@DrawableRes int resourceId, @NonNull RichBlockBean richBlockBean) {
        try {
            //这里可以用占位图
            BlockSpanBean blockSpanBean = richBlockBean.getBlockSpan();
            Drawable drawable = AppCompatResources.getDrawable(mActivity, resourceId);
            if (drawable == null || blockSpanBean == null) {
                return;
            }
            drawable.setBounds(0, 0, blockSpanBean.getWidth(), blockSpanBean.getHeight());
            SpannableString spanStringContent = new SpannableString(IMAGE_SPAN_PLACEHOLDER);
            spanStringContent.setSpan(new BlockImageSpan(drawable, blockSpanBean), 0, spanStringContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            append(spanStringContent);
            append("\n");
        } catch (Exception e) {
            LogUtil.e(TAG, "Unable to find resource: " + resourceId);
        }
    }

    /**
     * 插入图片
     *
     * @param bitmap             图片的bitmap
     * @param spanStringContent  内容文本
     * @param blockImageSpanBean 图片的扩展实体类
     */
    public void insertBlockImage(Bitmap bitmap, @NonNull SpannableString spanStringContent, BlockSpanBean blockImageSpanBean) {
        Drawable drawable = mActivity != null
                ? new BitmapDrawable(mActivity.getResources(), bitmap)
                : new BitmapDrawable(bitmap);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, Math.max(width, 0), Math.max(height, 0));
        insertBlockImage(drawable, spanStringContent, blockImageSpanBean);
    }

    public int getVideoMarkResourceId() {
        return mVideoMarkResourceId;
    }

    public void setVideoMarkResourceId(int videoMarkResourceId) {
        this.mVideoMarkResourceId = videoMarkResourceId;
    }

    public boolean isShowVideoMark() {
        return mIsShowVideoMark;
    }

    public void setIsShowVideoMark(boolean isShowVideoMark) {
        this.mIsShowVideoMark = isShowVideoMark;
    }

    public boolean isShowGifMark() {
        return mIsShowGifMark;
    }

    public void setIsShowGifMark(boolean isShowGifMark) {
        this.mIsShowGifMark = isShowGifMark;
    }

    public boolean isShowLongImageMark() {
        return mIsShowLongImageMark;
    }

    public void setIsShowLongImageMark(boolean isShowLongImageMark) {
        this.mIsShowLongImageMark = isShowLongImageMark;
    }

    public int getHeadlineTextSize() {
        return mHeadlineTextSize;
    }

    public void setHeadlineTextSize(int headlineTextSize) {
        this.mHeadlineTextSize = headlineTextSize;
    }

    /**
     * 获取段落的对象，其实就是标题、引用、换行等
     *
     * @param blockType 段落类型
     * @return 段落对象
     */
    private IBlockSpan getBlockSpan(@BlockSpanEnum String blockType) {
        switch (blockType) {
            case BlockSpanEnum.HEADLINE:
                return new HeadlineSpan(mHeadlineTextSize);
            case BlockSpanEnum.QUOTE:
                return new CustomQuoteSpan(mActivity);
            default:
                return null;
        }
    }

    /**
     * 获取行内的Span对象，
     *
     * @param inlineType 行内标签类型
     * @return 返回行内标签对象
     */
    private IInlineSpan getInlineStyleSpan(@InlineSpanEnum String inlineType) {
        switch (inlineType) {
            case InlineSpanEnum.BOLD:
                return new BoldStyleSpan();
            case InlineSpanEnum.ITALIC:
                return new ItalicStyleSpan();
            case InlineSpanEnum.STRIKE_THROUGH:
                return new CustomStrikeThroughSpan();
            case InlineSpanEnum.UNDERLINE:
                return new CustomUnderlineSpan();
            default:
                return null;
        }
    }

    /**
     * 获取行内的Span对象，
     *
     * @param spanClazz 行内标签的class对象
     * @return 返回行内标签对象
     */
    private IInlineSpan getInlineStyleSpan(Class spanClazz) {
        if (BoldStyleSpan.class == spanClazz) {
            return new BoldStyleSpan();
        } else if (ItalicStyleSpan.class == spanClazz) {
            return new ItalicStyleSpan();
        } else if (CustomStrikeThroughSpan.class == spanClazz) {
            return new CustomStrikeThroughSpan();
        } else if (CustomUnderlineSpan.class == spanClazz) {
            return new CustomUnderlineSpan();
        }

        return null;
    }


    /**
     * 获取Span的flag
     *
     * @param type 标签类型
     * @return span的flag
     */
    private int getBlockSpanFlag(String type) {
        /**
         *     exclusive 不包含
         *     inclusive 包含
         *
         *     int SPAN_EXCLUSIVE_EXCLUSIVE = 33;
         *     //在Span前后输入的字符都不应用Span效果
         *
         *     int SPAN_EXCLUSIVE_INCLUSIVE = 34;
         *     //在Span前面输入的字符不应用Span效果，后面输入的字符应用Span效果
         *
         *     int SPAN_INCLUSIVE_EXCLUSIVE = 17;
         *     //在Span前面输入的字符应用Span效果，后面输入的字符不应用Span效果
         *
         *     int SPAN_INCLUSIVE_INCLUSIVE = 18;
         *     //在Span前后输入的字符都应用Span效果
         */
        if (BlockSpanEnum.QUOTE.equals(type)) {
            return Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        }
        return Spanned.SPAN_INCLUSIVE_INCLUSIVE;
    }

    /**
     * 在插入blockImage之前，先删除被光标选中的区域
     */
    private void removeSelectedContent() {
        Editable editable = getEditableText();
        int start = getText().toString().length();
        IInlineSpan[] inlineSpans = editable.getSpans(start, start, IInlineSpan.class);
        // 可能存在多个分段的span，需要先都移除
        for (IInlineSpan span : inlineSpans) {
            // 先将两端的span进行切割
            handleInlineStyleBoundary(start, start, getInlineSpanClass(span.getType()));
            editable.removeSpan(span);
        }
    }

    /**
     * 通过样式类型获取对应Class(肯定是行内标签)
     */
    private Class getInlineSpanClass(@InlineSpanEnum String type) {
        switch (type) {
            case InlineSpanEnum.BOLD:
                return BoldStyleSpan.class;
            case InlineSpanEnum.ITALIC:
                return ItalicStyleSpan.class;
            case InlineSpanEnum.STRIKE_THROUGH:
                return CustomStrikeThroughSpan.class;
            case InlineSpanEnum.UNDERLINE:
                return CustomUnderlineSpan.class;
            case InlineSpanEnum.AT:
            case InlineSpanEnum.TOPIC:
            case InlineSpanEnum.LINK:
                return AtSpan.class;
            default:
                return null;
        }
    }

    /**
     * 处理行内样式的边界
     * 比如选中的区域start和end分别处于两个指定StyleSpan时间，则需要将这两端的StyleSpan切割成左右两块
     *
     * @param start     索引的首
     * @param end       索引的尾
     * @param spanClazz 执行行内样式class类型
     */
    private void handleInlineStyleBoundary(int start, int end, Class spanClazz) {
        Editable editable = getEditableText();
        IInlineSpan[] inlineSpans = (IInlineSpan[]) editable.getSpans(start, end, spanClazz);
        if (inlineSpans.length <= 0) {
            return;
        }
        if (inlineSpans.length == 1) {
            IInlineSpan singleSpan = inlineSpans[0];
            int singleSpanStart = editable.getSpanStart(singleSpan);
            int singleSpanEnd = editable.getSpanEnd(singleSpan);
            if (singleSpanStart < start) {
                IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
                if (wantAddSpan != null) {
                    editable.setSpan(wantAddSpan, singleSpanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            if (singleSpanEnd > end) {
                IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
                if (wantAddSpan != null) {
                    editable.setSpan(wantAddSpan, end, singleSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
            }

            return;
        }

        IInlineSpan firstSpan = inlineSpans[0];
        IInlineSpan lastSpan = inlineSpans[inlineSpans.length - 1];

        int firstSpanStart = editable.getSpanStart(firstSpan);
        if (firstSpanStart < start) {
            IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
            if (wantAddSpan != null) {
                editable.setSpan(wantAddSpan, firstSpanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        int lastSpanEnd = editable.getSpanEnd(lastSpan);
        if (lastSpanEnd > end) {
            IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
            if (wantAddSpan != null) {
                editable.setSpan(wantAddSpan, end, lastSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }
}
