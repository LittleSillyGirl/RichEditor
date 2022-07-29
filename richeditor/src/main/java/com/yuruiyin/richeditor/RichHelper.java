package com.yuruiyin.richeditor;

import android.app.Activity;
import android.database.Observable;
import android.graphics.Color;
import android.text.Editable;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yuruiyin.richeditor.config.AppConfig;
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;
import com.yuruiyin.richeditor.model.BlockSpanBean;
import com.yuruiyin.richeditor.model.InlineSpanBean;
import com.yuruiyin.richeditor.model.RichBlockBean;
import com.yuruiyin.richeditor.model.StyleBtnBean;
import com.yuruiyin.richeditor.span.AtSpan;
import com.yuruiyin.richeditor.span.BlockImageSpan;
import com.yuruiyin.richeditor.span.BoldStyleSpan;
import com.yuruiyin.richeditor.span.CustomQuoteSpan;
import com.yuruiyin.richeditor.span.CustomStrikeThroughSpan;
import com.yuruiyin.richeditor.span.CustomUnderlineSpan;
import com.yuruiyin.richeditor.span.HeadlineSpan;
import com.yuruiyin.richeditor.span.IBlockSpan;
import com.yuruiyin.richeditor.span.IInlineSpan;
import com.yuruiyin.richeditor.span.InlineSpan;
import com.yuruiyin.richeditor.span.ItalicStyleSpan;
import com.yuruiyin.richeditor.undoredo.UndoRedoHelper;
import com.yuruiyin.richeditor.utils.LogUtil;
import com.yuruiyin.richeditor.utils.SoftKeyboardUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 富文本编辑器帮助类。用来处理行内样式、段样式，图片（或视频封面）、自定义布局等。
 *
 * @author admin
 */
public class RichHelper {

    // 样式修改被观察者
    private final ToggleStyleObservable mToggleStyleObservable = new ToggleStyleObservable();

    private final String TAG = "RichHelper";

    /**
     * 富文本编辑器
     */
    private RichEditText mRichEditText;

    /**
     * activity
     */
    private Activity mActivity;

    /**
     * 标记支持哪些行内和段落样式
     */
    private final Map<String, StyleBtnBean> mRichStyleBtnMap = new HashMap<>();

    RichHelper(Activity activity, RichEditText richEditText) {
        //activity
        mActivity = activity;
        //富文本编辑器
        mRichEditText = richEditText;

        //监听文本输入变化
        mRichEditText.addTextWatcher(new RichTextWatcher(mRichEditText));

        // 监听光标位置变化
        mRichEditText.setOnSelectionChangedListener(this::handleSelectionChanged);

        // 监听删除按键
        mRichEditText.setBackspaceListener(this::handleDeleteKey);

        //为了兼容模拟器
        mRichEditText.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.KEYCODE_DEL == event.getKeyCode()
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && !SoftKeyboardUtil.isSoftShowing(mActivity)) {
                //监听到删除键但是软键盘没弹出，可以基本断定是用模拟器
                // TODO 也存在模拟器也会弹出软键盘的
                return handleDeleteKey();
            }
            return false;
        });
    }

    /**
     * 初始化样式按钮（如注册按钮监听器）
     *
     * @param styleBtnBean 样式实体
     */
    void initStyleButton(StyleBtnBean styleBtnBean) {
        String type = styleBtnBean.getType();
        //是否是行内标签
        styleBtnBean.setInlineType(isInlineType(type));
        //把需要样式变化的btn存储起来
        mRichStyleBtnMap.put(type, styleBtnBean);
        //获取点击view
        View clickedView = styleBtnBean.getClickedView();
        if (clickedView == null) {
            clickedView = styleBtnBean.getIvIcon();
        }
        clickedView.setOnClickListener(v -> {
            if (mRichEditText.isFocused()) {
                // 若未聚焦，则不响应点击事件
                toggleStyleFromClickBtn(type);
            }
        });
    }

    /**
     * 插入整段标题或引用文本（可能包含行内样式）
     * 使用场景：如恢复草稿
     *
     * @param blockType           文本段落类型
     * @param content             文本内容
     * @param inlineStyleEntities 行内样式列表
     */
    void insertBlockSpanText(String blockType, SpannableString content, List<RichBlockBean.InlineStyleEntity> inlineStyleEntities) {
        //获取段落标题或者引用文本对象
        IBlockSpan blockSpan = getBlockSpan(blockType);
        content.setSpan(blockSpan, 0, content.length(), getBlockSpanFlag(blockType));
        insertNormalTextBlock(content, inlineStyleEntities);
    }

    /**
     * 插入整段普通文本（可能包含行内样式）
     * 使用场景：如恢复草稿
     *
     * @param content             文本内容
     * @param inlineStyleEntities 行内样式列表
     */
    void insertNormalTextBlock(SpannableString content, List<RichBlockBean.InlineStyleEntity> inlineStyleEntities) {
        int cursorPos = mRichEditText.getSelectionStart();
        if (inlineStyleEntities == null || inlineStyleEntities.isEmpty()) {
            //插入设置好的SpannableString
            insertCharSequence(content, cursorPos);
            return;
        }
        //遍历设置行内样式
        for (RichBlockBean.InlineStyleEntity inlineStyleEntity : inlineStyleEntities) {
            String inlineType = inlineStyleEntity.getInlineType();
            int offset = inlineStyleEntity.getOffset();
            int length = inlineStyleEntity.getLength();
            if (InlineSpanEnum.IMAGE.equals(inlineType)) {
                // TODO 行内的image还未实现
                continue;
            }
            //行内样式对应的span对象，比如加粗、斜体等
            IInlineSpan inlineSpan = getInlineStyleSpan(inlineType);
            content.setSpan(inlineSpan, offset, offset + length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        //插入设置好的SpannableString
        insertCharSequence(content, cursorPos);
    }

    /**
     * 判断光标是否处于行首
     */
    private boolean isCursorInFirstIndexOfLine() {
        int cursor = mRichEditText.getSelectionStart();
        if (cursor < 0 || mRichEditText.length() <= 0) {
            return true;
        }

        Editable editable = mRichEditText.getEditableText();
        return cursor == 0 || editable.charAt(cursor - 1) == '\n';
    }

    /**
     * 在光标位置插入段落ImageSpan（如图片、视频封面、自定义view等）
     *
     * @param blockImageSpan 段落ImageSpan
     */
    void insertBlockImageSpan(BlockImageSpan blockImageSpan) {
        if (blockImageSpan == null) {
            return;
        }
        // 只有正常编辑器插入的ImageSpan才在前面都插入一空行，否则从草稿插入的不再另外加一空行
        if (!blockImageSpan.getBlockSpanObtainObject().isFromDraft() && !isCursorInFirstIndexOfLine()) {
            insertCharSequence("\n", mRichEditText.getSelectionStart());
        }
        //将bitmap插入到editText中，这里采用的是[image]，也可以换成其他的
        SpannableString imageSpannableString = new SpannableString(AppConfig.IMAGE_SPAN_PLACEHOLDER);
        imageSpannableString.setSpan(blockImageSpan, 0, AppConfig.IMAGE_SPAN_PLACEHOLDER.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //插入设置好的SpannableString
        insertCharSequence(imageSpannableString, mRichEditText.getSelectionStart());
        //在imageSpan后面都插入一空行
        insertCharSequence("\n", mRichEditText.getSelectionStart());
    }

    /**
     * 在光标位置插入行内ImageSpan（@人，提及游戏，提及话题等，类似发微博等）
     *
     * @param inlineImageSpan 行内ImageSpan
     */
    void insertInlineImageSpan(InlineSpan inlineImageSpan) {
        if (inlineImageSpan == null) {
            return;
        }
        //将bitmap插入到editText中
        String content = inlineImageSpan.getInlineSpanBean().getValue();
        SpannableString imageSpannableString = new SpannableString(content);
        imageSpannableString.setSpan(inlineImageSpan, 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //插入设置好的SpannableString
        insertCharSequence(imageSpannableString, mRichEditText.getSelectionStart());
//        insertCharSequence("\u200b", mRichEditText.getSelectionStart());
    }

    /**
     * 在光标位置插入@用户或者#话题（@人，提及游戏，提及话题等，类似发微博等）
     *
     * @param atSpan 行内ImageSpan
     */
    void insertAtSpan(AtSpan atSpan) {
        if (atSpan == null) {
            return;
        }
        Editable editable = mRichEditText.getEditableText();
        int start = mRichEditText.getSelectionStart();
        int end = mRichEditText.getSelectionEnd();
        List<StyleBtnBean> styleBtnBeanList = new ArrayList<>();
        IInlineSpan[] inlineSpans = editable.getSpans(start, end, IInlineSpan.class);
        // 可能存在多个分段的span，需要先都移除
        for (IInlineSpan span : inlineSpans) {
            String type = span.getType();
            // 先将两端的span进行切割
            handleInlineStyleBoundary(start, end, getInlineSpanClass(type));
            editable.removeSpan(span);
            styleBtnBeanList.add(mRichStyleBtnMap.get(type));
        }
        //将bitmap插入到editText中
        String content = atSpan.getInlineSpanBean().getValue();
        SpannableString imageSpannableString = new SpannableString(content);
        imageSpannableString.setSpan(atSpan, 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //插入设置好的SpannableString
        insertCharSequence(imageSpannableString, mRichEditText.getSelectionStart());
        //@用户或者#话题，我们再其后面多追加一个空格
        insertCharSequence(" ", mRichEditText.getSelectionStart());
        for (StyleBtnBean styleBtnBean : styleBtnBeanList) {
            styleBtnBean.getClickedView().performClick();
        }
    }

    /**
     * 获取单个行内样式实体
     *
     * @param inlineType     行内标签类型
     * @param spanStart      行内的首
     * @param spanEnd        行内的尾
     * @param blockStart     段落的首
     * @param blockEnd       段落的尾
     * @param inlineSpanBean 段落的行内扩展对象
     * @return 行内样式对象
     */
    private RichBlockBean.InlineStyleEntity getInlineStyleEntity(
            String inlineType, int spanStart, int spanEnd, int blockStart, int blockEnd,
            @Nullable InlineSpanBean inlineSpanBean) {
        RichBlockBean.InlineStyleEntity inlineStyleEntity = new RichBlockBean.InlineStyleEntity();
        inlineStyleEntity.setInlineType(inlineType);
        inlineStyleEntity.setOffset(spanStart - blockStart);
        inlineStyleEntity.setLength(spanEnd - spanStart);
        inlineStyleEntity.setInlineSpan(inlineSpanBean);
        return inlineStyleEntity;
    }

    /**
     * 获取行内样式列表
     *
     * @param blockStart 段落的首
     * @param blockEnd   段落的尾
     */
    private List<RichBlockBean.InlineStyleEntity> getInlineStyleEntities(int blockStart, int blockEnd) {
        if (blockStart > blockEnd) {
            LogUtil.e(TAG, "the start-index is large than the end-index when get inlineStyle entities");
            return null;
        }
        List<RichBlockBean.InlineStyleEntity> inlineStyleEntityList = new ArrayList<>();
        Editable editable = mRichEditText.getEditableText();
        //获取行内的span对象，开始处理
        IInlineSpan[] inlineSpans = editable.getSpans(blockStart, blockEnd, IInlineSpan.class);
        for (IInlineSpan inlineSpan : inlineSpans) {
            int spanStart = Math.max(editable.getSpanStart(inlineSpan), blockStart);
            int spanEnd = Math.min(editable.getSpanEnd(inlineSpan), blockEnd);
            if (spanEnd - spanStart > 0) {
                String inlineType = inlineSpan.getType();
                InlineSpanBean inlineSpanObtainObject = null;
                //遇到行内的图片、@用户、#话题我们需要特殊处理数据
                if (TextUtils.equals(inlineType, InlineSpanEnum.AT) || TextUtils.equals(inlineType, InlineSpanEnum.TOPIC) || TextUtils.equals(inlineType, InlineSpanEnum.LINK)) {
                    AtSpan atSpan = (AtSpan) inlineSpan;
                    inlineSpanObtainObject = atSpan.getInlineSpanBean();
                } else if (TextUtils.equals(inlineType, InlineSpanEnum.IMAGE)) {
                    // TODO: 2022/7/22 处理行内标签图标

                }
                RichBlockBean.InlineStyleEntity inlineStyleEntity = getInlineStyleEntity(
                        inlineType,
                        spanStart,
                        spanEnd,
                        blockStart,
                        blockEnd,
                        inlineSpanObtainObject
                );
                inlineStyleEntityList.add(inlineStyleEntity);
            }
        }
        return inlineStyleEntityList;
    }


    /**
     * 获取编辑的段落对象
     *
     * @param blockType             段落类型
     * @param text                  文本内容
     * @param blockSpanBean         段落扩展对象
     * @param inlineStyleEntityList 行内样式
     * @return 段落对象
     */
    private RichBlockBean getRichBlock(
            @BlockSpanEnum String blockType, String text,
            BlockSpanBean blockSpanBean,
            List<RichBlockBean.InlineStyleEntity> inlineStyleEntityList
    ) {
        RichBlockBean richEditorBlock = new RichBlockBean();
        richEditorBlock.setBlockType(blockType);
        richEditorBlock.setText(text);
        richEditorBlock.setBlockSpan(blockSpanBean);
        richEditorBlock.setInlineStyleEntityList(inlineStyleEntityList);
        return richEditorBlock;
    }


    /**
     * 获取编辑器 对应的 段落列表
     *
     * @return 段落集合
     */
    List<RichBlockBean> getRichBlockBeanList() {
        List<RichBlockBean> richEditorBlockList = new ArrayList<>();
        Editable editableTmp = mRichEditText.getEditableText();
        if (editableTmp.length() <= 0) {
            return richEditorBlockList;
        }

        String editTextContent = mRichEditText.getEditableText().toString();
        //为了代码容易处理，如果文本末尾不是'\n'，则强制在末尾增加一个回车
        if (editTextContent.charAt(editableTmp.length() - 1) != '\n') {
            editTextContent += "\n";
        }
        Editable editable = mRichEditText.getEditableText();
        int editTextLen = editTextContent.length();
        // 先获取所有\n的位置
        List<Integer> enterCharPosList = new ArrayList<>();
        for (int i = 0; i < editTextLen; i++) {
            if (editTextContent.charAt(i) == '\n') {
                enterCharPosList.add(i);
            }
        }

        int enterCharSize = enterCharPosList.size();
        // 通过换行符（\n）来遍历每个段落，每个段落处理一个段落样式和多个行内样式
        for (int i = 0; i < enterCharSize; i++) {
            // 段落的起始位置
            int blockStart = i == 0 ? 0 : enterCharPosList.get(i - 1) + 1;
            int enterCharPos = enterCharPosList.get(i);
            // 先处理段落样式, 包含普通文本、标题、引用、BlockImageSpan,其实就是图片对应的Span
            //先处理图片对应的段落样式
            BlockImageSpan[] blockImageSpans = editable.getSpans(enterCharPos - 1, enterCharPos, BlockImageSpan.class);
            if (blockImageSpans.length > 0) {
                // 说明当前段落是ImageSpan, 没有行内样式
                BlockImageSpan blockImageSpan = blockImageSpans[0];
                BlockSpanBean blockImageSpanBean = blockImageSpan.getBlockSpanObtainObject();
                richEditorBlockList.add(getRichBlock(blockImageSpanBean.getType(), null, blockImageSpanBean, null));
                continue;
            }

            // 当前段落文本
            String blockTextContent;
            if (i == 0) {
                // 第一段就是普通文本
                blockTextContent = editTextContent.substring(0, enterCharPos);
            } else {
                blockTextContent = editTextContent.substring(enterCharPosList.get(i - 1) + 1, enterCharPos);
            }
            //标题或者引用
            IBlockSpan[] blockSpans = editable.getSpans(enterCharPos - 1, enterCharPos, IBlockSpan.class);
            if (blockSpans.length > 0) {
                IBlockSpan blockSpan = blockSpans[0];
                richEditorBlockList.add(getRichBlock(blockSpan.getType(), blockTextContent, null, getInlineStyleEntities(blockStart, enterCharPos)));
                continue;
            }
            // TODO 后续有新增Block类型再说

            // 剩下的就是普通文本（可能包含行内样式）
            richEditorBlockList.add(getRichBlock(BlockSpanEnum.NORMAL_TEXT, blockTextContent, null, getInlineStyleEntities(blockStart, enterCharPos)));
        }
        return richEditorBlockList;
    }

    /**
     * 删除指定的段落ImageSpan（如已插入的图片、视频封面、自定义view等）
     * 场景：用户长按ImageSpan触发弹窗然后点击删除操作
     *
     * @param blockImageSpan 段落ImageSpan
     */
    public void removeBlockImageSpan(BlockImageSpan blockImageSpan) {
        Editable editable = mRichEditText.getEditableText();
        BlockImageSpan[] blockImageSpans = editable.getSpans(0, editable.length(), BlockImageSpan.class);

        for (BlockImageSpan curBlockImageSpan : blockImageSpans) {
            if (curBlockImageSpan == blockImageSpan) {
                int start = editable.getSpanStart(curBlockImageSpan);
                int end = editable.getSpanEnd(curBlockImageSpan);
                editable.removeSpan(curBlockImageSpan);
                editable.delete(start, end);
                break;
            }
        }
    }

    /**
     * 设置SpannableString
     *
     * @param content 文本内容
     * @param pos     插入的索引位置
     */
    void insertCharSequence(CharSequence content, int pos) {
        //获得文本内容
        Editable editable = mRichEditText.getEditableText();
        if (pos < 0 || pos >= editable.length()) {
            editable.append(content);
            mRichEditText.setSelection(editable.length());
        } else {
            editable.insert(pos, content);
            mRichEditText.setSelection(pos + content.length());
        }
    }

    /**
     * 改变标签按钮的样式(图标)
     *
     * @param styleBtnBean 标签按钮的样式对象
     */
    private void changeStyleBtnImage(StyleBtnBean styleBtnBean) {
        ImageView ivIcon = styleBtnBean.getIvIcon();
        if (ivIcon == null) {
            return;
        }

        ivIcon.setImageResource(styleBtnBean.isLight() ? styleBtnBean.getLightResId() : styleBtnBean.getNormalResId());
    }

    /**
     * 改变标签按钮的样式(标题)
     *
     * @param styleBtnBean 标签按钮的样式对象
     */
    private void changeStyleBtnText(StyleBtnBean styleBtnBean) {
        TextView tvTitle = styleBtnBean.getTvTitle();
        if (tvTitle == null) {
            return;
        }

        tvTitle.setTextColor(styleBtnBean.isLight() ? styleBtnBean.getTitleLightColor() : styleBtnBean.getTitleNormalColor());
    }

    /**
     * 判断是否行内样式类型
     *
     * @param type 行内标签类型
     * @return true或者false true表示是行内标签；false则不是
     */
    private boolean isInlineType(String type) {
        switch (type) {
            case BlockSpanEnum.HEADLINE:
            case BlockSpanEnum.QUOTE:
                return false;
        }
        return true;
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
                return new HeadlineSpan(mRichEditText.getHeadlineTextSize());
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
     * 处理行内样式的边界
     * 比如选中的区域start和end分别处于两个指定StyleSpan时间，则需要将这两端的StyleSpan切割成左右两块
     *
     * @param start     索引的首
     * @param end       索引的尾
     * @param spanClazz 执行行内样式class类型
     */
    private void handleInlineStyleBoundary(int start, int end, Class spanClazz) {
        Editable editable = mRichEditText.getEditableText();
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

    /**
     * 获取合并后的span flag
     *
     * @param mergedLeftSpanFlag  被合并的左侧span flag
     * @param mergedRightSpanFlag 被合并的右侧span flag
     * @return 合并后的flag
     */
    private int getMergeSpanFlag(int mergedLeftSpanFlag, int mergedRightSpanFlag) {
        boolean isStartInclusive = false;  // 是否包括左端点
        boolean isEndInclusive = false;    // 是否包括右端点
        if (mergedLeftSpanFlag == Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                || mergedLeftSpanFlag == Spanned.SPAN_INCLUSIVE_INCLUSIVE) {
            isStartInclusive = true;
        }

        if (mergedRightSpanFlag == Spanned.SPAN_INCLUSIVE_INCLUSIVE
                || mergedRightSpanFlag == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
            isEndInclusive = true;
        }

        if (isStartInclusive && isEndInclusive) {
            return Spanned.SPAN_INCLUSIVE_INCLUSIVE;
        }

        if (isStartInclusive) {
            return Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
        }

        if (isEndInclusive) {
            return Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
        }

        return Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
    }

    /**
     * 先合并连续的inline span（由于有切割算法）
     * 时机：
     * 1. 光标在一个位置点击行内样式按钮时；
     * 2. 光标发生变化时
     */
    private void mergeContinuousInlineSpan(int leftPos, int rightPos, Class spanClazz) {
        Editable editable = mRichEditText.getEditableText();
        if (leftPos < 0 || leftPos > editable.length()
                || rightPos < 0 || rightPos > editable.length()
                || leftPos > rightPos) {
            return;
        }
        if (leftPos > 0) {
            IInlineSpan[] leftSpans = (IInlineSpan[]) editable.getSpans(leftPos, leftPos, spanClazz);
            if (leftSpans.length >= 2) {
                IInlineSpan leftSpan = null;
                int resSpanStart = 0;
                int resSpanEnd = rightPos;
                for (IInlineSpan span : leftSpans) {
                    if (editable.getSpanStart(span) < leftPos) {
                        resSpanStart = editable.getSpanStart(span);
                        leftSpan = span;
                        break;
                    }
                }
                if (leftSpan != null) {
                    int leftSpanFlags = editable.getSpanFlags(leftSpan);
                    int rightSpanFlags = Spanned.SPAN_INCLUSIVE_INCLUSIVE;
                    for (IInlineSpan span : leftSpans) {
                        if (editable.getSpanStart(span) < leftPos) {
                            editable.removeSpan(span);
                        }
                        if (editable.getSpanStart(span) == leftPos && editable.getSpanEnd(span) == rightPos) {
                            rightSpanFlags = editable.getSpanFlags(span);
                            editable.removeSpan(span);
                        }
                    }
                    IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
                    editable.setSpan(wantAddSpan, resSpanStart, resSpanEnd, getMergeSpanFlag(leftSpanFlags, rightSpanFlags));
                }
            }
        }
        if (rightPos < editable.length()) {
            IInlineSpan[] rightSpans = (IInlineSpan[]) editable.getSpans(rightPos, rightPos, spanClazz);
            if (rightSpans.length >= 2) {
                IInlineSpan curRightSpan = null;
                IInlineSpan curLeftSpan = null;
                int resSpanStart = 0;
                int resSpanEnd = 0;
                for (IInlineSpan span : rightSpans) {
                    if (editable.getSpanEnd(span) == rightPos) {
                        curLeftSpan = span;
                        resSpanStart = editable.getSpanStart(span);
                    } else if (editable.getSpanEnd(span) > rightPos) {
                        curRightSpan = span;
                        resSpanEnd = editable.getSpanEnd(span);
                    }
                }

                if (curLeftSpan != null && curRightSpan != null) {
                    int leftSpanFlags = editable.getSpanFlags(curLeftSpan);
                    int rightSpanFlags = editable.getSpanFlags(curRightSpan);
                    for (IInlineSpan span : rightSpans) {
                        editable.removeSpan(span);
                    }
                    IInlineSpan wantAddSpan = getInlineStyleSpan(spanClazz);
                    editable.setSpan(wantAddSpan, resSpanStart, resSpanEnd, getMergeSpanFlag(leftSpanFlags, rightSpanFlags));
                }
            }
        }
    }

    /**
     * 判断是否为文本block（包括headlineBlock）
     *
     * @return true-光标在文本block中，false-光标不在文本block中
     */
    private boolean isTextBlock() {
        int cursorPos = mRichEditText.getSelectionStart();
        if (cursorPos == 0) {
            return true;
        }
        Editable editable = mRichEditText.getEditableText();
        // 由于ImageSpan包括行内ImageSpan（@xxx等）和段落ImageSpan（图片、视频封面、自定义布局等）
        BlockImageSpan[] imageSpans = editable.getSpans(cursorPos - 1, cursorPos, BlockImageSpan.class);
        return imageSpans.length <= 0;
    }

    private void notifyUndoRedoStyleChanged(String type, int start, int end) {
        UndoRedoHelper.Action action = new UndoRedoHelper.Action(start, end, type);
        mToggleStyleObservable.notifyChange(action);
    }

    public void registerToggleStyleObserver(UndoRedoHelper.ToggleStyleObserver observer) {
        mToggleStyleObservable.registerObserver(observer);
    }

    /**
     * 放用户点击按钮（加粗、斜体、标题等）的时候
     *
     * @param type 标签类型
     */
    private void toggleStyleFromClickBtn(String type) {
        int start = mRichEditText.getSelectionStart();
        int end = mRichEditText.getSelectionEnd();
        toggleStyle(type, start, end, false);
    }

    /**
     * 撤回的标签样式
     *
     * @param type  标签类型
     * @param start 索引首
     * @param end   索引尾
     */
    public void toggleStyleFromUndoRedo(@InlineSpanEnum String type, int start, int end) {
        toggleStyle(type, start, end, true);
    }


    /**
     * 处理行内标签样式
     *
     * @param start        起始位置
     * @param end          结束位置
     * @param spanClazz    标签class对象
     * @param styleBtnBean 标签对应按钮样式
     */
    private void handleInlineStyle(int start, int end, Class spanClazz, StyleBtnBean styleBtnBean) {
        Editable editable = mRichEditText.getEditableText();

        IInlineSpan[] inlineSpans = (IInlineSpan[]) editable.getSpans(start, end, spanClazz);

        // 先将两端的span进行切割
        handleInlineStyleBoundary(start, end, spanClazz);

        // 可能存在多个分段的span，需要先都移除
        for (IInlineSpan span : inlineSpans) {
            editable.removeSpan(span);
        }

        if (styleBtnBean.isLight()) {
            int flags = start == end ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
            editable.setSpan(getInlineStyleSpan(spanClazz), start, end, flags);
            mergeContinuousInlineSpan(start, end, spanClazz);
        }
    }

    /**
     * 处理样式修改
     */
    private void toggleStyle(@InlineSpanEnum String type, int start, int end, boolean isFromUndoRedo) {
        if (!isTextBlock()) {
            return;
        }

        StyleBtnBean styleBtnBean = mRichStyleBtnMap.get(type);
        if (styleBtnBean == null) {
            return;
        }

        Class spanClazz = getInlineSpanClass(type);
        if (spanClazz == null) {
            return;
        }
        styleBtnBean.setLight(!styleBtnBean.isLight()); // 状态取反
        changeStyleBtnImage(styleBtnBean);
        changeStyleBtnText(styleBtnBean);

        if (!styleBtnBean.isInlineType()) {
            // 段落样式（标题、引用）
            handleBlockType(styleBtnBean);
        } else {
            // 行内样式
            handleInlineStyle(start, end, spanClazz, styleBtnBean);
        }

        if (!isFromUndoRedo) {
            notifyUndoRedoStyleChanged(type, start, end);
        }
    }

    /**
     * 通过样式类型获取对应Class
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
     * 通过样式类型获取对应Class
     */
    private Class getBlockSpanClass(@BlockSpanEnum String type) {
        switch (type) {
            case BlockSpanEnum.HEADLINE:
                return HeadlineSpan.class;
            case BlockSpanEnum.QUOTE:
                return CustomQuoteSpan.class;
            default:
                return null;
        }
    }

    /**
     * 获取光标位置的block的起始位置和终止位置
     *
     * @return block的起始位置和终止位置的二维int数组
     */
    private int[] getCursorPosBlockBoundary() {
        int[] blockBoundaryArr = new int[2];
        int cursorPos = mRichEditText.getSelectionStart();
        if (cursorPos == -1) {
            cursorPos = 0;
        }
        String content = mRichEditText.getEditableText().toString();
        int size = content.length();
        for (int i = cursorPos - 1; i >= 0; i--) {
            if (i >= size) {
                continue;
            }
            if (content.charAt(i) == '\n') {
                blockBoundaryArr[0] = i + 1;
                break;
            }
            if (i == 0) {
                blockBoundaryArr[0] = 0;
                break;
            }
        }
        blockBoundaryArr[1] = cursorPos; //若当前光标是最后一个位置
        for (int i = cursorPos; i < size; i++) {
            if (i == size - 1) {
                if (content.charAt(i) != '\n') {
                    blockBoundaryArr[1] = i + 1;
                } else {
                    blockBoundaryArr[1] = i;
                }
                break;
            }
            if (content.charAt(i) == '\n') {
                blockBoundaryArr[1] = i;
                break;
            }
        }
        return blockBoundaryArr;
    }

    /**
     * 删除block span
     *
     * @param spanClazz 当前执行的block span class
     * @param start     起始位置
     * @param end       终止位置
     */
    private void removeBlockSpan(Class spanClazz, int start, int end) {
        Editable editable = mRichEditText.getEditableText();
        IBlockSpan[] blockSpans;
        if (spanClazz == null) {
            blockSpans = editable.getSpans(start, end, IBlockSpan.class);
        } else {
            blockSpans = (IBlockSpan[]) editable.getSpans(start, end, spanClazz);
        }
        for (IBlockSpan blockSpan : blockSpans) {
            editable.removeSpan(blockSpan);
        }
    }

    /**
     * 当点击任意一个段落类型的按钮时，置灰其他段落类型的按钮，同时修改数据
     *
     * @param curBlockType 当前点击的段落类型
     */
    private void setOtherBlockStyleBtnDisable(@InlineSpanEnum String curBlockType) {
        for (StyleBtnBean styleBtnBean : mRichStyleBtnMap.values()) {
            if (styleBtnBean.isInlineType() && !styleBtnBean.getType().equals(curBlockType)) {
                styleBtnBean.setLight(false);
                changeStyleBtnImage(styleBtnBean);
                changeStyleBtnText(styleBtnBean);
            }
        }
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

    private int getCursorHeight(@InlineSpanEnum String type) {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionStart();
        BlockImageSpan[] blockImageSpans = editable.getSpans(cursorPos - 1, cursorPos, BlockImageSpan.class);

        if (blockImageSpans.length > 0) {
            // 若光标处于ImageSpan段落上，则光标的高度设置为图片的高度即可
            return blockImageSpans[0].getDrawable().getIntrinsicHeight();
        }

        switch (type) {
            case BlockSpanEnum.HEADLINE:
                return (int) (mRichEditText.getHeadlineTextSize() * 1.25);
            case BlockSpanEnum.QUOTE:
                return (int) (mActivity.getResources().getDimension(R.dimen.rich_editor_quote_text_size) * 1.25);
        }

        return (int) (mRichEditText.getTextSize() * 1.25);
    }

    /**
     * 重置光标高度为默认的高度
     */
    private void resetCursorHeight() {
        mRichEditText.setCursorHeight((int) (mRichEditText.getTextSize() * 1.25));
    }

    /**
     * 处理段内样式（标题、引用等）
     *
     * @param styleBtnBean 样式按钮实体
     */
    private void handleBlockType(StyleBtnBean styleBtnBean) {
        String blockType = styleBtnBean.getType();
        int[] curBlockBoundary = getCursorPosBlockBoundary();
        int start = curBlockBoundary[0];
        int end = curBlockBoundary[1];
        Editable editable = mRichEditText.getEditableText();
        Class spanClazz = getBlockSpanClass(blockType);
        if (styleBtnBean.isLight()) {
            removeBlockSpan(null, start, end);
            editable.setSpan(getBlockSpan(blockType), start, end, getBlockSpanFlag(blockType));
            mRichEditText.setCursorHeight(getCursorHeight(blockType));
            setOtherBlockStyleBtnDisable(blockType);
        } else {
            // 为了避免重复，移除统一段落自己类型的BlockSpan
            removeBlockSpan(spanClazz, start, end);
            resetCursorHeight();
        }
    }

    /**
     * 处理段落样式各个按钮的状态（点亮或置灰）
     *
     * @param type 样式类型
     */
    private boolean handleBlockTypeButtonStatus(@BlockSpanEnum String type) {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionEnd();
        String content = editable.toString();
        IBlockSpan[] blockSpans;
        if (cursorPos <= 0 || content.charAt(cursorPos - 1) == '\n') {
            // 若光标处于block的起始位置
            blockSpans = (IBlockSpan[]) editable.getSpans(cursorPos, cursorPos + 1, getBlockSpanClass(type));
        } else {
            //光标处于block的中间或末尾
            blockSpans = (IBlockSpan[]) editable.getSpans(cursorPos - 1, cursorPos, getBlockSpanClass(type));
        }

        return blockSpans.length > 0;
    }

    /**
     * 处理行内样式各个按钮的状态（点亮或置灰）
     *
     * @param type 样式类型
     */
    private boolean handleInlineStyleButtonStatus(@InlineSpanEnum String type) {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionEnd();
        IInlineSpan[] inlineSpans = (IInlineSpan[]) editable.getSpans(cursorPos, cursorPos, getInlineSpanClass(type));
        if (inlineSpans.length <= 0) {
            return false;
        }

        boolean isLight = false; //是否点亮

        for (IInlineSpan span : inlineSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            int spanFlag = editable.getSpanFlags(span);
            if (spanStart < cursorPos && spanEnd > cursorPos) {
                isLight = true;
            } else if (spanStart == cursorPos
                    && (spanFlag == Spanned.SPAN_INCLUSIVE_INCLUSIVE || spanFlag == Spanned.SPAN_INCLUSIVE_EXCLUSIVE)) {
                isLight = true;
            } else if (spanEnd == cursorPos
                    && (spanFlag == Spanned.SPAN_INCLUSIVE_INCLUSIVE || spanFlag == Spanned.SPAN_EXCLUSIVE_INCLUSIVE)) {
                isLight = true;
            }
        }

        return isLight;
    }

    /**
     * 将所有按钮置灰
     */
    private void clearStyleButtonsStatus() {
        for (StyleBtnBean styleBtnBean : mRichStyleBtnMap.values()) {
            styleBtnBean.setLight(false);
            changeStyleBtnImage(styleBtnBean);
            changeStyleBtnText(styleBtnBean);
        }
    }

    /**
     * 修改各个按钮的状态（点亮或置灰）
     */
    private void handleStyleButtonsStatus() {
        // 先将所有按钮置灰
        clearStyleButtonsStatus();

        for (String type : mRichStyleBtnMap.keySet()) {
            boolean isLight;
            if (isInlineType(type)) {
                isLight = handleInlineStyleButtonStatus(type);
            } else {
                isLight = handleBlockTypeButtonStatus(type);
            }

            if (isLight) {
                StyleBtnBean styleBtnBean = mRichStyleBtnMap.get(type);
                if (styleBtnBean == null) {
                    continue;
                }
                styleBtnBean.setLight(true);
                changeStyleBtnImage(styleBtnBean);
                changeStyleBtnText(styleBtnBean);
            }
        }
    }

    /**
     * 光标发生变化的时候，若光标的位置处于某个span的右侧，则将该span的end恢复成包含（inclusive）
     *
     * @param cursorPos 当前位置
     * @param spanClazz 具体的spanClazz
     */
    private void restoreSpanEndToInclusive(int cursorPos, Class spanClazz) {
        Editable editable = mRichEditText.getEditableText();
        ParcelableSpan[] parcelableSpans = (ParcelableSpan[]) editable.getSpans(cursorPos, cursorPos, spanClazz);
        for (ParcelableSpan span : parcelableSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            if (spanEnd == cursorPos) {
                editable.removeSpan(span);
                if (span instanceof IInlineSpan) {
                    editable.setSpan(getInlineStyleSpan(spanClazz), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                } else if (span instanceof IBlockSpan) {
                    IBlockSpan blockSpan = (IBlockSpan) span;
//                    IBlockSpan newBlockSpan = getBlockSpan(spanClazz);
                    editable.setSpan(blockSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
    }

    /**
     * 改变光标的高度
     */
    private void changeCursorHeight() {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionStart();
        IBlockSpan[] blockSpans = editable.getSpans(cursorPos, cursorPos, IBlockSpan.class);
        if (blockSpans.length == 0) {
            // 当前段落没有block样式
            resetCursorHeight();
            return;
        }

        IBlockSpan blockSpan = blockSpans[0];
        mRichEditText.setCursorHeight(getCursorHeight(blockSpan.getType()));
    }

    /**
     * 处理光标的位置变化
     *
     * @param selStart 当前光标起始位置
     * @param selEnd   当前光标结束位置
     */
    private void handleSelectionChanged(int selStart, int selEnd) {
        Editable editable = mRichEditText.getEditableText();
        if (editable.length() <= 0 && selEnd <= 0) {
            mRichEditText.requestFocus();
            mRichEditText.setSelection(0);
            // 修改各个按钮的状态（点亮或置灰）
            clearStyleButtonsStatus();
            // 根据光标的位置动态修改光标的高度
            changeCursorHeight();
            // 可能存在多个分段的span，需要先都移除
            IInlineSpan[] inlineSpans = editable.getSpans(selStart, selEnd, IInlineSpan.class);
            for (IInlineSpan span : inlineSpans) {
                editable.removeSpan(span);
            }
            return;
        }
        AtSpan[] atSpans = editable.getSpans(selStart, selEnd, AtSpan.class);
        if (atSpans.length > 0) {
            //获取@用户的开始索引
            for (int i = 0; i < atSpans.length; i++) {
                AtSpan atSpan = atSpans[i];
                int startPosition = editable.getSpanStart(atSpan);
                int endPosition = editable.getSpanEnd(atSpan);
                if (startPosition != -1 && selStart > startPosition && selEnd <= endPosition) {
                    mRichEditText.setSelection(endPosition);
                }
            }
            if (selStart == selEnd) {
                //表示在首位置或者尾位置
                BackgroundColorSpan[] backgroundColorSpans = editable.getSpans(selStart, selEnd, BackgroundColorSpan.class);
                for (BackgroundColorSpan span : backgroundColorSpans) {
                    editable.removeSpan(span);
                }
            }
            return;
        }

        // 先合并指定位置前后连续的行内样式
        for (String type : mRichStyleBtnMap.keySet()) {
            boolean isInlineType = isInlineType(type);
            Class typeClass = getInlineSpanClass(type);
            if (isInlineType) {
                mergeContinuousInlineSpan(selStart, selEnd, typeClass);
            }
            restoreSpanEndToInclusive(selEnd, typeClass);
        }

        // 修改各个按钮的状态（点亮或置灰）
        handleStyleButtonsStatus();

        // 根据光标的位置动态修改光标的高度
        changeCursorHeight();
    }

    /**
     * 处理删除按键
     * 1、删除BlockImageSpan的时候，直接将光标定位到上一行末尾
     * 2、当光标处于BlockImageSpan下一行的第一个位置（不是EditText最后一个字符）上按删除按键时,
     * 不删除字符，而是将光标定位到上一行的末尾（即BlockImageSpan的末尾）
     * 3、当光标处于BlockImageSpan的行首按删除按键时，如果上一行不是空行，则不删除字符，而是将光标移动到上一行的末尾
     * 4、当光标处于AtSpan(@用户)或者TopicSpan(话题)的内容中，或者在话题后面时，第一次按下删除键，实现选中文字效果，并且修改文本背景色，再次点击删除话题文本
     */
    private boolean handleDeleteKey() {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionStart();
        if (cursorPos == 0) {
            return false;
        }

        //删除imageSpan的时候直接将光标定位到上一行末尾
        BlockImageSpan[] imageSpans = editable.getSpans(cursorPos - 1, cursorPos, BlockImageSpan.class);
        if (imageSpans.length > 0) {
            ImageSpan imageSpan = imageSpans[0];
            int start = editable.getSpanStart(imageSpan);
            int end = editable.getSpanEnd(imageSpan);
            if (start > 0) {
                start--; //光标跳到上一行
            }
            editable.delete(start, end);
            return true;
        }

        //当光标处于imageSpan下一行（当行不是空行）的第一个位置上按删除按键时
        BlockImageSpan[] imageSpans1 = editable.getSpans(cursorPos - 2, cursorPos - 1, BlockImageSpan.class);
        String content = mRichEditText.getEditableText().toString();
        if (imageSpans1.length > 0 && cursorPos < content.length() && content.charAt(cursorPos) != '\n') {
            mRichEditText.setSelection(cursorPos - 1);
            return true;
        }

        // 当光标处于BlockImageSpan的行首按删除按键时，如果上一行不是空行，则不删除字符，而是将光标移动到上一行的末尾
        BlockImageSpan[] imageSpans2 = editable.getSpans(cursorPos, cursorPos + 1, BlockImageSpan.class);
        if (imageSpans2.length > 0 && cursorPos >= 2
                && content.charAt(cursorPos - 1) == '\n' && content.charAt(cursorPos - 2) != '\n') {
            mRichEditText.setSelection(cursorPos - 1);
            return true;
        }

        int selectionEnd = mRichEditText.getSelectionEnd();
        if (cursorPos != selectionEnd) {
            AtSpan[] atSpans = editable.getSpans(cursorPos, cursorPos, AtSpan.class);
            if (atSpans.length > 0) {
                for (AtSpan atSpan : atSpans) {
                    editable.removeSpan(atSpan);
                }
            }
            return false;
        }
        AtSpan[] atSpans = editable.getSpans(cursorPos, cursorPos, AtSpan.class);
        if (atSpans.length > 0) {
            //获取@用户的开始索引
            for (int i = 0; i < atSpans.length; i++) {
                AtSpan atSpan = atSpans[i];
                int startPosition = editable.getSpanStart(atSpan);
                int endPosition = editable.getSpanEnd(atSpan);
                if (startPosition >= 0 && cursorPos >= startPosition && cursorPos <= endPosition) {
                    mRichEditText.setSelection(startPosition, endPosition);
                    editable.setSpan(new BackgroundColorSpan(
                                    Color.BLUE), startPosition, endPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 删除回车之后需要处理两行的block span合并，修改成第一行的block样式
     */
    void mergeBlockSpanAfterDeleteEnter() {
        Editable editable = mRichEditText.getEditableText();
        int[] curBlockBoundary = getCursorPosBlockBoundary();
        int start = curBlockBoundary[0];
        int end = curBlockBoundary[1];
        // 合并前第一行的block span
        IBlockSpan[] firstBlockSpans = editable.getSpans(start, start, IBlockSpan.class);
        // 合并后的block span
        IBlockSpan[] mergeBlockSpans = editable.getSpans(start, end, IBlockSpan.class);

        // 清空合并后的block span
        for (IBlockSpan blockSpan : mergeBlockSpans) {
            editable.removeSpan(blockSpan);
        }

        if (firstBlockSpans.length <= 0) {
            return;
        }

        String blockType = firstBlockSpans[0].getType();
        editable.setSpan(getBlockSpan(blockType), start, end, getBlockSpanFlag(blockType));
        mRichEditText.setCursorHeight(getCursorHeight(blockType));
        setOtherBlockStyleBtnDisable(blockType);
    }

    /**
     * 换行后，清除上一行的样式     *
     */
    void changeLastBlockOrInlineSpanFlag() {
        Editable editable = mRichEditText.getEditableText();
        int cursorPos = mRichEditText.getSelectionStart();

        // 先处理行内样式
        IInlineSpan[] inlineSpans = editable.getSpans(cursorPos - 1, cursorPos - 1, IInlineSpan.class);

        for (IInlineSpan span : inlineSpans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            if (cursorPos > end) {
                continue;
            }
            Class spanClazz = getInlineSpanClass(span.getType());
            IInlineSpan newSpan = getInlineStyleSpan(spanClazz);
            editable.removeSpan(span);
            if (cursorPos == end) {
                // 在span末尾点击了回车
                editable.setSpan(newSpan, start, end - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                // 在span中间点击了回车, 需要将一个span拆成两个span
                editable.setSpan(newSpan, start, cursorPos - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                IInlineSpan newRightSpan = getInlineStyleSpan(spanClazz);
                editable.setSpan(newRightSpan, cursorPos, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        // 再处理段落样式
        IBlockSpan[] blockSpans = editable.getSpans(cursorPos - 1, cursorPos - 1, IBlockSpan.class);

        for (IBlockSpan span : blockSpans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            if (cursorPos > end) {
                continue;
            }
            String blockType = span.getType();
            editable.removeSpan(span);
            if (cursorPos == end) {
                // 在span末尾点击了回车
                IBlockSpan newSpan = getBlockSpan(blockType);
                editable.setSpan(newSpan, start, end - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                // 在span中间点击了回车, 需要将一个span拆成两个span
                // 这里需要考虑在span的起始位置或者中间插入了图片
                String leftSpanContent = editable.toString().substring(start, cursorPos - 1);
                String rightSpanContent = editable.toString().substring(cursorPos, end);
                int leftSpanEnd = cursorPos - 1;
                if (leftSpanContent.contains(AppConfig.IMAGE_SPAN_PLACEHOLDER)) {
                    // 在图片后面blockSpan前面插入了回车符
                    leftSpanContent = leftSpanContent.substring(0,
                            leftSpanContent.length() - AppConfig.IMAGE_SPAN_PLACEHOLDER.length());
                    leftSpanEnd -= AppConfig.IMAGE_SPAN_PLACEHOLDER.length();
                }
                if (!leftSpanContent.isEmpty()) {
                    IBlockSpan newLeftSpan = getBlockSpan(blockType);
                    editable.setSpan(newLeftSpan, start, leftSpanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (!rightSpanContent.isEmpty()) {
                    IBlockSpan newRightSpan = getBlockSpan(blockType);
                    editable.setSpan(newRightSpan, cursorPos, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    public class ToggleStyleObservable extends Observable<UndoRedoHelper.ToggleStyleObserver> {
        void notifyChange(UndoRedoHelper.Action action) {
            for (UndoRedoHelper.ToggleStyleObserver observer : mObservers) {
                observer.onChange(action);
            }
        }
    }

}
