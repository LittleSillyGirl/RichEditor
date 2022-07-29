package com.yuruiyin.richeditor.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 样式实体
 * 简单理解就是带选中的标签样式
 *
 * @author admin
 */
public class StyleBtnBean {

    /**
     * 具体类型（包含粗体、斜体、标题、下划线等）
     */
    private @InlineSpanEnum
    final
    String type;

    /**
     * 是否行内样式
     */
    private boolean isInlineType;

    /**
     * 是否点亮，也就是图标是否选中
     */
    private boolean isLight;

    /**
     * 按钮ImageView
     */
    private final ImageView ivIcon;

    /**
     * 正常的资源id
     */
    private final int iconNormalResId;

    /**
     * 点亮的资源id，也就是选中的资源id
     */
    private final int iconLightResId;

    /**
     * 被点击的view
     */
    private final View clickedView;

    /**
     * 标题文本（如粗体、斜体、标题等）
     */
    private final TextView tvTitle;

    /**
     * 标题文本正常的颜色
     */
    private @ColorInt
    final int titleNormalColor;

    /**
     * 标题文本点亮的颜色
     */
    private @ColorInt
    final int titleLightColor;

    public StyleBtnBean(Builder builder) {
        this.type = builder.type;
        this.ivIcon = builder.ivIcon;
        this.isLight = false;
        this.iconNormalResId = builder.iconNormalResId;
        this.iconLightResId = builder.iconLightResId;
        this.clickedView = builder.clickedView;
        this.tvTitle = builder.tvTitle;
        this.titleNormalColor = builder.titleNormalColor;
        this.titleLightColor = builder.titleLightColor;
    }

    public String getType() {
        return type;
    }

    public ImageView getIvIcon() {
        return ivIcon;
    }

    public boolean isLight() {
        return isLight;
    }

    public void setLight(boolean light) {
        isLight = light;
    }

    public int getNormalResId() {
        return iconNormalResId;
    }

    public int getLightResId() {
        return iconLightResId;
    }

    public boolean isInlineType() {
        return isInlineType;
    }

    public void setInlineType(boolean inlineType) {
        isInlineType = inlineType;
    }

    public View getClickedView() {
        return clickedView;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public int getTitleNormalColor() {
        return titleNormalColor;
    }

    public int getTitleLightColor() {
        return titleLightColor;
    }

    public static class Builder {
        /**
         * 具体类型（包含粗体、斜体、标题等）
         */
        private @InlineSpanEnum
        String type;

        /**
         * 按钮ImageView
         */
        private ImageView ivIcon;

        /**
         * 正常的资源id
         */
        private int iconNormalResId;

        /**
         * 点亮的资源id
         */
        private int iconLightResId;

        /**
         * 被点击的view
         */
        private View clickedView;

        /**
         * 标题文本（如粗体、斜体、标题等）
         */
        private TextView tvTitle;

        /**
         * 标题文本正常的颜色
         */
        private @ColorInt
        int titleNormalColor;

        /**
         * 标题文本点亮的颜色
         */
        private @ColorInt
        int titleLightColor;

        public Builder setType(@InlineSpanEnum String type) {
            this.type = type;
            return this;
        }

        public Builder setIvIcon(ImageView ivIcon) {
            this.ivIcon = ivIcon;
            return this;
        }

        public Builder setIconNormalResId(int iconNormalResId) {
            this.iconNormalResId = iconNormalResId;
            return this;
        }

        public Builder setIconLightResId(int iconLightResId) {
            this.iconLightResId = iconLightResId;
            return this;
        }

        public Builder setTvTitle(TextView tvTitle) {
            this.tvTitle = tvTitle;
            return this;
        }

        public Builder setTitleNormalColor(@ColorInt int titleNormalColor) {
            this.titleNormalColor = titleNormalColor;
            return this;
        }

        public Builder setTitleLightColor(@ColorInt int titleLightColor) {
            this.titleLightColor = titleLightColor;
            return this;
        }

        public Builder setClickedView(View clickedView) {
            this.clickedView = clickedView;
            return this;
        }

        public StyleBtnBean build() {
            return new StyleBtnBean(this);
        }
    }
}
