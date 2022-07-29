package com.yuruiyin.richeditor.enumtype;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 段落标签的枚举类
 *
 * @author admin
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({BlockSpanEnum.IMAGE, BlockSpanEnum.VIDEO, BlockSpanEnum.DIVIDER, BlockSpanEnum.GAME, BlockSpanEnum.HEADLINE, BlockSpanEnum.QUOTE, BlockSpanEnum.NORMAL_TEXT})
public @interface BlockSpanEnum {

    /**
     * 段落普通文本（但是可能包含行内样式）
     */
    String NORMAL_TEXT = "normal_text";

    /**
     * 相册图片
     */
    String IMAGE = "image";

    /**
     * 视频
     */
    String VIDEO = "video";

    /**
     * 分割线
     */
    String DIVIDER = "divider";

    /**
     * 游戏
     */
    String GAME = "game";

    /**
     * 段落标题
     */
    String HEADLINE = "headline";

    /**
     * 段落引用
     */
    String QUOTE = "quote";


}
