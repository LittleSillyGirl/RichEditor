package com.yuruiyin.richeditor.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 行内标签对应的实体类
 *
 * @author admin
 */
public class InlineSpanBean<T> {

    /**
     * 行内标签类型
     */
    @InlineSpanEnum
    private String inlineType;

    /**
     * #话题、@用户登场景的唯一标识
     */
    private String key;

    /**
     * #话题、@用户登场景的要显示的文本
     */
    private String value;

    /**
     * #话题、@用户登场景的文本颜色
     */
    private String textColor;

    /**
     * #话题、@用户登场景的文本大小
     */
    private float textSize;

    /**
     * 可扩展的对象
     */
    private T spanObtainObject;

    public InlineSpanBean() {
    }

    public InlineSpanBean(@InlineSpanEnum String inlineType, String key, String value) {
        this(inlineType, key, value, "", 15);
    }

    public InlineSpanBean(@InlineSpanEnum String inlineType, String key, String value, String textColor, float textSize) {
        this.inlineType = inlineType;
        this.key = key;
        this.value = value;
        this.textColor = TextUtils.isEmpty(textColor) ? "#31BC63" : textColor;
        this.textSize = textSize;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public T getSpanObtainObject() {
        return spanObtainObject;
    }

    public void setSpanObtainObject(T spanObtainObject) {
        this.spanObtainObject = spanObtainObject;
    }

    @NonNull
    @Override
    public String toString() {
        return "InlineSpanBean{" +
                "inlineType='" + inlineType + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", textColor='" + textColor + '\'' +
                ", textSize=" + textSize +
                ", spanObtainObject=" + spanObtainObject +
                '}';
    }

    /**
     * 获取行内标签的类型，比如加粗、斜体、下划线等
     *
     * @return 行内标签类型
     */
    @InlineSpanEnum
    public String getType() {
        return TextUtils.isEmpty(inlineType) ? InlineSpanEnum.NORMAL : inlineType;
    }
}
