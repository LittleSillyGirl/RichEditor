package com.yuruiyin.richeditor;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hanks.lineheightedittext.LineHeightEditText;
import com.makeramen.roundedimageview.RoundedImageView;
import com.yuruiyin.richeditor.callback.OnRichClickListener;
import com.yuruiyin.richeditor.config.AppConfig;
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.enumtype.FileTypeEnum;
import com.yuruiyin.richeditor.enumtype.ImageTypeMarkEnum;
import com.yuruiyin.richeditor.ext.LongClickableLinkMovementMethod;
import com.yuruiyin.richeditor.model.BlockSpanBean;
import com.yuruiyin.richeditor.model.InlineSpanBean;
import com.yuruiyin.richeditor.model.RichBlockBean;
import com.yuruiyin.richeditor.model.StyleBtnBean;
import com.yuruiyin.richeditor.span.AtSpan;
import com.yuruiyin.richeditor.span.BlockImageSpan;
import com.yuruiyin.richeditor.span.InlineSpan;
import com.yuruiyin.richeditor.undoredo.UndoRedoHelper;
import com.yuruiyin.richeditor.utils.BitmapUtil;
import com.yuruiyin.richeditor.utils.ClipboardUtil;
import com.yuruiyin.richeditor.utils.FileUtil;
import com.yuruiyin.richeditor.utils.LogUtil;
import com.yuruiyin.richeditor.utils.ViewUtil;
import com.yuruiyin.richeditor.utils.WindowUtil;

import java.io.File;
import java.util.List;

/**
 * 自定义EditText，实现富文本，同时可监听光标位置变化
 *
 * @author admin
 * @version 2019-04-29
 */
public class RichEditText extends LineHeightEditText {

    private final String TAG = "RichEditText";

    /**
     * 宽度撑满编辑区的ImageSpan需要减去的一个值，为了防止ImageSpan碰到边界导致的重复绘制的问题
     */
    private final int IMAGE_SPAN_MINUS_VALUE = 6;

    /**
     * 块级padding值
     */
    private int mImageSpanPaddingTop;
    private int mImageSpanPaddingBottom;
    private int mImageSpanPaddingLeft;
    private int mImageSpanPaddingRight;

    /**
     * 是否显示视频标识
     */
    private boolean mIsShowVideoMark;

    /**
     * 是否显示图片删除图标
     */
    private boolean mIsShowImageDel;
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

    private RichInputConnectionWrapper mRichInputConnection;

    private Activity mActivity;

    /**
     * 富文本工具类
     */
    private RichHelper mRichHelper;

    /**
     * 撤回辅助类
     */
    private UndoRedoHelper mUndoRedoHelper;

    /**
     * 光标位置的变化回调监听
     */
    public interface OnSelectionChangedListener {
        /**
         * 光标位置改变回调
         *
         * @param selStart 新的光标起始位置
         * @param selEnd   新的光标结束位置
         */
        void onChange(int selStart, int selEnd);
    }

    /**
     * EditText监听复制、粘贴、剪切事件回调的接口
     */
    public interface IClipCallback {
        /**
         * 剪切回调
         */
        void onCut();

        /**
         * 复制回调
         */
        void onCopy();

        /**
         * 粘贴回调
         */
        void onPaste();
    }

    /**
     * 光标位置变化监听器
     */
    private OnSelectionChangedListener mOnSelectionChangedListener;

    public RichEditText(Context context) {
        super(context);
        init(context, null);
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化属性
     *
     * @param context 上下文生命周期
     * @param attrs   属性
     */
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichEditText);
            //是否显示视频标识图标
            mIsShowVideoMark = ta.getBoolean(R.styleable.RichEditText_editor_show_video_mark, true);
            //是否显示图片删除图标
            mIsShowImageDel = ta.getBoolean(R.styleable.RichEditText_editor_show_image_del, true);
            //视频图标资源id
            mVideoMarkResourceId = ta.getResourceId(R.styleable.RichEditText_editor_video_mark_resource_id, R.drawable.default_video_icon);
            //是否显示gif标识图标
            mIsShowGifMark = ta.getBoolean(R.styleable.RichEditText_editor_show_gif_mark, true);
            //是否显示长图标识
            mIsShowLongImageMark = ta.getBoolean(R.styleable.RichEditText_editor_show_long_image_mark, true);

            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            //是否显示长图标识
            mImageRadius = (int) ta.getDimension(R.styleable.RichEditText_editor_image_radius, 0);

            float defHeadlineTextSize = context.getResources().getDimension(R.dimen.rich_editor_headline_text_size);
            //标题字体大小
            mHeadlineTextSize = (int) ta.getDimension(R.styleable.RichEditText_editor_headline_text_size, defHeadlineTextSize);

            ta.recycle();
        }

        mActivity = (Activity) context;
        if (mActivity == null) {
            LogUtil.e(TAG, "activity is null");
            return;
        }
        mImageSpanPaddingTop = (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_top);
        mImageSpanPaddingBottom = (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_bottom);
        mImageSpanPaddingLeft = (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_left);
        mImageSpanPaddingRight = (int) mActivity.getResources().getDimension(R.dimen.rich_editor_image_span_padding_right);

        mRichInputConnection = new RichInputConnectionWrapper(null, true);
        requestFocus();
        setSelection(0);
        //辅助类
        mRichHelper = new RichHelper(mActivity, this);
        //富文本的点击事件
        setMovementMethod(new LongClickableLinkMovementMethod());
        //屏幕的宽度
        mScreenWidth = WindowUtil.getScreenSize(mActivity)[0];
    }

    public void undo() {
        if (mUndoRedoHelper != null) {
            mUndoRedoHelper.undo();
        }
    }

    public void redo() {
        if (mUndoRedoHelper != null) {
            mUndoRedoHelper.redo();
        }
    }

    /**
     * 获取不含padding的控件宽度
     *
     * @return 控件宽度
     */
    private int getWidthWithoutPadding() {
        int editTextMeasureWidth = getMeasuredWidth();
        if (editTextMeasureWidth <= 0) {
            // 可能是编辑器还不可见, 则直接设置编辑器的宽度为屏幕的宽度
            editTextMeasureWidth = mScreenWidth;
        }
        return editTextMeasureWidth - getPaddingLeft() - getPaddingRight() - IMAGE_SPAN_MINUS_VALUE;
    }

    /**
     * 初始化样式按钮（如注册按钮监听器）
     *
     * @param styleBtnBean 带选中样式的按钮实体类
     */
    public void initStyleButton(StyleBtnBean styleBtnBean) {
        mRichHelper.initStyleButton(styleBtnBean);
    }

    /**
     * 清空编辑器的内容
     */
    public void clearContent() {
        setText("");
        requestFocus();
        setSelection(0);
    }

    /**
     * 插入整段文本(可能是普通文本、段落样式文本（标题或引用）)
     * 使用场景：如恢复草稿
     *
     * @param richBlockBean 段落的文本内容
     */
    public void insertBlockText(RichBlockBean richBlockBean) {
        //应为块级也就是段落的因素，我们需要主动加入换行符
        SpannableString spanStringContent = new SpannableString(richBlockBean.getText() + "\n");
        String blockType = richBlockBean.getBlockType();
        switch (blockType) {
            default:
            case BlockSpanEnum.NORMAL_TEXT:
                mRichHelper.insertNormalTextBlock(spanStringContent, richBlockBean.getInlineStyleEntityList());
                break;
            case BlockSpanEnum.HEADLINE:
            case BlockSpanEnum.QUOTE:
                mRichHelper.insertBlockSpanText(blockType, spanStringContent, richBlockBean.getInlineStyleEntityList());
                break;
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
        ivImageDel.setVisibility(mIsShowImageDel ? View.VISIBLE : View.GONE);
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
     * 在插入blockImage之前，先删除被光标选中的区域
     */
    private void removeSelectedContent() {
        Editable editable = getEditableText();
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();

        if (selectionStart >= selectionEnd) {
            return;
        }

        editable.delete(selectionStart, selectionEnd);
    }

    /**
     * 插入@或者话题，这里采用的是行内image，而不是文字
     *
     * @param inlineSpanBean      行内标签的实体类
     * @param onRichClickListener 点击@用户或者话题的回调函数
     */
    public void insertInlineImage(@NonNull InlineSpanBean inlineSpanBean, OnRichClickListener<InlineSpan> onRichClickListener) {
        removeSelectedContent();
        InlineSpan inlineImageSpan = new InlineSpan(inlineSpanBean);
        mRichHelper.insertInlineImageSpan(inlineImageSpan);
        // 设置图片点击监听器
        inlineImageSpan.setOnRichClickListener(onRichClickListener);
    }

    /**
     * 插入@用户或者#话题，这里采用的是行内文本而不是image
     *
     * @param inlineSpanBean      @用户的行内标签实体类
     * @param onRichClickListener 点击@用户或者话题的回调函数
     */
    public void insertAt(@NonNull InlineSpanBean inlineSpanBean, OnRichClickListener<AtSpan> onRichClickListener) {
        removeSelectedContent();
        AtSpan atSpan = new AtSpan(inlineSpanBean);
        mRichHelper.insertAtSpan(atSpan);
        atSpan.setOnRichClickListener(onRichClickListener);
    }

    private final int IMAGE_WIDTH = ViewUtil.dp2px(120);

    /**
     * 插入图片(Drawable), 通过drawable显示图片
     *
     * @param drawable            图片drawable
     * @param blockImageSpanBean  图片扩展的实体类
     * @param onRichClickListener 点击图片的回调监听
     */
    public void insertBlockImage(Drawable drawable, @NonNull BlockSpanBean blockImageSpanBean, OnRichClickListener<BlockImageSpan> onRichClickListener) {
        removeSelectedContent();
        int originWidth = drawable.getIntrinsicWidth();
        int originHeight = drawable.getIntrinsicHeight();
        //是否是长图
        blockImageSpanBean.setLong(originHeight > originWidth * AppConfig.IMAGE_MAX_HEIGHT_WIDTH_RATIO);
        // 这里减去一个值是为了防止部分手机（如华为Mate-10）ImageSpan右侧超出编辑区的时候，会导致ImageSpan被重复绘制的问题
        int editTextWidth = getWidthWithoutPadding();
        int imageWidth = blockImageSpanBean.getWidth() <= 0 ? originWidth : blockImageSpanBean.getWidth();
        imageWidth = Math.max(imageWidth, IMAGE_WIDTH);
        int resImageWidth = Math.min(imageWidth, editTextWidth);
        int imageMaxHeight = blockImageSpanBean.getMaxHeight() <= 0 ? originHeight : blockImageSpanBean.getMaxHeight();
        int resImageHeight = (int) (originHeight * 1.0 / originWidth * resImageWidth);
        resImageHeight = Math.min(resImageHeight, imageMaxHeight);
//        // 控制显示出来的图片的高度不会大于宽度的3倍
//        double maxHeightWidthRadio = AppConfig.IMAGE_MAX_HEIGHT_WIDTH_RATIO;
//        resImageHeight = resImageHeight > resImageWidth * maxHeightWidthRadio
//                ? (int) (resImageWidth * maxHeightWidthRadio)
//                : resImageHeight;
        blockImageSpanBean.setWidth(resImageWidth);
        blockImageSpanBean.setHeight(resImageHeight);
        blockImageSpanBean.setShowDel(mIsShowImageDel);
        View imageItemView = mActivity.getLayoutInflater().inflate(R.layout.rich_editor_image, null);
        RoundedImageView imageView = imageItemView.findViewById(R.id.image);
        imageView.setImageDrawable(drawable);
        // 设置圆角
        imageView.setCornerRadius(mImageRadius);

        // 控制视频、gif、长图标识的显示和隐藏
        setMarkIconVisibility(imageItemView, blockImageSpanBean);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = resImageWidth;
        layoutParams.height = resImageHeight;
        ViewUtil.layoutView(
                imageItemView,
                resImageWidth + mImageSpanPaddingLeft + mImageSpanPaddingRight,
                resImageHeight + mImageSpanPaddingTop + mImageSpanPaddingBottom
        );
        BlockImageSpan blockImageSpan = new BlockImageSpan(mActivity, BitmapUtil.getBitmap(imageItemView), blockImageSpanBean);
        mRichHelper.insertBlockImageSpan(blockImageSpan);
        // 设置图片点击监听器
        blockImageSpan.setOnRichClickListener(onRichClickListener);
    }

    /**
     * 根据uri插入图片或视频封面
     * 插入图片(本地Uri)
     *
     * @param uri                 文件uri
     * @param blockImageSpanBean  相关实体
     * @param onRichClickListener 图片点击事件监听器
     */
    public void insertBlockImage(Uri uri, @NonNull BlockSpanBean blockImageSpanBean, OnRichClickListener<BlockImageSpan> onRichClickListener) {
        if (uri == null) {
            LogUtil.e(TAG, "uri is null");
            return;
        }
        String filePath = FileUtil.getFileRealPath(mActivity, uri);
        if (TextUtils.isEmpty(filePath)) {
            LogUtil.e(TAG, "file path is empty");
            return;
        }
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            LogUtil.e(TAG, "image file does not exist");
            return;
        }
        String fileType = FileUtil.getFileType(filePath);
        int maxWidth = getWidthWithoutPadding();
        blockImageSpanBean.setMaxWidth(maxWidth);
        if (blockImageSpanBean.getMaxHeight() <= 0) {
            blockImageSpanBean.setMaxHeight(ViewUtil.dp2px(1000));
        }
        switch (fileType) {
            case FileTypeEnum.VIDEO:
                blockImageSpanBean.setGif(false);
                break;
            default:
            case FileTypeEnum.STATIC_IMAGE:
            case FileTypeEnum.GIF:
                blockImageSpanBean.setGif(FileTypeEnum.GIF.equals(fileType));
                // 通过uri或path调用的可以断定为相册图片或视频，有添加圆角的需求
                blockImageSpanBean.setPhoto(true);
                break;
        }
        Glide.with(mActivity)
                .load(uri)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        insertBlockImage(resource, blockImageSpanBean, onRichClickListener);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    /**
     * 插入图片(本地资源id)
     *
     * @param resourceId          图片的资源id
     * @param blockImageSpanBean  图片的扩展实体类
     * @param onRichClickListener 图片的点击回调监听
     */
    public void insertBlockImage(@DrawableRes int resourceId, @NonNull BlockSpanBean blockImageSpanBean, OnRichClickListener<BlockImageSpan> onRichClickListener) {
        try {
            Drawable drawable = AppCompatResources.getDrawable(mActivity, resourceId);
            insertBlockImage(drawable, blockImageSpanBean, onRichClickListener);


        } catch (Exception e) {
            LogUtil.e(TAG, "Unable to find resource: " + resourceId);
        }
    }

    /**
     * 插入图片(本地资源id)
     *
     * @param resourceId         图片的资源id
     * @param blockImageSpanBean 图片的扩展实体类
     */
    public void insertDivider(@DrawableRes int resourceId, @NonNull BlockSpanBean blockImageSpanBean) {
        try {
            Drawable drawable = AppCompatResources.getDrawable(mActivity, resourceId);
            if (drawable == null) {
                return;
            }
            removeSelectedContent();
            int originWidth = drawable.getIntrinsicWidth();
            int originHeight = drawable.getIntrinsicHeight();
            int editTextWidth = getWidthWithoutPadding();
            drawable.setBounds(0, 0, Math.max(originWidth, editTextWidth), Math.max(originHeight, 0));
            // 这里减去一个值是为了防止部分手机（如华为Mate-10）ImageSpan右侧超出编辑区的时候，会导致ImageSpan被重复绘制的问题
            blockImageSpanBean.setMaxWidth(editTextWidth);
            if (blockImageSpanBean.getMaxHeight() <= 0) {
                blockImageSpanBean.setMaxHeight(ViewUtil.dp2px(1000));
            }
            int imageWidth = blockImageSpanBean.getWidth() <= 0 ? originWidth : blockImageSpanBean.getWidth();
            imageWidth = Math.max(imageWidth, IMAGE_WIDTH);
            int resImageWidth = Math.min(imageWidth, editTextWidth);
            int imageMaxHeight = blockImageSpanBean.getMaxHeight() <= 0 ? originHeight : blockImageSpanBean.getMaxHeight();
            int resImageHeight = (int) (originHeight * 1.0 / originWidth * resImageWidth);
            resImageHeight = Math.min(resImageHeight, imageMaxHeight);
            blockImageSpanBean.setWidth(resImageWidth);
            blockImageSpanBean.setHeight(resImageHeight);
            BlockImageSpan blockImageSpan = new BlockImageSpan(drawable, blockImageSpanBean);
            mRichHelper.insertBlockImageSpan(blockImageSpan);
        } catch (Exception e) {
            LogUtil.e(TAG, "Unable to find resource: " + resourceId);
        }
    }

    /**
     * 插入图片(bitmap)
     *
     * @param bitmap              图片的bitmap
     * @param blockImageSpanBean  图片的扩展实体类
     * @param onRichClickListener 图片的点击监听
     */
    public void insertBlockImage(Bitmap bitmap, @NonNull BlockSpanBean blockImageSpanBean, OnRichClickListener<BlockImageSpan> onRichClickListener) {
        Drawable drawable = mActivity != null
                ? new BitmapDrawable(mActivity.getResources(), bitmap)
                : new BitmapDrawable(bitmap);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, Math.max(width, 0), Math.max(height, 0));
        insertBlockImage(drawable, blockImageSpanBean, onRichClickListener);
    }

    /**
     * 根据文件路径插入图片或视频封面
     *
     * @param url                 图片(或视频)的url或者本地的图片路径
     * @param blockImageSpanBean  相关实体
     * @param onRichClickListener 图片点击事件监听器
     */
    public void insertBlockImage(String url, @NonNull BlockSpanBean blockImageSpanBean, OnRichClickListener<BlockImageSpan> onRichClickListener) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "file path is empty");
            return;
        }
        int maxWidth = getWidthWithoutPadding();
        blockImageSpanBean.setMaxWidth(maxWidth);
        if (blockImageSpanBean.getMaxHeight() <= 0) {
            blockImageSpanBean.setMaxHeight(ViewUtil.dp2px(1000));
        }
        Glide.with(mActivity)
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        insertBlockImage(resource, blockImageSpanBean, onRichClickListener);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * 获取编辑器中的内容
     *
     * @return 编辑的内容
     */
    public List<RichBlockBean> getRichBlockBeanList() {
        return mRichHelper.getRichBlockBeanList();
    }

    public RichHelper getRichHelper() {
        return mRichHelper;
    }

    /**
     * 设置软键盘删除按键监听器
     *
     * @param backspaceListener 软键盘删除按键监听器
     */
    protected void setBackspaceListener(RichInputConnectionWrapper.BackspaceListener
                                                backspaceListener) {
        mRichInputConnection.setBackspaceListener(backspaceListener);
    }

    /**
     * 注册光标位置监听器
     *
     * @param listener 光标位置变化监听器
     */
    protected void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mOnSelectionChangedListener = listener;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mOnSelectionChangedListener != null) {
            mOnSelectionChangedListener.onChange(selStart, selEnd);
        }
    }

    /**
     * 处理粘贴
     */
    private void handlePaste() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        Editable editable = getEditableText();
        editable.delete(selectionStart, selectionEnd);
        selectionStart = getSelectionStart();
        mRichHelper.insertCharSequence(ClipboardUtil.getInstance(mActivity).getClipboardText(), selectionStart);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        switch (id) {
            case android.R.id.cut:
                if (mActivity instanceof IClipCallback) {
                    ((IClipCallback) mActivity).onCut();
                }
                break;
            case android.R.id.copy:
                if (mActivity instanceof IClipCallback) {
                    ((IClipCallback) mActivity).onCopy();
                }
                break;
            case android.R.id.paste:
                if (mActivity instanceof IClipCallback) {
                    ((IClipCallback) mActivity).onPaste();
                }

                handlePaste();
                return true;
            default:
                break;
        }

        return super.onTextContextMenuItem(id);
    }

    /**
     * 当输入法和EditText建立连接的时候会通过这个方法返回一个InputConnection。
     * 我们需要代理这个方法的父类方法生成的InputConnection并返回我们自己的代理类。
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        mRichInputConnection.setTarget(super.onCreateInputConnection(outAttrs));
        return mRichInputConnection;
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

    public void setUndoRedoEnable(boolean enableUndoRedo) {
        if (enableUndoRedo) {
            mUndoRedoHelper = new UndoRedoHelper(this);
        }
    }

    public boolean isUndoRedoEnable() {
        return mUndoRedoHelper != null;
    }

    /**
     * 是否正在删除图片
     */
    private boolean isDeletingImg;

    public boolean isDeletingImg() {
        return isDeletingImg;
    }

    public void setDeletingImg(boolean deletingImg) {
        isDeletingImg = deletingImg;
    }

    @Override
    public int getOffsetForPosition(float x, float y) {
        if (isDeletingImg) {
            int offset = super.getOffsetForPosition(x, y);
            int selectionStart = Math.max(getSelectionStart(), 0);
            return Math.max(offset, selectionStart);
        }
        return super.getOffsetForPosition(x, y);
    }
}
