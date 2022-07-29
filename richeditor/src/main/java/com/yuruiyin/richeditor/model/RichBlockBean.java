package com.yuruiyin.richeditor.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

import java.util.List;

/**
 * 富文本编辑器中块级(段落)的实体对象
 *
 * @author admin
 */
public class RichBlockBean {

    /**
     * 段落类型：如普通文本、标题、引用、以及调用方自定义的各种类型的ImageSpan(包含图片、视频封面、其它自定义view)等。
     * 其中，文本段落都可能包含段落样式和行内样式
     */
    private @BlockSpanEnum
    String blockType;

    /**
     * 文本内容，只有文本段落才有值
     */
    private String text;

    /**
     * 段落可扩展对象对应的实体，比如图片、自定义view等带换行的都可以认为是段落级的
     */
    private @Nullable
    BlockSpanBean blockSpanBean;

    /**
     * 行内样式列表（一个段落可能包含多个行内样式）
     */
    private List<InlineStyleEntity> inlineStyleEntityList;

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public BlockSpanBean getBlockSpan() {
        return blockSpanBean;
    }

    public void setBlockSpan(@Nullable BlockSpanBean blockSpanBean) {
        this.blockSpanBean = blockSpanBean;
    }

    public List<InlineStyleEntity> getInlineStyleEntityList() {
        return inlineStyleEntityList;
    }

    public void setInlineStyleEntityList(List<InlineStyleEntity> inlineStyleEntityList) {
        this.inlineStyleEntityList = inlineStyleEntityList;
    }

    /**
     * 行内样式的Entity
     */
    public static class InlineStyleEntity {
        /**
         * 行内样式：如加粗、斜体、行内Span（如@人、提及游戏、插入话题）等
         */
        private @InlineSpanEnum
        String inlineType;

        /**
         * 该行内样式在段落中的偏移量，起始就是索引
         */
        private int offset;

        /**
         * 该行内样式所占有的字符长度
         */
        private int length;

        /**
         * 该行内ImageSpan所包含的实体, 如@人、提及游戏、插入话题。若是加粗、斜体等行内样式，则可为null
         */
        private @Nullable
        InlineSpanBean inlineSpanBean;

        public String getInlineType() {
            return inlineType;
        }

        public void setInlineType(String inlineType) {
            this.inlineType = inlineType;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Nullable
        public InlineSpanBean getInlineSpan() {
            return inlineSpanBean;
        }

        public void setInlineSpan(@Nullable InlineSpanBean inlineSpanBean) {
            this.inlineSpanBean = inlineSpanBean;
        }

        @NonNull
        @Override
        public String toString() {
            return "InlineStyleEntity{" +
                    "inlineType='" + inlineType + '\'' +
                    ", offset=" + offset +
                    ", length=" + length +
                    ", inlineSpanBean=" + inlineSpanBean +
                    '}';
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "RichEditorBlock{" +
                "blockType='" + blockType + '\'' +
                ", text='" + text + '\'' +
                ", blockSpanBean=" + blockSpanBean +
                ", inlineStyleEntityList=" + inlineStyleEntityList +
                '}';
    }
}
