package com.yuruiyin.richeditor.enumtype;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 行内标签类型
 *
 * @author admin
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({InlineSpanEnum.NORMAL, InlineSpanEnum.BOLD, InlineSpanEnum.ITALIC, InlineSpanEnum.STRIKE_THROUGH, InlineSpanEnum.UNDERLINE, InlineSpanEnum.AT, InlineSpanEnum.TOPIC,
        InlineSpanEnum.LINK, InlineSpanEnum.IMAGE})
public @interface InlineSpanEnum {

    /**
     * 正常
     */
    String NORMAL = "normal";

    /**
     * 加粗
     */
    String BOLD = "bold";

    /**
     * 斜体
     */
    String ITALIC = "italic";

    /**
     * 删除线
     */
    String STRIKE_THROUGH = "strike_through";

    /**
     * 下划线
     */
    String UNDERLINE = "underline";

    /**
     * 表示 @别人
     */
    String AT = "at";

    /**
     * 话题
     */
    String TOPIC = "topic";

    /**
     * 链接
     */
    String LINK = "link";

    /**
     * 行内ImageSpan，也就是图标、表情之类的
     */
    String IMAGE = "image";
}
